package org.sharedhealth.mci.web.infrastructure.dedup.event;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.web.mapper.PatientUpdateLogData;
import org.sharedhealth.mci.web.model.DuplicatePatient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.buildAddressChangeSet;
import static org.sharedhealth.mci.web.utils.JsonMapper.readValue;

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
        when(duplicatePatientMapper.map(duplicateData)).thenReturn(duplicates);

        PatientUpdateLogData log = new PatientUpdateLogData();
        log.setHealthId(healthId);
        log.setChangeSet(readValue(buildAddressChangeSet(), new TypeReference<Map<String, Map<String, Object>>>() {
        }));
        eventProcessor.process(log, marker);
        verify(duplicatePatientRepository).update(healthId, log.getOldCatchmentFromChangeSet(), duplicates, marker);
    }
}