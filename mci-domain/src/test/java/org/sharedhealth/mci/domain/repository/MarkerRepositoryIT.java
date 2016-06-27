package org.sharedhealth.mci.domain.repository;

import org.junit.After;
import org.junit.Test;
import org.sharedhealth.mci.domain.model.Marker;
import org.sharedhealth.mci.domain.util.BaseRepositoryIT;
import org.springframework.beans.factory.annotation.Autowired;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.mci.domain.util.TestUtil.truncateAllColumnFamilies;

public class MarkerRepositoryIT extends BaseRepositoryIT {

    @Autowired
    private MarkerRepository markerRepository;

    @Test
    public void shouldFindByType() throws Exception {
        String type = "type_x";

        Marker marker = new Marker();
        marker.setType(type);
        marker.setCreatedAt(timeBased());
        marker.setMarker("marker_1");
        cassandraOps.update(marker);

        String value = markerRepository.find(type);
        assertNotNull(value);
        assertEquals("marker_1", value);
    }

    @After
    public void tearDown() {
        truncateAllColumnFamilies(cassandraOps);
    }
}