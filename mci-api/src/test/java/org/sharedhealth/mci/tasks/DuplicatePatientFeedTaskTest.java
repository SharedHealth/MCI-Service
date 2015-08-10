package org.sharedhealth.mci.tasks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.deduplication.service.DuplicatePatientFeedService;
import org.sharedhealth.mci.deduplication.task.DuplicatePatientFeedTask;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicatePatientFeedTaskTest {

    @Mock
    private DuplicatePatientFeedService duplicatePatientFeedService;

    @Before
    public void setup() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldProcessDuplicatePatientFeed() throws Exception {
        new DuplicatePatientFeedTask(duplicatePatientFeedService).execute();
        verify(duplicatePatientFeedService).processDuplicatePatients();
    }
}