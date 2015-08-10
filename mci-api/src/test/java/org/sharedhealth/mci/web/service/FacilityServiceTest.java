package org.sharedhealth.mci.web.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.model.Catchment;
import org.sharedhealth.mci.web.infrastructure.persistence.FacilityRepository;
import org.sharedhealth.mci.web.infrastructure.registry.FacilityRegistryClient;
import org.sharedhealth.mci.web.mapper.FacilityResponse;
import org.sharedhealth.mci.web.model.Facility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FacilityRegistryClient facilityRegistryClient;

    @Mock
    private MCIProperties mciProperties;

    private FacilityService facilityService;

    @Before
    public void setUp() {
        initMocks(this);
        facilityService = new FacilityService(facilityRepository, facilityRegistryClient, mciProperties);
    }

    @Test
    public void shouldNotQueryFacilityRegistryServiceIfFacilityFoundInLocalDatabase() {
        Facility facility = new Facility("1", "foo", "bar", "101010", "101010");
        when(facilityRepository.find(facility.getId())).thenReturn(facility);
        facilityService.find(facility.getId());
        verify(facilityRegistryClient, never()).find(facility.getId());
    }

    @Test
    public void shouldQueryFacilityRegistryIfFacilityNotFoundInLocalDatabase() {
        FacilityResponse facility = new FacilityResponse();
        facility.setId("1");
        facility.setName("foo");
        final int ttl = 1000;

        when(facilityRepository.find(facility.getId())).thenReturn(null);
        when(facilityRegistryClient.find(facility.getId())).thenReturn(facility);
        when(mciProperties.getFrCacheTtl()).thenReturn(ttl);

        facilityService.find(facility.getId());

        verify(facilityRepository).find(facility.getId());
        verify(facilityRegistryClient).find(facility.getId());
        verify(facilityRepository).save(facilityService.map(facility), ttl);
    }

    @Test
    public void shouldReturnCatchmentLists() {

        final String catchmentsString = "101010,101020";

        Facility facility = new Facility("1", "foo", "bar", catchmentsString, "101010");
        when(facilityRepository.find(facility.getId())).thenReturn(facility);
        final List<Catchment> catchments = facilityService.find(facility.getId()).getCatchmentsList();

        verify(facilityRegistryClient, never()).find(facility.getId());
        assertEquals(getCatchmentsList(catchmentsString), catchments);
    }

    private List<Catchment> getCatchmentsList(String catchments) {
        List<String> catchmentsStringList = new ArrayList<>();
        if (StringUtils.isNotBlank(catchments)) {
            catchmentsStringList = Arrays.asList(catchments.split(","));
        }

        List<Catchment> catchmentArrayList = new ArrayList<>();
        for (String catchment : catchmentsStringList) {
            catchmentArrayList.add(new Catchment(catchment));
        }
        return catchmentArrayList;
    }
}
