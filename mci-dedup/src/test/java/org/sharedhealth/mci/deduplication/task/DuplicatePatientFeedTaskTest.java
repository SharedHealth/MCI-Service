package org.sharedhealth.mci.deduplication.task;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.deduplication.service.DuplicatePatientFeedService;
import org.sharedhealth.mci.domain.config.MCIProperties;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicatePatientFeedTaskTest {

    @Mock
    private DuplicatePatientFeedService duplicatePatientFeedService;
    @Mock
    private MCIProperties mciProperties;

    @Before
    public void setup() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldProcessDuplicatePatientFeed() throws Exception {
        when(mciProperties.getIsMCIMasterNode()).thenReturn(true);
        new DuplicatePatientFeedTask(duplicatePatientFeedService, mciProperties).execute();
        verify(duplicatePatientFeedService).processDuplicatePatients();
    }
}
