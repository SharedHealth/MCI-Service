package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.model.HealthId;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientHealthIdServiceTest {

    @Mock
    private HealthIdService healthIdService;
    @Mock
    MCIProperties mciProperties;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(mciProperties.getHealthIdBlockSizeThreshold()).thenReturn(1);
    }

    @Test
    public void shouldReplenishIfNeeded() throws Exception {
        ArrayList<HealthId> healthIds = new ArrayList<>();
        healthIds.add(new HealthId("1213", "MCI", 0));
        when(healthIdService.getNextBlock()).thenReturn(healthIds);
        PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService, mciProperties);
        patientHealthIdService.replenishIfNeeded();
        verify(healthIdService).getNextBlock();
    }

    @Test
    public void shouldGetNextBlockIfNoHIDLeft() throws Exception {
        ArrayList<HealthId> healthIds = new ArrayList<>();
        healthIds.add(new HealthId("1213", "MCI", 0));
        when(healthIdService.getNextBlock()).thenReturn(healthIds);
        PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService, mciProperties);
        patientHealthIdService.replenishIfNeeded();
        HealthId healthId = patientHealthIdService.getNextHealthId();
        assertEquals("1213", healthId.getHid());
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowExceptionIfQueueIsEmpty() throws Exception {
        ArrayList<HealthId> healthIds = new ArrayList<>();
        when(healthIdService.getNextBlock()).thenReturn(healthIds);
        PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService, mciProperties);
        patientHealthIdService.replenishIfNeeded();
        patientHealthIdService.getNextHealthId();
    }

    @Test
    public void shouldAllocateNewHealthIdEveryTime() throws Exception {
        ArrayList<HealthId> healthIds = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            healthIds.add(new HealthId(String.valueOf(1213000 + i), "MCI", 0));
        }
        when(healthIdService.getNextBlock()).thenReturn(healthIds);
        final PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService, mciProperties);
        patientHealthIdService.replenishIfNeeded();

        ExecutorService executor = Executors.newFixedThreadPool(100);
        final Set<Future<HealthId>> eventualHealthIds = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            Callable<HealthId> nextBlock = new Callable<HealthId>() {
                @Override
                public HealthId call() throws Exception {
                    return patientHealthIdService.getNextHealthId();
                }
            };
            Future<HealthId> eventualHealthId = executor.submit(nextBlock);
            eventualHealthIds.add(eventualHealthId);
        }
        Set<HealthId> uniqueHealthIds = new HashSet<>();

        for (Future<HealthId> eventualHealthId : eventualHealthIds) {
            uniqueHealthIds.add(eventualHealthId.get());
        }
        assertEquals(10000, uniqueHealthIds.size());
    }

}