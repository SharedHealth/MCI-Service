package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sharedhealth.mci.domain.model.LocationData;
import org.sharedhealth.mci.domain.model.LocationRepositoryMarker;
import org.sharedhealth.mci.domain.repository.LocationCriteria;
import org.sharedhealth.mci.domain.repository.LocationRepository;
import org.sharedhealth.mci.domain.service.LocationService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
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
        assertEquals("10", locationData1.getCode());
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
        assertEquals("10", locationDataList1.get(0).getCode());
    }

    @Test
    public void shouldSaveLocationDataIfNotExist() throws Exception {

        List<LocationData> locationDataList = new ArrayList<>();
        LocationData locationData = new LocationData();

        locationData.setCode("10");
        locationData.setParent("00");
        locationData.setActive("1");
        locationDataList.add(locationData);

        Mockito.when(locationRepository.saveOrUpdateLocationData(locationDataList)).thenReturn(true);
        assertTrue(locationService.saveOrUpdateLocationData(locationDataList));
    }

    @Test
    public void shouldReturnLRMarkerDataIfExist() throws Exception {

        LocationRepositoryMarker lrMarker = new LocationRepositoryMarker();
        lrMarker.setLastFeedUrl("2015-03-04");
        lrMarker.setType("DIVISION");

        Mockito.when(locationRepository.getLRMarkerData("DIVSION")).thenReturn(lrMarker);

        lrMarker = locationService.getLRMarkerData("DIVSION");

        assertEquals("2015-03-04", lrMarker.getLastFeedUrl());
        assertEquals("DIVISION", lrMarker.getType());

    }

    @Test
    public void shouldReturnNullIfLRMarkerDataNotExist() throws Exception {

        Mockito.when(locationRepository.getLRMarkerData("DIVSION")).thenReturn(null);

        LocationRepositoryMarker lrMarker = locationService.getLRMarkerData("DIVSION");
        assertNull(lrMarker);

    }

    @Test
    public void shouldSaveLRMarkerDataIfNotExist() throws Exception {

        Mockito.when(locationRepository.saveOrUpdateLRMarkerData("DIVISION", "2015-03-04")).thenReturn(true);
        assertTrue(locationService.saveOrUpdateLRMarkerData("DIVISION", "2015-03-04"));
    }
}
