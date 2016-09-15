package org.sharedhealth.mci.domain.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.model.Location;
import org.sharedhealth.mci.domain.model.LocationData;
import org.sharedhealth.mci.domain.util.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
public class LocationRepositoryIT extends BaseIntegrationTest {
    @Autowired
    private LocationRepository locationRepository;
    private List<Location> locations;

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        locations = new ArrayList<>();

        Location location1 = new Location();
        location1.setActive("1");
        location1.setName("New Division");
        location1.setCode("90");
        location1.setParent("00");

        Location location2 = new Location();
        location2.setActive("1");
        location2.setName("New District");
        location2.setCode("90");
        location2.setParent("30");

        locations.add(location1);
        locations.add(location2);
        truncateLocationCFs();
    }

    @Test
    public void shouldFindLocationByGeoCode() throws Exception {
        cassandraOps.insert(locations);
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
        cassandraOps.insert(locations);
        LocationCriteria locationCriteria = new LocationCriteria();
        locationCriteria.setParent("00");

        List<LocationData> locationDataList = locationRepository.findLocationsByParent(locationCriteria);

        assertEquals(1, locationDataList.size());
        assertEquals("90", locationDataList.get(0).getCode());
    }

    private void truncateLocationCFs() {
        cassandraOps.execute("truncate locations");
        cassandraOps.execute("truncate lr_markers");
    }
}