package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.querybuilder.Batch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.exception.PatientNotFoundException;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.util.BaseIntegrationTest;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.domain.constant.MCIConstants.*;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;
import static org.sharedhealth.mci.domain.util.TimeUuidUtil.getTimeFromUUID;

@RunWith(SpringJUnit4ClassRunner.class)
public class PatientRepositoryIT extends BaseIntegrationTest {
    private static final String FACILITY = "Bahmni";

    public String surname = "Tiger";
    private String phoneNumber = "999900000";
    private String divisionId = "10";
    private String districtId = "04";
    private String upazilaId = "09";
    private String nationalId = "1234567890123";
    private String birthRegistrationNumber = "12345678901234567";
    private String uid = "12345678901";
    private String givenName = "Scott";
    private String householdCode = "12345";


    @Autowired
    private PatientRepository patientRepository;

    @Test
    public void shouldCreateActivePatients() throws Exception {
        PatientData data = buildPatient();
        MCIResponse mciResponseForCreate = patientRepository.create(data);
        assertEquals(201, mciResponseForCreate.getHttpStatus());

        String healthId = mciResponseForCreate.getId();
        PatientData savedPatient = patientRepository.findByHealthId(healthId);
        assertPatient(savedPatient, data);
        assertTrue(savedPatient.isActive());
    }

    @Test(expected = PatientNotFoundException.class)
    public void shouldThrowException_IfPatientDoesNotExistForGivenHealthId() {
        patientRepository.findByHealthId(UUID.randomUUID().toString());
    }

    @Test
    public void shouldSavePatientWithDefaultValuesIfNotGiven() throws Exception {
        PatientData patientData = initPatientData();
        patientData.setGender("M");
        patientData.setGivenName("Murli");
        patientData.setSurName("Dharan");

        String healthId = patientRepository.create(patientData).getId();

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertNotNull(patient);
        assertFalse(patient.getConfidential());
        assertTrue(patient.isActive());
        assertEquals(PATIENT_STATUS_ALIVE, patient.getStatus());
        assertEquals(HID_CARD_STATUS_REGISTERED, patient.getHidCardStatus());
    }

    @Test
    public void shouldBuildUpdateProcessBatch() {
        PatientData patient = buildPatient();
        MCIResponse mciResponse = patientRepository.create(patient);
        String healthId = mciResponse.getId();

        Batch batch = patientRepository.buildUpdateProcessBatch(patient, healthId, batch());
        String queryString = batch.getQueryString();
        assertNotNull(queryString);
        assertTrue(queryString.startsWith("BEGIN BATCH"));
    }

    @Test
    public void shouldFindByHealthIdsInTheOrderIdsArePassed() {
        PatientData patient = buildPatient();
        patient.setNationalId(null);
        patient.setBirthRegistrationNumber(null);
        patient.setUid(null);

        String healthId1 = patientRepository.create(patient).getId();
        assertTrue(isNotBlank(healthId1));
        patient.setHealthId(String.valueOf(new Date().getTime()));
        String healthId2 = patientRepository.create(patient).getId();
        assertTrue(isNotBlank(healthId2));
        patient.setHealthId(String.valueOf(new Date().getTime()));
        String healthId3 = patientRepository.create(patient).getId();
        assertTrue(isNotBlank(healthId3));

        List<PatientData> patients = patientRepository.findByHealthId(asList(healthId1, healthId2, healthId3));
        assertEquals(healthId1, patients.get(0).getHealthId());
        assertEquals(healthId2, patients.get(1).getHealthId());
        assertEquals(healthId3, patients.get(2).getHealthId());
    }

    @Test
    public void shouldReturnEmptyCollectionIfNoPatientFoundInCatchment() {
        Catchment catchment = new Catchment("10", "20", "30");
        catchment.setCityCorpId("40");
        List<PatientData> patients = patientRepository.findAllByCatchment(catchment, new Date(), null, 100);
        assertNotNull(patients);
        assertTrue(isEmpty(patients));
    }

