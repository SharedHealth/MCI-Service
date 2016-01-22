package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.GeneratedHidBlockRepository;
import org.sharedhealth.mci.web.model.GeneratedHIDBlock;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class GeneratedHIDBlockServiceTest {
    private GeneratedHidBlockService hidBlockService;

    @Mock
    private GeneratedHidBlockRepository generatedHidBlockRepository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        hidBlockService = new GeneratedHidBlockService(generatedHidBlockRepository);
    }

    @Test
    public void shouldAskRepositoryToSaveGivenHIDBlock() throws Exception {
        GeneratedHIDBlock hidBlock = new GeneratedHIDBlock(91L, "MCI", 9100L, 9165L, 20L, null, timeBased());
        when(generatedHidBlockRepository.saveGeneratedHidBlock(hidBlock)).thenReturn(null);

        hidBlockService.saveGeneratedHidBlock(hidBlock);

        verify(generatedHidBlockRepository, times(1)).saveGeneratedHidBlock(hidBlock);
    }

    @Test
    public void shouldAskRepositoryToRetrieveBlockForSeries() throws Exception {
        long seriesNo = 91L;
        when(generatedHidBlockRepository.getPreGeneratedHidBlocks(seriesNo)).thenReturn(null);
        
        hidBlockService.getPreGeneratedHidBlocks(seriesNo);
        verify(generatedHidBlockRepository, times(1)).getPreGeneratedHidBlocks(seriesNo);
    }

    @Test
    public void shouldIdentifyStartForAGivenSeries() throws Exception {

    }
}