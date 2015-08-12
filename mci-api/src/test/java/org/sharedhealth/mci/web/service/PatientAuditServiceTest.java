package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.domain.model.PatientAuditLogData;
import org.sharedhealth.mci.domain.model.Requester;
import org.sharedhealth.mci.domain.model.RequesterDetails;
import org.sharedhealth.mci.domain.repository.PatientAuditRepository;
import org.sharedhealth.mci.domain.repository.PatientFeedRepository;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

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
        auditService = new PatientAuditService(auditRepository, requesterService);
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
}