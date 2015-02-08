package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sharedhealth.mci.web.infrastructure.persistence.LocationRepository;
import org.sharedhealth.mci.web.mapper.LocationCriteria;
import org.sharedhealth.mci.web.mapper.LocationData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

public class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    private LocationService locationService;

    @Before
    public void setUp() {
        initMocks(this);
        locationService = new LocationService(locationRepository);
    }

    @Test
    public void shouldFindLocationIfGeoCodeExist() throws ExecutionException,
            InterruptedException {
        LocationData locationData = new LocationData();
        locationData.setCode("10");
        locationData.setParent("00");
        locationData.setActive("1");
        Mockito.when(locationRepository.findByGeoCode("10")).thenReturn(locationData);
        LocationData locationData1 = locationService.findByGeoCode("10");
        assertNotNull(locationData1);
    }

    @Test
    public void shouldFindLocationByParent() throws ExecutionException,
            InterruptedException {

        List<LocationData> locationDataList = new ArrayList<>();
        LocationData locationData = new LocationData();

        locationData.setCode("10");
        locationData.setParent("00");
        locationData.setActive("1");
        locationDataList.add(locationData);

        LocationCriteria locationCriteria = new LocationCriteria();
        locationCriteria.setParent("00");
        Mockito.when(locationRepository.findLocationsByParent(locationCriteria)).thenReturn(locationDataList);

        List<LocationData> locationDataList1 = locationService.findLocationsByParent(locationCriteria);
        assertNotNull(locationDataList1);
    }
}
