package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.model.HealthId;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientHealthIdServiceTest {

    @Mock
    private HealthIdService healthIdService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldReplenishIfNeeded() throws Exception {
        ArrayList<HealthId> healthIds = new ArrayList<>();
        healthIds.add(new HealthId("1213", "MCI", 0));
        when(healthIdService.getNextBlock()).thenReturn(healthIds);
        PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService);
        patientHealthIdService.replenishIfNeeded();
        verify(healthIdService).getNextBlock();
    }

    @Test
    public void shouldGetNextBlockIfNoHIDLeft() throws Exception {
        ArrayList<HealthId> healthIds = new ArrayList<>();
        healthIds.add(new HealthId("1213", "MCI", 0));
        when(healthIdService.getNextBlock()).thenReturn(healthIds);
        PatientHealthIdService patientHealthIdService = new PatientHealthIdService(healthIdService);
        patientHealthIdService.replenishIfNeeded();
        HealthId healthId = patientHealthIdService.getNextHealthId();
        assertEquals("1213", healthId.getHid());
    }

}