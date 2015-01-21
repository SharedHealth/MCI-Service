package org.sharedhealth.mci.web.service;

import java.util.concurrent.ExecutionException;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sharedhealth.mci.web.infrastructure.fr.FacilityRegistryWrapper;
import org.sharedhealth.mci.web.infrastructure.persistence.FacilityRepository;
import org.sharedhealth.mci.web.model.Facility;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.initMocks;

public class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FacilityRegistryWrapper facilityRegistryWrapper;

    private FacilityService facilityService;

    @Before
    public void setUp() {
        initMocks(this);
        facilityService = new FacilityService(facilityRepository, facilityRegistryWrapper);
    }

    @Test
    public void shouldNotQueryFacilityRegistryWrapperIfFacilityFoundInLocalDatabase() throws ExecutionException,
            InterruptedException {
        Facility facility = new Facility("1", "foo", "bar", "101010", "101010");
        Mockito.when(facilityRepository.find(facility.getId())).thenReturn(facility);
        facilityService.ensurePresent(facility.getId());
        Mockito.verify(facilityRegistryWrapper, never()).find(facility.getId());
    }

    @Test
    public void shouldQueryFacilityRegistryWrapperIfFacilityNotFoundInLocalDatabase() throws ExecutionException,
            InterruptedException {
        Facility facility = new Facility("1", "foo", "bar", "101010", "101010");

        Mockito.when(facilityRepository.find(facility.getId())).thenReturn(facility);
        Mockito.when(facilityRegistryWrapper.find(facility.getId())).thenReturn(facility);
        assertNotNull(facilityService.ensurePresent(facility.getId()));

    }
}
