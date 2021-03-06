package org.sharedhealth.mci.deduplication.event;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.deduplication.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.deduplication.model.DuplicatePatient;
import org.sharedhealth.mci.deduplication.model.DuplicatePatientData;
import org.sharedhealth.mci.deduplication.repository.DuplicatePatientRepository;
import org.sharedhealth.mci.deduplication.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.domain.model.PatientUpdateLogData;
import org.sharedhealth.mci.domain.repository.PatientRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.util.JsonMapper.readValue;
import static org.sharedhealth.mci.domain.util.TestUtil.buildAddressChangeSet;

public class DuplicatePatientUpdateEventProcessorTest {

    @Mock
    private DuplicatePatientRuleEngine ruleEngine;
    @Mock
    private DuplicatePatientMapper duplicatePatientMapper;
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
        eventProcessor.setMapper(duplicatePatientMapper);
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
        when(duplicatePatientMapper.mapToDuplicatePatientList(duplicateData)).thenReturn(duplicates);

        PatientUpdateLogData log = new PatientUpdateLogData();
        log.setHealthId(healthId);
        log.setChangeSet(readValue(buildAddressChangeSet(), new TypeReference<Map<String, Map<String, Object>>>() {
        }));
        eventProcessor.process(log, marker);
        verify(duplicatePatientRepository).update(healthId, log.getOldCatchmentFromChangeSet(), duplicates, marker);
    }
}