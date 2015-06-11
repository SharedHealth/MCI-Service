package org.sharedhealth.mci.web.infrastructure.dedup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicatePatientEventProcessorTest {

    @Mock
    private DuplicatePatientRuleEngine ruleEngine;

    private DuplicatePatientEventProcessor eventProcessor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void testFindDuplicates() {
        eventProcessor = new DuplicatePatientCreateEventProcessor(ruleEngine);
        String healthId = "h100";
        eventProcessor.findDuplicates(healthId);
        verify(ruleEngine).apply(healthId);
    }
}