package org.sharedhealth.mci.web.infrastructure.persistence;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.LocationCriteria;
import org.sharedhealth.mci.web.mapper.LocationData;
import org.sharedhealth.mci.web.model.LRMarker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class LocationRepositoryIT {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cqlTemplate;

    @Autowired
    private LocationRepository locationRepository;

    List<LocationData> locationDataList;


    @Before
    public void setup() throws ExecutionException, InterruptedException {
        cqlTemplate.execute("truncate lr_markers");
        cqlTemplate.execute("delete from locations where code = '90' and parent='00'");
        cqlTemplate.execute("delete from locations where code = '90' and parent='30'");

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
    }

    @Test
    public void shouldFindLocationByGeoCode() throws Exception {

        locationRepository.saveOrUpdateLocationData(locationDataList);

        LocationData locationData = locationRepository.findByGeoCode("90");

        Assert.assertEquals("90", locationData.getCode());
        Assert.assertEquals("00", locationData.getParent());
        Assert.assertEquals("New Division", locationData.getName());

        LocationData locationData1 = locationRepository.findByGeoCode("3090");

        Assert.assertEquals("90", locationData1.getCode());
        Assert.assertEquals("30", locationData1.getParent());
        Assert.assertEquals("New District", locationData1.getName());

    }

    @Test
    public void shouldFindByLocationByParent() throws Exception {

        locationRepository.saveOrUpdateLocationData(locationDataList);
        LocationCriteria locationCriteria = new LocationCriteria();
        locationCriteria.setParent("00");
        List<LocationData> locationDataList1 = locationRepository.findLocationsByParent(locationCriteria);

        Assert.assertEquals(8, locationDataList1.size());
        Assert.assertEquals("90", locationDataList1.get(7).getCode());

    }

    @Test
    public void shouldFindAndUpdateLRMarkerIfExist() throws Exception {
        locationRepository.saveOrUpdateLRMarkerData("DIVISION", "2015-02-08");
        locationRepository.saveOrUpdateLRMarkerData("DISTRICT", "2015-02-08");

        LRMarker lrMarker = locationRepository.getLRMarkerData("DIVISION");
        Assert.assertEquals("2015-02-08", lrMarker.getLastSync());
        LRMarker lrMarker1 = locationRepository.getLRMarkerData("UPAZILA");
        Assert.assertNull(lrMarker1);

        locationRepository.saveOrUpdateLRMarkerData("DIVISION", "2015-02-09");
        LRMarker lrMarker2 = locationRepository.getLRMarkerData("DIVISION");
        Assert.assertEquals("2015-02-09", lrMarker2.getLastSync());

    }

    @After
    public void teardown() {
        cqlTemplate.execute("truncate lr_markers");
        cqlTemplate.execute("delete from locations where code = '90' and parent='00'");
        cqlTemplate.execute("delete from locations where code = '90' and parent='30'");
    }

}