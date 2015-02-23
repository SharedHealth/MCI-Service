package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientAuditRepository;
import org.sharedhealth.mci.web.mapper.PatientAuditLogData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientAuditServiceTest {

    @Mock
    private PatientAuditRepository auditRepository;

    private PatientAuditService auditService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        auditService = new PatientAuditService(auditRepository);
    }

    @Test
    public void shouldFindByHealthId() {
        String healthId = "h100";
        List<PatientAuditLogData> logs = new ArrayList<>();
        when(auditRepository.findByHealthId(healthId)).thenReturn(logs);

        assertEquals(logs, auditService.findByHealthId(healthId));
        verify(auditRepository).findByHealthId(healthId);
    }
}