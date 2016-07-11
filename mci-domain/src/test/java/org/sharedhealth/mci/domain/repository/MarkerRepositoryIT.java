package org.sharedhealth.mci.domain.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.model.Marker;
import org.sharedhealth.mci.domain.util.BaseIntegrationTest;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
public class MarkerRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private MarkerRepository markerRepository;

    @Test
    public void shouldFindByType() throws Exception {
        String type = "type_x";

        Marker marker = new Marker();
        marker.setType(type);
        marker.setCreatedAt(TimeUuidUtil.uuidForDate(new Date()));
        marker.setMarker("marker_1");
        cassandraOps.update(marker);

        String value = markerRepository.find(type);
        assertNotNull(value);
        assertEquals("marker_1", value);
    }
}