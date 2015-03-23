package org.sharedhealth.mci.web.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.exception.FacilityNotFoundException;
import org.sharedhealth.mci.web.infrastructure.fr.FacilityRegistryWrapper;
import org.sharedhealth.mci.web.infrastructure.persistence.FacilityRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.model.Facility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.initMocks;

public class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FacilityRegistryWrapper facilityRegistryWrapper;

    @Mock
    private MCIProperties mciProperties;

    private FacilityService facilityService;

    @Before
    public void setUp() {
        initMocks(this);
        facilityService = new FacilityService(facilityRepository, facilityRegistryWrapper, mciProperties);
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
        final int ttl = 1000;

        Mockito.when(facilityRepository.find(facility.getId())).thenReturn(null);
        Mockito.when(facilityRegistryWrapper.find(facility.getId())).thenReturn(facility);
        Mockito.when(mciProperties.getFrCacheTtl()).thenReturn(ttl);
        assertNotNull(facilityService.ensurePresent(facility.getId()));
        Mockito.verify(facilityRepository).find(facility.getId());
        Mockito.verify(facilityRepository).save(facility, ttl);
        Mockito.verify(facilityRegistryWrapper).find(facility.getId());
    }

    @Test
    public void shouldReturnCatchmentLists() throws ExecutionException,
            InterruptedException {

        final String catchmentsString = "101010,101020";

        Facility facility = new Facility("1", "foo", "bar", catchmentsString, "101010");
        Mockito.when(facilityRepository.find(facility.getId())).thenReturn(facility);
        final List<Catchment> catchments = facilityService.getCatchmentAreasByFacility(facility.getId());

        Mockito.verify(facilityRegistryWrapper, never()).find(facility.getId());
        assertEquals(getCatchmentsList(catchmentsString), catchments);
    }

    @Test(expected = FacilityNotFoundException.class)
    public void shouldThroughFacilityNotFoundExceptionIfFacilityNotFound() throws ExecutionException,
            InterruptedException {

        final String facilityId = "1";
        Mockito.when(facilityRepository.find(facilityId)).thenReturn(null);
        Mockito.when(facilityRegistryWrapper.find(facilityId)).thenReturn(null);
        assertNotNull(facilityService.getCatchmentAreasByFacility(facilityId));
        Mockito.verify(facilityRepository).find(facilityId);
        Mockito.verify(facilityRegistryWrapper).find(facilityId);
    }

    private List<Catchment> getCatchmentsList(String catchments) {

        List<String> catchmentsStringList = new ArrayList<>();

        if(StringUtils.isNotBlank(catchments)) {
            catchmentsStringList = Arrays.asList(catchments.split(","));
        }

        List<Catchment> catchmentArrayList = new ArrayList<>();

        for (String catchment : catchmentsStringList) {
            catchmentArrayList.add(new Catchment(catchment));
        }

        return catchmentArrayList;
    }
}
