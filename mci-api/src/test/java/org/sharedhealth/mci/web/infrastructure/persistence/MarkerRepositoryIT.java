package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.model.Marker;
import org.sharedhealth.mci.domain.repository.MarkerRepository;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.truncateAllColumnFamilies;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class MarkerRepositoryIT {

    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cassandraOps;

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