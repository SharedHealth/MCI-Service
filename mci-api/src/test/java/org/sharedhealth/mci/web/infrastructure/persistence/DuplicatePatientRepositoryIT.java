package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.asSet;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class DuplicatePatientRepositoryIT {

    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cassandraOps;

    @Autowired
    private DuplicatePatientRepository duplicatePatientRepository;

    @Before
    public void setUp() throws Exception {
        cassandraOps.update(buildDuplicatePatients());
    }

    @Test
    public void shouldFindAllByCatchment() {
        List<DuplicatePatient> duplicatePatients1 = duplicatePatientRepository.findAllByCatchment(new Catchment("102030"));
        assertTrue(isNotEmpty(duplicatePatients1));
        assertEquals(6, duplicatePatients1.size());

        List<DuplicatePatient> duplicatePatients2 = duplicatePatientRepository.findAllByCatchment(new Catchment("112233"));
        assertTrue(isNotEmpty(duplicatePatients2));
        assertEquals(1, duplicatePatients2.size());
    }

    @Test
    public void shouldIgnoreDuplicates() throws Exception {
        PatientData patient1 = new PatientData();
        patient1.setHealthId("110");
        patient1.setAddress(new Address("10", "20", "30"));
        PatientData patient2 = new PatientData();
        patient2.setHealthId("111");
        patient2.setAddress(new Address("11", "22", "33"));

        duplicatePatientRepository.ignore(patient1, patient2);

        String cql1 = select().from(CF_PATIENT_DUPLICATE).where(eq(CATCHMENT_ID, "A10B20C30"))
                .and(eq(HEALTH_ID1, "110")).and(eq(HEALTH_ID2, "111")).toString();
        assertTrue(isEmpty(cassandraOps.select(cql1, DuplicatePatient.class)));

        String cql2 = select().from(CF_PATIENT_DUPLICATE).where(eq(CATCHMENT_ID, "A11B22C33"))
                .and(eq(HEALTH_ID1, "111")).and(eq(HEALTH_ID2, "110")).toString();
        assertTrue(isEmpty(cassandraOps.select(cql2, DuplicatePatient.class)));
    }

    private List<DuplicatePatient> buildDuplicatePatients() {
        List<DuplicatePatient> duplicatePatients = new ArrayList<>();
        String catchmentId = "A10B20C30";
        duplicatePatients.add(new DuplicatePatient(catchmentId, "100", "101", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "100", "101", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "102", "103", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "104", "105", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "106", "107", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "108", "109", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "110", "111", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient("A11B22C33", "111", "110", asSet("nid"), timeBased()));
        return duplicatePatients;
    }
}