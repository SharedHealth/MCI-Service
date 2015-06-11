package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicatePatientEventProcessorTest {

    @Mock
    private DuplicatePatientRuleEngine ruleEngine;
    @Mock
    private DuplicatePatientMapper mapper;

    private DuplicatePatientEventProcessor eventProcessor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldBuildDuplicates() {
        eventProcessor = new DuplicatePatientCreateEventProcessor(ruleEngine, mapper);
        String healthId = "h100";

        List<DuplicatePatientData> duplicates = asList(new DuplicatePatientData());
        when(ruleEngine.apply(healthId)).thenReturn(duplicates);

        eventProcessor.buildDuplicates(healthId);
        verify(ruleEngine).apply(healthId);
        verify(mapper).map(duplicates);
    }
}