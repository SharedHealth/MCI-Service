package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.GeneratedHidRangeRepository;
import org.sharedhealth.mci.web.model.GeneratedHidRange;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class GeneratedHidRangeServiceTest {
    private GeneratedHidRangeService hidRangeService;

    @Mock
    private GeneratedHidRangeRepository generatedHidRangeRepository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        hidRangeService = new GeneratedHidRangeService(generatedHidRangeRepository);
    }

    @Test
    public void shouldAskRepositoryToSaveGivenHIDRange() throws Exception {
        GeneratedHidRange hidRange = new GeneratedHidRange(91L, 9100L, 9165L, "MCI", null);
        when(generatedHidRangeRepository.saveGeneratedHidRange(hidRange)).thenReturn(null);

        hidRangeService.saveGeneratedHidRange(hidRange);

        verify(generatedHidRangeRepository, times(1)).saveGeneratedHidRange(hidRange);
    }

    @Test
    public void shouldAskRepositoryToRetrieveRangeForGivenBlock() throws Exception {
        long blockBiginsAt = 91L;
        when(generatedHidRangeRepository.getPreGeneratedHidRanges(blockBiginsAt)).thenReturn(null);
        
        hidRangeService.getPreGeneratedHidRanges(blockBiginsAt);
        verify(generatedHidRangeRepository, times(1)).getPreGeneratedHidRanges(blockBiginsAt);
    }
}