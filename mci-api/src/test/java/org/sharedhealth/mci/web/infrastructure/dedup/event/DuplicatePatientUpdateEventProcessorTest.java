package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.web.model.DuplicatePatient;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicatePatientUpdateEventProcessorTest {

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
        eventProcessor = new DuplicatePatientUpdateEventProcessor();
        eventProcessor.setRuleEngine(ruleEngine);
        eventProcessor.setMapper(mapper);
        eventProcessor.setPatientRepository(patientRepository);
        eventProcessor.setDuplicatePatientRepository(duplicatePatientRepository);
    }

    @Test
    public void shouldProcessRetireEvent() {
        String healthId = "h100";
        UUID marker = randomUUID();
        List<DuplicatePatientData> duplicateData = asList(new DuplicatePatientData());
        when(ruleEngine.apply(healthId)).thenReturn(duplicateData);
        List<DuplicatePatient> duplicates = asList(new DuplicatePatient());
        when(mapper.map(duplicateData)).thenReturn(duplicates);

        eventProcessor.process(healthId, marker);
        verify(duplicatePatientRepository).update(healthId, duplicates, marker);
    }
}