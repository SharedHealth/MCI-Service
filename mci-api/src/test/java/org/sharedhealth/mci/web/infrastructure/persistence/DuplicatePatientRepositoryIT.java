package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.Requester;
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
import static org.junit.Assert.*;
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

    @Autowired
    private PatientRepository patientRepository;

    @Test
    public void shouldFindAllByCatchment() {
        cassandraOps.update(buildDuplicatePatientsForSearch());
        List<DuplicatePatient> duplicatePatients1 = duplicatePatientRepository.findAllByCatchment(new Catchment("182838"));
        assertTrue(isNotEmpty(duplicatePatients1));
        assertEquals(6, duplicatePatients1.size());

        List<DuplicatePatient> duplicatePatients2 = duplicatePatientRepository.findAllByCatchment(new Catchment("192939"));
        assertTrue(isNotEmpty(duplicatePatients2));
        assertEquals(1, duplicatePatients2.size());
    }

    @Test
    public void shouldIgnoreDuplicates() {
        PatientData patientData1 = new PatientData();
        PatientData patientData2 = new PatientData();
        PatientData patientData3 = new PatientData();
        buildDuplicatePatientsForMerge(patientData1, patientData2, patientData3);

        patientData1.setBloodGroup("X");
        patientData2.setBloodGroup("Y");

        duplicatePatientRepository.processDuplicates(patientData1, patientData2, false);
        assertDuplicatesDeleted(patientData1.getHealthId(), patientData2.getHealthId(), patientData3.getHealthId(), false);

        PatientData patient1 = patientRepository.findByHealthId(patientData1.getHealthId());
        assertEquals("A", patient1.getBloodGroup());

        PatientData patient2 = patientRepository.findByHealthId(patientData2.getHealthId());
        assertEquals("B", patient2.getBloodGroup());
    }

    @Test
    public void shouldMergeDuplicates() {
        PatientData patientData1 = new PatientData();
        PatientData patientData2 = new PatientData();
        PatientData patientData3 = new PatientData();
        buildDuplicatePatientsForMerge(patientData1, patientData2, patientData3);

        patientData1.setBloodGroup("X");
        patientData2.setBloodGroup("Y");

        duplicatePatientRepository.processDuplicates(patientData1, patientData2, true);
        assertDuplicatesDeleted(patientData1.getHealthId(), patientData2.getHealthId(), patientData3.getHealthId(), true);

        PatientData patient1 = patientRepository.findByHealthId(patientData1.getHealthId());
        assertEquals("X", patient1.getBloodGroup());

        PatientData patient2 = patientRepository.findByHealthId(patientData2.getHealthId());
        assertEquals("Y", patient2.getBloodGroup());
    }

    private void assertDuplicatesDeleted(String healthId1, String healthId2, String healthId3, boolean isMerged) {
        String cql1 = select().from(CF_PATIENT_DUPLICATE).where(eq(CATCHMENT_ID, "A10B20C30"))
                .and(eq(HEALTH_ID1, healthId1)).and(eq(HEALTH_ID2, healthId2)).toString();
        assertTrue(isEmpty(cassandraOps.select(cql1, DuplicatePatient.class)));

        String cql2 = select().from(CF_PATIENT_DUPLICATE).where(eq(CATCHMENT_ID, "A11B22C33"))
                .and(eq(HEALTH_ID1, healthId2)).and(eq(HEALTH_ID2, healthId1)).toString();
        assertTrue(isEmpty(cassandraOps.select(cql2, DuplicatePatient.class)));

        String cql3 = select().from(CF_PATIENT_DUPLICATE).where(eq(CATCHMENT_ID, "A10B20C30"))
                .and(eq(HEALTH_ID1, healthId1)).and(eq(HEALTH_ID2, healthId3)).toString();
        assertEquals(isMerged, cassandraOps.select(cql3, DuplicatePatient.class).isEmpty());
    }

    private List<DuplicatePatient> buildDuplicatePatientsForSearch() {
        List<DuplicatePatient> duplicatePatients = new ArrayList<>();
        String catchmentId = "A18B28C38";
        duplicatePatients.add(new DuplicatePatient(catchmentId, "100", "101", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "100", "101", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "102", "103", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "104", "105", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "106", "107", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "108", "109", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId, "110", "111", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient("A19B29C39", "111", "110", asSet("nid"), timeBased()));
        return duplicatePatients;
    }

    private void buildDuplicatePatientsForMerge(PatientData patientData1, PatientData patientData2, PatientData patientData3) {
        Address address1 = new Address("10", "20", "30");
        patientData1.setAddress(address1);
        patientData1.setBloodGroup("A");
        patientData1.setCreatedBy(new Requester("f110"));

        Address address2 = new Address("11", "22", "33");
        patientData2.setAddress(address2);
        patientData2.setBloodGroup("B");
        patientData2.setCreatedBy(new Requester("f111"));

        String healthId1 = patientRepository.create(patientData1).getId();
        String healthId2 = patientRepository.create(patientData2).getId();
        String healthId3 = patientRepository.create(patientData1).getId();
        patientData1.setHealthId(healthId1);
        patientData2.setHealthId(healthId2);
        patientData3.setHealthId(healthId3);
        patientData3.setBloodGroup("A");
        patientData3.setAddress(address1);

        List<DuplicatePatient> duplicatePatients = new ArrayList<>();
        duplicatePatients.add(new DuplicatePatient(patientData1.getCatchment().getId(), healthId1, healthId2, asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(patientData2.getCatchment().getId(), healthId2, healthId1, asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(patientData1.getCatchment().getId(), healthId1, healthId3, asSet("nid"), timeBased()));
        cassandraOps.update(duplicatePatients);
    }

    @Test
    public void shouldFindByCatchmentAndHealthIds() {
        cassandraOps.update(buildDuplicatePatientsForSearch());
        DuplicatePatient duplicatePatient = duplicatePatientRepository.findByCatchmentAndHealthIds(new Catchment("182838"), "102", "103");
        assertNotNull(duplicatePatient);
        assertEquals("102", duplicatePatient.getHealth_id1());
        assertEquals("103", duplicatePatient.getHealth_id2());
    }
}