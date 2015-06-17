package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.web.mapper.PatientUpdateLogData;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicatePatientRetireEventProcessorTest {

    @Mock
    private DuplicatePatientRuleEngine ruleEngine;
    @Mock
    private DuplicatePatientMapper mapper;
    @Mock
    private DuplicatePatientRepository duplicatePatientRepository;
    @Mock
    private PatientRepository patientRepository;

    private DuplicatePatientEventProcessor eventProcessor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        eventProcessor = new DuplicatePatientRetireEventProcessor();
        eventProcessor.setRuleEngine(ruleEngine);
        eventProcessor.setMapper(mapper);
        eventProcessor.setPatientRepository(patientRepository);
        eventProcessor.setDuplicatePatientRepository(duplicatePatientRepository);
    }

    @Test
    public void shouldProcessRetireEvent() {
        String healthId = "h100";
        UUID marker = randomUUID();
        PatientUpdateLogData log = new PatientUpdateLogData();
        log.setHealthId(healthId);
        eventProcessor.process(log, marker);
        verify(duplicatePatientRepository).retire(healthId, marker);
    }
}