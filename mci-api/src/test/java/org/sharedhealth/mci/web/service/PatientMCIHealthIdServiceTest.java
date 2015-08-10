package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.web.model.MciHealthId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientMCIHealthIdServiceTest {

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
        ArrayList<MciHealthId> MciHealthIds = new ArrayList<>();
        MciHealthIds.add(new MciHealthId("1213"));
        when(healthIdService.getNextBlock()).thenReturn(MciHealthIds);
        PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService, mciProperties);
        patientHealthIdService.replenishIfNeeded();
        verify(healthIdService).getNextBlock();
    }

    @Test
    public void shouldGetNextBlockIfNoHIDLeft() throws Exception {
        ArrayList<MciHealthId> MciHealthIds = new ArrayList<>();
        MciHealthIds.add(new MciHealthId("1213"));
        when(healthIdService.getNextBlock()).thenReturn(MciHealthIds);
        PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService, mciProperties);
        patientHealthIdService.replenishIfNeeded();
        MciHealthId MciHealthId = patientHealthIdService.getNextHealthId();
        assertEquals("1213", MciHealthId.getHid());
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowExceptionIfQueueIsEmpty() throws Exception {
        ArrayList<MciHealthId> MciHealthIds = new ArrayList<>();
        when(healthIdService.getNextBlock()).thenReturn(MciHealthIds);
        PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService, mciProperties);
        patientHealthIdService.replenishIfNeeded();
        patientHealthIdService.getNextHealthId();
    }

    @Test
    public void shouldAllocateNewHealthIdEveryTime() throws Exception {
        ArrayList<MciHealthId> MciHealthIds = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            MciHealthIds.add(new MciHealthId(String.valueOf(1213000 + i)));
        }
        when(healthIdService.getNextBlock()).thenReturn(MciHealthIds);
        final PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService, mciProperties);
        patientHealthIdService.replenishIfNeeded();

        ExecutorService executor = Executors.newFixedThreadPool(100);
        final Set<Future<MciHealthId>> eventualHealthIds = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            Callable<MciHealthId> nextBlock = new Callable<MciHealthId>() {
                @Override
                public MciHealthId call() throws Exception {
                    return patientHealthIdService.getNextHealthId();
                }
            };
            Future<MciHealthId> eventualHealthId = executor.submit(nextBlock);
            eventualHealthIds.add(eventualHealthId);
        }
        Set<MciHealthId> uniqueMciHealthIds = new HashSet<>();

        for (Future<MciHealthId> eventualHealthId : eventualHealthIds) {
            uniqueMciHealthIds.add(eventualHealthId.get());
        }
        assertEquals(10000, uniqueMciHealthIds.size());
    }

    @Test
    public void shouldPutBackHidToHidBlock() throws Exception {
        ArrayList<MciHealthId> MciHealthIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            MciHealthIds.add(new MciHealthId(String.valueOf(1213000 + i)));
        }
        when(healthIdService.getNextBlock()).thenReturn(MciHealthIds);
        final PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService, mciProperties);
        patientHealthIdService.replenishIfNeeded();
        int before = patientHealthIdService.getHealthIdBlockSize();
        MciHealthId nextMciHealthId = patientHealthIdService.getNextHealthId();
        patientHealthIdService.putBackHealthId(nextMciHealthId);
        int after = patientHealthIdService.getHealthIdBlockSize();
        assertEquals(before, after);
    }

    @Test
    public void shouldMarkHidAsUsed() throws Exception {
        ArrayList<MciHealthId> MciHealthIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            MciHealthIds.add(new MciHealthId(String.valueOf(1213000 + i)));
        }
        when(healthIdService.getNextBlock()).thenReturn(MciHealthIds);
        doNothing().when(healthIdService).markUsed(any(MciHealthId.class));
        final PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService, mciProperties);
        patientHealthIdService.replenishIfNeeded();
        int before = patientHealthIdService.getHealthIdBlockSize();
        MciHealthId nextMciHealthId = patientHealthIdService.getNextHealthId();
        patientHealthIdService.markUsed(nextMciHealthId);
        int after = patientHealthIdService.getHealthIdBlockSize();
        assertEquals(before, after + 1);
        verify(healthIdService, times(1)).markUsed(any(MciHealthId.class));
    }
}