    @Test
    public void shouldFindAllPendingApprovalMappingsInAscendingOrderOfLastUpdated() throws Exception {
        cassandraOps.insert(asList(buildPendingApprovalMapping("31", "h101"),
                buildPendingApprovalMapping("30", "h102"),
                buildPendingApprovalMapping("30", "h103"),
                buildPendingApprovalMapping("32", "h104"),
                buildPendingApprovalMapping("30", "h105")));

        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), null, null,
                25);
        assertEquals(3, mappings.size());
        PendingApprovalMapping mapping1 = mappings.get(0);
        PendingApprovalMapping mapping2 = mappings.get(1);
        PendingApprovalMapping mapping3 = mappings.get(2);

        assertEquals("h102", mapping1.getHealthId());
        assertEquals("h103", mapping2.getHealthId());
        assertEquals("h105", mapping3.getHealthId());

        assertTrue(isGraterUUID(mapping2.getLastUpdated(), mapping1.getLastUpdated()));
        assertTrue(isGraterUUID(mapping3.getLastUpdated(), mapping2.getLastUpdated()));
    }

    @Test
    public void shouldFindPendingApprovalMappingsBasedOnGivenTimes() throws Exception {
        List<PendingApprovalMapping> entities = asList(buildPendingApprovalMapping("30", "h101"),
                buildPendingApprovalMapping("30", "h102"),
                buildPendingApprovalMapping("30", "h103"),
                buildPendingApprovalMapping("30", "h104"),
                buildPendingApprovalMapping("30", "h105"));
        cassandraOps.insert(entities);

        UUID after = entities.get(1).getLastUpdated();
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), after,
                null, 25);
        assertEquals(3, mappings.size());
        assertEquals("h103", mappings.get(0).getHealthId());
        assertEquals("h104", mappings.get(1).getHealthId());
        assertEquals("h105", mappings.get(2).getHealthId());

        UUID before = entities.get(4).getLastUpdated();
        mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), null,
                before, 3);
        assertEquals(3, mappings.size());
        assertEquals("h102", mappings.get(0).getHealthId());
        assertEquals("h103", mappings.get(1).getHealthId());
        assertEquals("h104", mappings.get(2).getHealthId());

        after = entities.get(0).getLastUpdated();
        before = entities.get(4).getLastUpdated();
        mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), after,
                before, 25);
        assertEquals(3, mappings.size());
        assertEquals("h102", mappings.get(0).getHealthId());
        assertEquals("h103", mappings.get(1).getHealthId());
        assertEquals("h104", mappings.get(2).getHealthId());
    }

    @Test
    public void shouldFindPendingApprovalMappingsWithLimit() throws Exception {
        List<PendingApprovalMapping> entities = asList(buildPendingApprovalMapping("30", "h101"),
                buildPendingApprovalMapping("30", "h102"),
                buildPendingApprovalMapping("30", "h103"),
                buildPendingApprovalMapping("30", "h104"),
                buildPendingApprovalMapping("30", "h105"));
        cassandraOps.insert(entities);
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), null, null,
                5);
        assertEquals(5, mappings.size());
        mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), null, null, 3);
        assertEquals(3, mappings.size());
    }

    private PatientData buildPatient() {
        PatientData data = initPatientData();
        data.setHealthId(String.valueOf(new Date().getTime()));
        data.setNationalId(nationalId);
        data.setBirthRegistrationNumber(birthRegistrationNumber);
        data.setUid(uid);
        data.setGivenName(givenName);
        data.setSurName(surname);
        data.setDateOfBirth(parseDate("2014-12-01"));
        data.setDobType("1");
        data.setGender("M");
        data.setOccupation("03");
        data.setEducationLevel("BA");
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber(phoneNumber);
        data.setPhoneNumber(phone);
        data.setHouseholdCode(householdCode);
        data.setHidCardStatus(HID_CARD_STATUS_REGISTERED);

        Address address = createAddress(divisionId, districtId, upazilaId, "99", "01", "02");
        data.setAddress(address);

        data.setRequester(FACILITY, null);

        return data;
    }

    private Address createAddress(String division, String district, String upazila, String cityCorp, String union, String ruralWard) {
        Address address = new Address(division, district, upazila);
        address.setAddressLine("house-10");
        address.setCityCorporationId(cityCorp);
        address.setUnionOrUrbanWardId(union);
        address.setRuralWardId(ruralWard);
        address.setCountryCode("050");

        return address;
    }

    private PatientData initPatientData() {
        PatientData patient = new PatientData();
        patient.setRequester(FACILITY, null);
        patient.setHealthId(String.valueOf(new Date().getTime()));
        return patient;
    }

    private void assertPatient(PatientData savedPatient, PatientData data) {
        assertEquals(data.getHealthId(), savedPatient.getHealthId());
        assertEquals(data.getDateOfBirth(), savedPatient.getDateOfBirth());
        assertEquals(data.getGender(), savedPatient.getGender());
        assertEquals(data.getNationalId(), savedPatient.getNationalId());
        assertEquals(data.getBirthRegistrationNumber(), savedPatient.getBirthRegistrationNumber());
        assertTrue(data.getAddress().equals(savedPatient.getAddress()));
        assertEquals(data.getPermanentAddress(), savedPatient.getPermanentAddress());
        assertEquals(data.getUid(), savedPatient.getUid());
        assertEquals(data.getGivenName(), savedPatient.getGivenName());
        assertEquals(data.getSurName(), savedPatient.getSurName());
        assertEquals(data.getOccupation(), savedPatient.getOccupation());
        assertEquals(data.getEducationLevel(), savedPatient.getEducationLevel());
        assertEquals(data.getHidCardStatus(), savedPatient.getHidCardStatus());
        assertNotNull(savedPatient.getUpdatedAt());
        assertNotNull(savedPatient.getCreatedAt());
    }

    private boolean isGraterUUID(UUID uuid1, UUID uuid2) {
        Long timeFromUUID = getTimeFromUUID(uuid1);
        Long timeFromLatestUUID = getTimeFromUUID(uuid2);
        int isGreater = timeFromUUID.compareTo(timeFromLatestUUID);
        if (isGreater == 0) {
            return uuid1.compareTo(uuid2) > 0;
        }
        return isGreater > 0;
    }

    private PendingApprovalMapping buildPendingApprovalMapping(String upazilaId, String healthId) throws InterruptedException {
        PendingApprovalMapping mapping = new PendingApprovalMapping();
        mapping.setCatchmentId(new Catchment("10", "20", upazilaId).getId());
        mapping.setHealthId(healthId);
        mapping.setLastUpdated(TimeUuidUtil.uuidForDate(new Date()));
        return mapping;
    }
}
