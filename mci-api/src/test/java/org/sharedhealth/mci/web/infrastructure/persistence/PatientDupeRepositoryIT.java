package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.model.PatientDupe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.asSet;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientDupeRepositoryIT {

    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cassandraOps;

    @Autowired
    private PatientDupeRepository patientDupeRepository;

    @Test
    public void shouldFindAllByCatchment() throws Exception {
        cassandraOps.update(buildDupes());
        List<PatientDupe> dupes = patientDupeRepository.findAllByCatchment(new Catchment("102030"));
        assertTrue(isNotEmpty(dupes));
        assertEquals(6, dupes.size());
    }

    private List<PatientDupe> buildDupes() {
        List<PatientDupe> dupes = new ArrayList<>();
        String catchmentId = "A10B20C30";
        dupes.add(new PatientDupe(catchmentId, "100", "101", asSet("nid"), timeBased()));
        dupes.add(new PatientDupe(catchmentId, "100", "101", asSet("phoneNo"), timeBased()));
        dupes.add(new PatientDupe(catchmentId, "102", "103", asSet("nid"), timeBased()));
        dupes.add(new PatientDupe(catchmentId, "104", "105", asSet("phoneNo"), timeBased()));
        dupes.add(new PatientDupe(catchmentId, "106", "107", asSet("phoneNo"), timeBased()));
        dupes.add(new PatientDupe(catchmentId, "108", "109", asSet("nid"), timeBased()));
        dupes.add(new PatientDupe(catchmentId, "110", "111", asSet("nid"), timeBased()));
        return dupes;
    }
}