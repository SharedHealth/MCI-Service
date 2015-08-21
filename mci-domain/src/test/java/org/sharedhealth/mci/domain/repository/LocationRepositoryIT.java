package org.sharedhealth.mci.domain.repository;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sharedhealth.mci.domain.model.LocationData;
import org.sharedhealth.mci.domain.model.LocationRepositoryMarker;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;


public class LocationRepositoryIT extends BaseRepositoryIT {

    @Autowired
    private LocationRepository locationRepository;

    List<LocationData> locationDataList;


    @Before
    public void setup() throws ExecutionException, InterruptedException {

        locationDataList = new ArrayList<>();

        LocationData locationData = new LocationData();
        locationData.setActive("1");
        locationData.setName("New Division");
        locationData.setCode("90");
        locationData.setParent("00");

        LocationData locationData1 = new LocationData();
        locationData1.setActive("1");
        locationData1.setName("New District");
        locationData1.setCode("3090");
        locationData1.setParent("30");

        locationDataList.add(locationData);
        locationDataList.add(locationData1);

        truncateLocationCFs();
    }

    @Test
    public void shouldFindLocationByGeoCode() throws Exception {

        locationRepository.saveOrUpdateLocationData(locationDataList);

        LocationData locationData = locationRepository.findByGeoCode("90");

        assertEquals("90", locationData.getCode());
        assertEquals("00", locationData.getParent());
        assertEquals("New Division", locationData.getName());

        LocationData locationData1 = locationRepository.findByGeoCode("3090");

        assertEquals("90", locationData1.getCode());
        assertEquals("30", locationData1.getParent());
        assertEquals("New District", locationData1.getName());

    }

    @Test
    public void shouldFindByLocationByParent() throws Exception {

        locationRepository.saveOrUpdateLocationData(locationDataList);
        LocationCriteria locationCriteria = new LocationCriteria();
        locationCriteria.setParent("00");
        List<LocationData> locationDataList = locationRepository.findLocationsByParent(locationCriteria);

        assertEquals(1, locationDataList.size());
        assertEquals("90", locationDataList.get(0).getCode());

    }

    @Test
    public void shouldFindAndUpdateLRMarkerIfExist() throws Exception {
        locationRepository.saveOrUpdateLRMarkerData("DIVISION", "2015-02-08");
        locationRepository.saveOrUpdateLRMarkerData("DISTRICT", "2015-02-08");

        LocationRepositoryMarker lrMarker = locationRepository.getLRMarkerData("DIVISION");
        assertEquals("2015-02-08", lrMarker.getLastFeedUrl());
        LocationRepositoryMarker lrMarker1 = locationRepository.getLRMarkerData("UPAZILA");
        Assert.assertNull(lrMarker1);

        locationRepository.saveOrUpdateLRMarkerData("DIVISION", "2015-02-09");
        LocationRepositoryMarker lrMarker2 = locationRepository.getLRMarkerData("DIVISION");
        assertEquals("2015-02-09", lrMarker2.getLastFeedUrl());

    }

    @Test
    public void shouldSaveLRDataIFNotExist() throws Exception {

        locationDataList = new ArrayList<>();

        LocationData locationData = new LocationData();
        locationData.setActive("1");
        locationData.setName("New Division");
        locationData.setCode("66");
        locationData.setParent("00");
        locationDataList.add(locationData);

        locationRepository.saveOrUpdateLocationData(locationDataList);

        LocationData locationData1 = locationRepository.findByGeoCode("66");

        assertEquals("66", locationData1.getCode());
        assertEquals("00", locationData1.getParent());
        assertEquals("New Division", locationData1.getName());

    }

    @Test
    public void shouldUpdateLRDataIFExist() throws Exception {

        locationDataList = new ArrayList<>();

        LocationData locationData = new LocationData();
        locationData.setActive("1");
        locationData.setName("New Division");
        locationData.setCode("33");
        locationData.setParent("00");
        locationDataList.add(locationData);

        locationRepository.saveOrUpdateLocationData(locationDataList);

        locationData = locationRepository.findByGeoCode("33");

        assertEquals("33", locationData.getCode());
        assertEquals("00", locationData.getParent());
        assertEquals("New Division", locationData.getName());

        locationDataList = new ArrayList<>();

        locationData = new LocationData();
        locationData.setActive("1");
        locationData.setName("Division Updated");
        locationData.setCode("33");
        locationData.setParent("00");
        locationDataList.add(locationData);

        locationRepository.saveOrUpdateLocationData(locationDataList);

        locationData = locationRepository.findByGeoCode("33");

        assertEquals("33", locationData.getCode());
        assertEquals("00", locationData.getParent());
        assertEquals("Division Updated", locationData.getName());


    }

    private void truncateLocationCFs() {
        cassandraOps.execute("truncate locations");
        cassandraOps.execute("truncate lr_markers");
    }
}