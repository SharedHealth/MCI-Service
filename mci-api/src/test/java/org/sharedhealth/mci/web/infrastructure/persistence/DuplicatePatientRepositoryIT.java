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

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.*;
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

    @Autowired
    private MarkerRepository markerRepository;

    @Test
    public void shouldFindByCatchment() {
        buildDuplicatePatientsForSearch();
        List<DuplicatePatient> duplicatePatients1 = duplicatePatientRepository.findByCatchment(new Catchment("182838"));
        assertTrue(isNotEmpty(duplicatePatients1));
        assertEquals(6, duplicatePatients1.size());

        List<DuplicatePatient> duplicatePatients2 = duplicatePatientRepository.findByCatchment(new Catchment("192939"));
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

        assertIgnoredList(healthId1, healthId2);
        assertIgnoredList(healthId2, healthId1);
    }

    private void assertIgnoredList(String healthId1, String healthId2) {
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

    private void buildDuplicatePatientsForSearch() {
        List<DuplicatePatient> duplicatePatients = new ArrayList<>();
        String catchmentId1 = "A18B28";
        String catchmentId2 = "A18B28C38";
        duplicatePatients.add(new DuplicatePatient(catchmentId1, "100", "101", asSet("nid", "phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId2, "100", "101", asSet("nid", "phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId1, "102", "103", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId2, "102", "103", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId1, "104", "105", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId2, "104", "105", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId1, "106", "107", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId2, "106", "107", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId1, "108", "109", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId2, "108", "109", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId1, "110", "111", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient(catchmentId2, "110", "111", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient("A19B29", "111", "110", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient("A19B29C39", "111", "110", asSet("nid"), timeBased()));
        duplicatePatientRepository.create(duplicatePatients, randomUUID());
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
        duplicatePatientRepository.create(duplicatePatients, randomUUID());
    }

    @Test
    public void shouldFindByCatchmentAndHealthIds() {
        buildDuplicatePatientsForSearch();
        List<DuplicatePatient> duplicatePatients = duplicatePatientRepository.findByCatchmentAndHealthIds(new Catchment("182838"), "102", "103");
        assertNotNull(duplicatePatients);
        assertEquals(2, duplicatePatients.size());

        assertDuplicate("102", "103", "A18B28", duplicatePatients.get(0));
        assertDuplicate("102", "103", "A18B28C38", duplicatePatients.get(1));
    }

    @Test
    public void shouldFindByCatchmentAndHealthId() {
        buildDuplicatePatientsForSearch();
        List<DuplicatePatient> duplicatePatients = duplicatePatientRepository.findByCatchmentAndHealthId(new Catchment("182838"), "102");
        assertNotNull(duplicatePatients);
        assertEquals(2, duplicatePatients.size());

        assertDuplicate("102", "103", "A18B28", duplicatePatients.get(0));
        assertDuplicate("102", "103", "A18B28C38", duplicatePatients.get(1));
    }

    private void assertDuplicate(String healthId1, String healthId2, String catchmentId, DuplicatePatient duplicatePatient) {
        assertNotNull(duplicatePatient);
        assertEquals(healthId1, duplicatePatient.getHealth_id1());
        assertEquals(healthId2, duplicatePatient.getHealth_id2());
        assertEquals(catchmentId, duplicatePatient.getCatchment_id());
    }

    @Test
    public void shouldCreateDuplicateAndUpdateMarker() {
        Catchment catchment = new Catchment("192939");
        String healthId1 = "111";
        String healthId2 = "110";
        Set<String> reasons = asSet("nid");
        UUID createdAt = timeBased();
        UUID marker = randomUUID();
        createDuplicate(catchment, healthId1, healthId2, reasons, createdAt, marker);

        List<DuplicatePatient> duplicates = duplicatePatientRepository.findByCatchmentAndHealthIds(catchment, healthId1, healthId2);
        assertTrue(isNotEmpty(duplicates));
        assertEquals(1, duplicates.size());

        DuplicatePatient actualDuplicate = duplicates.get(0);
        assertNotNull(actualDuplicate);
        assertEquals(catchment.getId(), actualDuplicate.getCatchment_id());
        assertEquals(healthId1, actualDuplicate.getHealth_id1());
        assertEquals(healthId2, actualDuplicate.getHealth_id2());
        assertEquals(reasons, actualDuplicate.getReasons());
        assertEquals(createdAt, actualDuplicate.getCreated_at());

        assertMarker(marker);
    }

    private void assertMarker(UUID marker) {
        String actualMarker = markerRepository.find(DUPLICATE_PATIENT_MARKER);
        assertNotNull(actualMarker);
        assertEquals(marker.toString(), actualMarker);
    }

    private void createDuplicate(Catchment catchment, String healthId1, String healthId2, Set<String> reasons, UUID createdAt, UUID marker) {
        DuplicatePatient duplicate = new DuplicatePatient(catchment.getId(), healthId1, healthId2, reasons, createdAt);
        duplicatePatientRepository.create(asList(duplicate), marker);
    }

    @Test
    public void shouldRetirePatientAndUpdateMarker() {
        PatientData patient1 = buildPatient("h001", new Address("10", "11", "12"));
        String healthId1 = patientRepository.create(patient1).getId();
        PatientData patient2 = buildPatient("h002", new Address("20", "21", "22"));
        String healthId2 = patientRepository.create(patient2).getId();
        PatientData patient3 = buildPatient("h003", new Address("30", "31", "32"));
        String healthId3 = patientRepository.create(patient3).getId();
        Set<String> reasons = asSet("nid");
        DuplicatePatient duplicate1 = new DuplicatePatient(patient1.getCatchment().getId(), healthId1, healthId2, reasons, timeBased());
        DuplicatePatient duplicate2 = new DuplicatePatient(patient2.getCatchment().getId(), healthId2, healthId1, reasons, timeBased());
        DuplicatePatient duplicate3 = new DuplicatePatient(patient2.getCatchment().getId(), healthId2, healthId3, reasons, timeBased());
        DuplicatePatient duplicate4 = new DuplicatePatient(patient3.getCatchment().getId(), healthId3, healthId2, reasons, timeBased());
        DuplicatePatient duplicate5 = new DuplicatePatient(patient1.getCatchment().getId(), healthId1, healthId3, reasons, timeBased());
        DuplicatePatient duplicate6 = new DuplicatePatient(patient3.getCatchment().getId(), healthId3, healthId1, reasons, timeBased());
        duplicatePatientRepository.create(asList(duplicate1, duplicate2, duplicate3, duplicate4, duplicate5, duplicate6), randomUUID());

        UUID marker = randomUUID();
        duplicatePatientRepository.retire(healthId1, marker);

        List<DuplicatePatient> duplicates = findAllDuplicates();
        assertTrue(isNotEmpty(duplicates));
        assertEquals(2, duplicates.size());
        assertDuplicate(healthId2, healthId3, patient2.getCatchment().getId(), duplicates.get(0));
        assertDuplicate(healthId3, healthId2, patient3.getCatchment().getId(), duplicates.get(1));
        assertMarker(marker);
    }

    @Test
    public void shouldUpdatePatientAndUpdateMarker() {
        PatientData patient1 = buildPatient("h001", new Address("10", "11", "12"));
        String healthId1 = patientRepository.create(patient1).getId();
        PatientData patient2 = buildPatient("h002", new Address("20", "21", "22"));
        String healthId2 = patientRepository.create(patient2).getId();
        PatientData patient3 = buildPatient("h003", new Address("30", "31", "32"));
        String healthId3 = patientRepository.create(patient3).getId();
        PatientData patient4 = buildPatient("h004", new Address("40", "41", "42"));
        String healthId4 = patientRepository.create(patient4).getId();
        Set<String> reasons = asSet("nid");

        duplicatePatientRepository.create(asList(
                        new DuplicatePatient(patient1.getCatchment().getId(), healthId1, healthId2, reasons, timeBased()),
                        new DuplicatePatient(patient2.getCatchment().getId(), healthId2, healthId1, reasons, timeBased())),
                randomUUID());

        cassandraOps.insert(new DuplicatePatientIgnored(healthId1, healthId4, reasons));
        cassandraOps.insert(new DuplicatePatientIgnored(healthId4, healthId1, reasons));

        UUID marker = randomUUID();
        duplicatePatientRepository.update(healthId1, new Catchment("101112"), asList(
                new DuplicatePatient(patient1.getCatchment().getId(), healthId1, healthId3, reasons, timeBased()),
                new DuplicatePatient(patient3.getCatchment().getId(), healthId3, healthId1, reasons, timeBased()),
                new DuplicatePatient(patient1.getCatchment().getId(), healthId1, healthId4, reasons, timeBased()),
                new DuplicatePatient(patient4.getCatchment().getId(), healthId4, healthId1, reasons, timeBased())
        ), marker);

        List<DuplicatePatient> duplicates = findAllDuplicates();
        assertTrue(isNotEmpty(duplicates));
        assertEquals(2, duplicates.size());
        assertDuplicate(healthId1, healthId3, patient1.getCatchment().getId(), duplicates.get(0));
        assertDuplicate(healthId3, healthId1, patient3.getCatchment().getId(), duplicates.get(1));
        assertMarker(marker);
    }

    private PatientData buildPatient(String healthId, Address address) {
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);
        patient.setAddress(address);
        return patient;
    }

    private List<DuplicatePatient> findAllDuplicates() {
        return cassandraOps.select(select().from(CF_PATIENT_DUPLICATE), DuplicatePatient.class);
    }

    @After
    public void tearDown() {
        truncateAllColumnFamilies(cassandraOps);
    }
}