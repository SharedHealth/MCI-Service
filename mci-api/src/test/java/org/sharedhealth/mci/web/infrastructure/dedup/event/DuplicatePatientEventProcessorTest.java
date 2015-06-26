package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.*;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicatePatientEventProcessorTest {

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
        eventProcessor = new DuplicatePatientEventProcessor() {
            @Override
            public void process(PatientUpdateLogData log, UUID marker) {

            }
        };
        eventProcessor.setRuleEngine(ruleEngine);
        eventProcessor.setMapper(mapper);
        eventProcessor.setPatientRepository(patientRepository);
        eventProcessor.setDuplicatePatientRepository(duplicatePatientRepository);
    }

    @Test
    public void shouldBuildDuplicates() {
        String healthId = "h100";
        List<DuplicatePatientData> duplicates = asList(new DuplicatePatientData());
        when(ruleEngine.apply(healthId)).thenReturn(duplicates);

        eventProcessor.buildDuplicates(healthId);
        verify(ruleEngine).apply(healthId);
        verify(mapper).mapToDuplicatePatientList(duplicates);
    }

    @Test
    public void shouldFindDuplicatesByHealthId1() {
        String healthId = "h100";
        PatientData patient = new PatientData();
        patient.setAddress(new Address("10", "20", "30"));

        when(patientRepository.findByHealthId(healthId)).thenReturn(patient);
        eventProcessor.findDuplicatesByHealthId1(healthId);

        verify(patientRepository).findByHealthId(healthId);
        verify(duplicatePatientRepository).findByCatchmentAndHealthId(patient.getCatchment(), healthId);
    }
}