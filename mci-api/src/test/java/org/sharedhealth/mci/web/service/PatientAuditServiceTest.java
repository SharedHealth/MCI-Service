package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientAuditRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientFeedRepository;
import org.sharedhealth.mci.web.mapper.PatientAuditLogData;
import org.sharedhealth.mci.web.mapper.Requester;
import org.sharedhealth.mci.web.mapper.RequesterDetails;
import org.sharedhealth.mci.web.model.PatientAuditLog;
import org.sharedhealth.mci.web.model.PatientUpdateLog;

import java.util.*;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.service.PatientAuditService.UPDATE_LOG_LIMIT;

public class PatientAuditServiceTest {

    @Mock
    private PatientAuditRepository auditRepository;
    @Mock
    private PatientFeedRepository feedRepository;
    @Mock
    private RequesterService requesterService;

    private PatientAuditService auditService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        auditService = new PatientAuditService(auditRepository, feedRepository, requesterService);
    }

    @Test
    public void shouldFindByHealthId() {
        String healthId = "h100";
        List<PatientAuditLogData> logs = new ArrayList<>();
        PatientAuditLogData log = new PatientAuditLogData();
        Requester approvedBy = new Requester(null, null, new RequesterDetails("a100", "Admin Monika"));
        log.setApprovedBy(approvedBy);
        Map<String, Set<Requester>> requestedBy = new HashMap<>();
        Set<Requester> requesters = new HashSet<>();
        requesters.add(new Requester("f100", "p100"));
        requestedBy.put("fieldX", requesters);
        log.setRequestedBy(requestedBy);
        logs.add(log);

        when(auditRepository.findByHealthId(healthId)).thenReturn(logs);

        assertEquals(logs, auditService.findByHealthId(healthId));
        verify(auditRepository).findByHealthId(healthId);
        verify(requesterService).populateRequesterDetails(approvedBy);
        verify(requesterService).populateRequesterDetails(requesters);
    }

    @Test
    public void shouldSyncWhenFeedAvailable() {
        UUID marker = timeBased();
        when(auditRepository.findLatestMarker()).thenReturn(marker);

        List<PatientUpdateLog> feeds = new ArrayList<>();
        PatientUpdateLog feed = new PatientUpdateLog();
        feed.setHealthId("h100");
        feeds.add(feed);
        when(feedRepository.findPatientsUpdatedSince(marker, UPDATE_LOG_LIMIT)).thenReturn(feeds);

        auditService.sync();

        verify(auditRepository).findLatestMarker();
        verify(feedRepository).findPatientsUpdatedSince(marker, UPDATE_LOG_LIMIT);
        verify(auditRepository).saveOrUpdate(anyListOf(PatientAuditLog.class));
    }

    @Test
    public void shouldNotSyncWhenFeedUnavailable() {
        UUID marker = timeBased();
        when(auditRepository.findLatestMarker()).thenReturn(marker);

        when(feedRepository.findPatientsUpdatedSince(marker, UPDATE_LOG_LIMIT)).thenReturn(null);

        auditService.sync();

        verify(auditRepository).findLatestMarker();
        verify(feedRepository).findPatientsUpdatedSince(marker, UPDATE_LOG_LIMIT);
        verify(auditRepository, never()).saveOrUpdate(anyListOf(PatientAuditLog.class));
    }

    @Test
    public void shouldMapFeedToAuditLog() {
        PatientUpdateLog feed1 = new PatientUpdateLog();
        feed1.setHealthId("h100");
        UUID eventId1 = timeBased();
        feed1.setEventId(eventId1);
        feed1.setChangeSet("xyz1");

        PatientUpdateLog feed2 = new PatientUpdateLog();
        feed2.setHealthId("h200");
        UUID eventId2 = timeBased();
        feed2.setEventId(eventId2);
        feed2.setChangeSet("xyz2");

        PatientUpdateLog feed3 = new PatientUpdateLog();
        feed3.setHealthId("h300");
        UUID eventId3 = timeBased();
        feed3.setEventId(eventId3);
        feed3.setChangeSet("xyz3");

        List<PatientAuditLog> auditLogs = auditService.map(asList(feed1, feed2, feed3));

        assertNotNull(auditLogs);
        assertEquals(3, auditLogs.size());

        assertEquals("h100", auditLogs.get(0).getHealthId());
        assertEquals(eventId1, auditLogs.get(0).getEventId());
        assertEquals("xyz1", auditLogs.get(0).getChangeSet());

        assertEquals("h200", auditLogs.get(1).getHealthId());
        assertEquals(eventId2, auditLogs.get(1).getEventId());
        assertEquals("xyz2", auditLogs.get(1).getChangeSet());

        assertEquals("h300", auditLogs.get(2).getHealthId());
        assertEquals(eventId3, auditLogs.get(2).getEventId());
        assertEquals("xyz3", auditLogs.get(2).getChangeSet());
    }
}