package org.sharedhealth.mci.web.infrastructure.persistence;

import org.apache.commons.collections.CollectionUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.Requester;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.sharedhealth.mci.web.model.DuplicatePatientIgnored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.asSet;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.truncateAllColumnFamilies;

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
        patientData1.setHealthId(String.valueOf(new Date().getTime() + 1));
        PatientData patientData2 = new PatientData();
        patientData2.setHealthId(String.valueOf(new Date().getTime() + 2));
        PatientData patientData3 = new PatientData();
        patientData3.setHealthId(String.valueOf(new Date().getTime() + 3));
        buildDuplicatePatientsForMerge(patientData1, patientData2, patientData3);
        String healthId1 = patientData1.getHealthId();
        String healthId2 = patientData2.getHealthId();
        patientData1.setBloodGroup("X");
        patientData2.setBloodGroup("Y");

        duplicatePatientRepository.processDuplicates(patientData1, patientData2, false);
        assertDuplicatesDeleted(healthId1, healthId2, patientData3.getHealthId(), false);

        PatientData patient1 = patientRepository.findByHealthId(healthId1);
        assertEquals("A", patient1.getBloodGroup());

        PatientData patient2 = patientRepository.findByHealthId(healthId2);
        assertEquals("B", patient2.getBloodGroup());

        String cql = select().from(CF_PATIENT_DUPLICATE_IGNORED).where(eq(HEALTH_ID1, healthId1))
                .and(eq(HEALTH_ID2, healthId2)).toString();
        List<DuplicatePatientIgnored> ignoredList = cassandraOps.select(cql, DuplicatePatientIgnored.class);
        assertNotNull(ignoredList);
        assertEquals(1, ignoredList.size());
        DuplicatePatientIgnored ignored = ignoredList.get(0);
        assertNotNull(ignored);
        assertEquals(healthId1, ignored.getHealth_id1());
        assertEquals(healthId2, ignored.getHealth_id2());
        assertTrue(CollectionUtils.isNotEmpty(ignored.getReasons()));
    }

    @Test
    public void shouldMergeDuplicates() {
        PatientData patientData1 = new PatientData();
        patientData1.setHealthId(String.valueOf(new Date().getTime() + 1));
        PatientData patientData2 = new PatientData();
        patientData2.setHealthId(String.valueOf(new Date().getTime() + 2));
        PatientData patientData3 = new PatientData();
        patientData3.setHealthId(String.valueOf(new Date().getTime() + 3));
        buildDuplicatePatientsForMerge(patientData1, patientData2, patientData3);

        patientData1.setBloodGroup("X");
        patientData2.setBloodGroup("A");

        duplicatePatientRepository.processDuplicates(patientData1, patientData2, true);
        assertDuplicatesDeleted(patientData1.getHealthId(), patientData2.getHealthId(), patientData3.getHealthId(), true);

        PatientData patient1 = patientRepository.findByHealthId(patientData1.getHealthId());
        assertEquals("X", patient1.getBloodGroup());

        PatientData patient2 = patientRepository.findByHealthId(patientData2.getHealthId());
        assertEquals("A", patient2.getBloodGroup());

        String cql = select().from(CF_PATIENT_DUPLICATE_IGNORED).where(eq(HEALTH_ID1, patientData1.getHealthId()))
                .and(eq(HEALTH_ID2, patientData2.getHealthId())).toString();
        assertTrue(cassandraOps.select(cql, DuplicatePatientIgnored.class).isEmpty());
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
        List<DuplicatePatient> duplicatePatients = duplicatePatientRepository.findByCatchmentAndHealthIds(new Catchment("182838"), "102", "103");
        assertNotNull(duplicatePatients);
        assertEquals(1, duplicatePatients.size());
        DuplicatePatient duplicatePatient = duplicatePatients.get(0);
        assertNotNull(duplicatePatient);
        assertEquals("102", duplicatePatient.getHealth_id1());
        assertEquals("103", duplicatePatient.getHealth_id2());
    }

    @After
    public void tearDown() {
        truncateAllColumnFamilies(cassandraOps);
    }
}