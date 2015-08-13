package org.sharedhealth.mci.deduplication.event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.deduplication.model.DuplicatePatient;
import org.sharedhealth.mci.deduplication.model.DuplicatePatientData;
import org.sharedhealth.mci.deduplication.model.DuplicatePatientMapper;
import org.sharedhealth.mci.deduplication.repository.DuplicatePatientRepository;
import org.sharedhealth.mci.deduplication.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.domain.model.PatientUpdateLogData;
import org.sharedhealth.mci.domain.repository.PatientRepository;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicatePatientCreateEventProcessorTest {

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
        eventProcessor = new DuplicatePatientCreateEventProcessor();
        eventProcessor.setRuleEngine(ruleEngine);
        eventProcessor.setMapper(mapper);
        eventProcessor.setPatientRepository(patientRepository);
        eventProcessor.setDuplicatePatientRepository(duplicatePatientRepository);
    }

    @Test
    public void shouldProcessCreateEvent() {
        String healthId = "h100";
        UUID marker = randomUUID();
        List<DuplicatePatientData> duplicateData = asList(new DuplicatePatientData());
        when(ruleEngine.apply(healthId)).thenReturn(duplicateData);
        List<DuplicatePatient> duplicates = asList(new DuplicatePatient("A10B20C30", "hid1", "hid2"));
        when(mapper.mapToDuplicatePatientList(duplicateData)).thenReturn(duplicates);

        PatientUpdateLogData log = new PatientUpdateLogData();
        log.setHealthId(healthId);
        eventProcessor.process(log, marker);
        verify(duplicatePatientRepository).create(duplicates, marker);
    }
}