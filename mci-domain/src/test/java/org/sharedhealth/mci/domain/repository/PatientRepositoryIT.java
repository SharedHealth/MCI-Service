package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.querybuilder.Batch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sharedhealth.mci.domain.exception.PatientNotFoundException;
import org.sharedhealth.mci.domain.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.domain.constant.JsonConstants.PHONE_NUMBER;
import static org.sharedhealth.mci.domain.constant.MCIConstants.PATIENT_STATUS_ALIVE;
import static org.sharedhealth.mci.domain.constant.MCIConstants.STRING_NO;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.sharedhealth.mci.domain.repository.TestUtil.setupApprovalsConfig;
import static org.sharedhealth.mci.domain.repository.TestUtil.truncateAllColumnFamilies;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;

public class PatientRepositoryIT extends BaseRepositoryIT {
    public static final String FACILITY = "Bahmni";

    public String surname = "Tiger";
    public String phoneNumber = "999900000";
    public String divisionId = "10";
    public String districtId = "04";
    public String upazilaId = "09";
    private String nationalId = "1234567890123";
    private String birthRegistrationNumber = "12345678901234567";
    private String uid = "12345678901";
    private String givenName = "Scott";
    private String householdCode = "12345";
    private PatientData data;


    @Autowired
    private PatientRepository patientRepository;

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        data = buildPatient();
        setupApprovalsConfig(cassandraOps);
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

    private Address createAddress(String division, String district, String upazila) {
        Address address = new Address(division, district, upazila);
        address.setCountryCode("050");

        return address;
    }

    @Test
    public void shouldNotCreateIdMappingsWhenPatientIsCreatedWithoutIds() {
        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setAddress(createAddress("10", "20", "30"));

        assertNotNull(patientRepository.create(patient).getId());
        assertIdAndPhoneNumberMappingsEmpty();
    }

    @Test
    public void shouldNotCreateHouseholdCodeMappingsWhenPatientIsCreatedWithoutHouseholdCode() {
        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setAddress(createAddress("10", "20", "30"));

        assertNotNull(patientRepository.create(patient).getId());
        assertHouseholdCodeMappingEmpty();
    }

    @Test
    public void shouldNotCreatePhoneNumberMappingWhenPatientIsCreatedWithoutPhoneNumber() {
        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setAddress(createAddress("1", "2", "3"));
        assertNotNull(patientRepository.create(patient).getId());

        String cql = select().from(CF_PHONE_NUMBER_MAPPING).toString();
        assertTrue(isEmpty(cassandraOps.select(cql, PhoneNumberMapping.class)));
    }

    @Test
    public void shouldFindPatientWithMatchingGeneratedHealthId() throws ExecutionException, InterruptedException {
        PatientData createData = initPatientData(data);

        MCIResponse mciResponse = patientRepository.create(createData);
        PatientData p = patientRepository.findByHealthId(mciResponse.id);

        assertNotNull(p);
        createData.setHealthId(mciResponse.id);
        createData.setCreatedAt(p.getCreatedAt());
        createData.setUpdatedAt(p.getUpdatedAt());
        PatientStatus patientStatus = p.getPatientStatus();
        patientStatus.setType(PATIENT_STATUS_ALIVE);
        createData.setPatientStatus(patientStatus);
        createData.setConfidential(STRING_NO);

        Address address = p.getAddress();
        address.setHoldingNumber(null);
        address.setStreet(null);
        address.setVillage(null);
        address.setPostCode(null);
        address.setPostOffice(null);
        address.setAreaMouja(null);
        p.setAddress(address);

        PhoneNumber phoneNumber = p.getPhoneNumber();
        phoneNumber.setAreaCode(null);
        phoneNumber.setExtension(null);
        phoneNumber.setCountryCode(null);
        p.setPhoneNumber(phoneNumber);

        assertEquals(createData, p);
        assertNotNull(p.getCreatedBy());
        assertEquals(createData.getRequester(), p.getCreatedBy());
        assertNotNull(p.getCreatedAt());
        assertNotNull(p.getCreatedAt());
    }

    @Test(expected = PatientNotFoundException.class)
    public void shouldThrowException_IfPatientDoesNotExistForGivenHealthId() {
        patientRepository.findByHealthId(UUID.randomUUID().toString());
    }

    @Test(expected = PatientNotFoundException.class)
    public void shouldThrowErrorIfPatientNotFound() throws Exception {
        patientRepository.update(initPatientData(), "1");
    }

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

    @Test
    public void shouldUpdatePatient() throws Exception {
        PatientData data = buildPatient();
        MCIResponse mciResponseForCreate = patientRepository.create(data);
        String healthId = mciResponseForCreate.getId();
        data.setHealthId(healthId);
        data.setGivenName("Danny");
        MCIResponse mciResponseForUpdate = patientRepository.update(data, data.getHealthId());
        assertEquals(202, mciResponseForUpdate.getHttpStatus());
        PatientData savedPatient = patientRepository.findByHealthId(healthId);

        Address address = savedPatient.getAddress();
        address.setHoldingNumber(null);
        address.setStreet(null);
        address.setVillage(null);
        address.setPostCode(null);
        address.setPostOffice(null);
        address.setAreaMouja(null);
        savedPatient.setAddress(address);
        assertPatient(savedPatient, data);
    }

    @Test
    public void shouldBuildUpdateProcessBatch() {
        PatientData patient = buildPatient();
        MCIResponse mciResponse = patientRepository.create(patient);
        String healthId = mciResponse.getId();

        Batch batch = batch();
        patientRepository.buildUpdateProcessBatch(patient, healthId, batch);
        String queryString = batch.getQueryString();
        assertNotNull(queryString);
        assertTrue(queryString.startsWith("BEGIN BATCH"));
    }

    @Test
    public void shouldNotUpdateIdAndPhoneNumberMappingsWhenExistingValueIsNullAndNewValueIsNull() {
        String existingNid = null;
        String existingBrn = null;
        String existingUid = null;
        PhoneNumber existingPhoneNumber = null;
        String existingReligion = "01";

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId(existingNid);
        patient.setBirthRegistrationNumber(existingBrn);
        patient.setUid(existingUid);
        patient.setReligion(existingReligion);
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(existingPhoneNumber);
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertIdAndPhoneNumberMappingsEmpty();

        String newNid = null;
        String newBrn = null;
        String newUid = null;
        PhoneNumber newPhoneNumber = null;
        String newReligion = "02";

        PatientData updateRequest = initPatientData();
        updateRequest.setReligion(newReligion);
        updateRequest.setNationalId(newNid);
        updateRequest.setBirthRegistrationNumber(newBrn);
        updateRequest.setUid(newUid);
        updateRequest.setPhoneNumber(newPhoneNumber);
        patientRepository.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newReligion, updatedPatient.getReligion());
        assertEquals(newNid, updatedPatient.getNationalId());
        assertEquals(newBrn, updatedPatient.getBirthRegistrationNumber());
        assertEquals(newUid, updatedPatient.getUid());
        assertEquals(newPhoneNumber, updatedPatient.getPhoneNumber());

        assertTrue(isEmpty(updatedPatient.getPendingApprovals()));
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_PENDING_APPROVAL_MAPPING).toString(), PendingApprovalMapping.class)));

        assertIdAndPhoneNumberMappingsEmpty();
    }

    @Test
    public void shouldNotUpdateHouseholdCodeMappingsWhenExistingValueIsNullAndNewValueIsNull() {
        String existingHouseholdCode = null;

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setHouseholdCode(existingHouseholdCode);
        patient.setAddress(createAddress("10", "20", "30"));
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertIdAndPhoneNumberMappingsEmpty();

        String newHouseholdCode = null;
        final String newReligion = "02";

        PatientData updateRequest = initPatientData();
        updateRequest.setReligion(newReligion);
        updateRequest.setHouseholdCode(newHouseholdCode);
        patientRepository.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newReligion, updatedPatient.getReligion());
        assertEquals(newHouseholdCode, updatedPatient.getHouseholdCode());


        assertHouseholdCodeMappingEmpty();
    }

    @Test
    public void shouldNotUpdateHouseholdCodeMappingsWhenExistingValueIsNullAndNewValueIsEmpty() {
        String existingHouseholdCode = null;

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setHouseholdCode(existingHouseholdCode);
        patient.setAddress(createAddress("10", "20", "30"));
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertIdAndPhoneNumberMappingsEmpty();

        String newHouseholdCode = "";
        final String newReligion = "02";

        PatientData updateRequest = initPatientData();
        updateRequest.setReligion(newReligion);
        updateRequest.setHouseholdCode(newHouseholdCode);
        patientRepository.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newReligion, updatedPatient.getReligion());
        assertEquals(newHouseholdCode, updatedPatient.getHouseholdCode());

        assertHouseholdCodeMappingEmpty();
    }

    @Test
    public void shouldUpdateHouseholdCodeMappingsWhenExistingValueIsNullAndNewValueIsNotEmpty() {
        String existingHouseholdCode = null;

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setHouseholdCode(existingHouseholdCode);
        patient.setAddress(createAddress("10", "20", "30"));
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertIdAndPhoneNumberMappingsEmpty();

        String newHouseholdCode = "1234";
        final String newReligion = "02";

        PatientData updateRequest = initPatientData();
        updateRequest.setReligion(newReligion);
        updateRequest.setHouseholdCode(newHouseholdCode);
        patientRepository.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newReligion, updatedPatient.getReligion());
        assertEquals(newHouseholdCode, updatedPatient.getHouseholdCode());

        assertSearchByHouseholdCode(newHouseholdCode, healthId);
    }

    @Test
    public void shouldNotUpdateIdAndPhoneNumberMappingsWhenExistingValueIsNullAndNewValueIsEmpty() {
        String existingNid = null;
        String existingBrn = null;
        String existingUid = null;
        PhoneNumber existingPhoneNumber = null;
        String existingReligion = "01";

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId(existingNid);
        patient.setBirthRegistrationNumber(existingBrn);
        patient.setUid(existingUid);
        patient.setReligion(existingReligion);
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(existingPhoneNumber);
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertIdAndPhoneNumberMappingsEmpty();

        String newNid = "";
        String newBrn = "";
        String newUid = "";
        String newReligion = "02";
        PhoneNumber newPhoneNumber = new PhoneNumber();
        newPhoneNumber.setCountryCode("");
        newPhoneNumber.setAreaCode("");
        newPhoneNumber.setNumber("");
        newPhoneNumber.setExtension("");

        PatientData updateRequest = initPatientData();
        updateRequest.setReligion(newReligion);
        updateRequest.setNationalId(newNid);
        updateRequest.setBirthRegistrationNumber(newBrn);
        updateRequest.setUid(newUid);
        updateRequest.setPhoneNumber(newPhoneNumber);
        patientRepository.update(updateRequest, healthId);

        PatientData approvalRequest = initPatientData();
        approvalRequest.setHealthId(healthId);
        approvalRequest.setPhoneNumber(newPhoneNumber);
        patientRepository.processPendingApprovals(approvalRequest, patientRepository.findByHealthId(healthId), true);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newReligion, updatedPatient.getReligion());
        assertEquals(newNid, updatedPatient.getNationalId());
        assertEquals(newBrn, updatedPatient.getBirthRegistrationNumber());
        assertEquals(newUid, updatedPatient.getUid());
        assertEquals(newPhoneNumber, updatedPatient.getPhoneNumber());

        assertIdAndPhoneNumberMappingsEmpty();
    }

    @Test
    public void shouldUpdateIdAndPhoneNumberMappingsWhenExistingValueIsNullAndNewValueIsNotEmpty() {
        String existingNid = null;
        String existingBrn = null;
        String existingUid = null;
        PhoneNumber existingPhoneNumber = null;
        String existingReligion = "01";

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId(existingNid);
        patient.setBirthRegistrationNumber(existingBrn);
        patient.setUid(existingUid);
        patient.setReligion(existingReligion);
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(existingPhoneNumber);
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertIdAndPhoneNumberMappingsEmpty();

        String newNid = "1000000000000";
        String newBrn = "10000000000000000";
        String newUid = "10000000000";
        String newReligion = "02";
        PhoneNumber newPhoneNumber = new PhoneNumber();
        newPhoneNumber.setCountryCode("91");
        newPhoneNumber.setAreaCode("80");
        newPhoneNumber.setNumber("10002000");
        newPhoneNumber.setExtension("");

        PatientData updateRequest = initPatientData();
        updateRequest.setReligion(newReligion);
        updateRequest.setNationalId(newNid);
        updateRequest.setBirthRegistrationNumber(newBrn);
        updateRequest.setUid(newUid);
        updateRequest.setPhoneNumber(newPhoneNumber);
        patientRepository.update(updateRequest, healthId);

        PatientData approvalRequest = initPatientData();
        approvalRequest.setHealthId(healthId);
        approvalRequest.setPhoneNumber(newPhoneNumber);
        patientRepository.processPendingApprovals(approvalRequest, patientRepository.findByHealthId(healthId), true);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newReligion, updatedPatient.getReligion());
        assertEquals(newNid, updatedPatient.getNationalId());
        assertEquals(newBrn, updatedPatient.getBirthRegistrationNumber());
        assertEquals(newUid, updatedPatient.getUid());
        assertEquals(newPhoneNumber, updatedPatient.getPhoneNumber());

        assertIdAndPhoneNumberMappingsExist();
        assertSearchByNid(newNid, healthId);
        assertSearchByBrn(newBrn, healthId);
        assertSearchByUid(newUid, healthId);
        assertSearchByPhoneNumber(newPhoneNumber, healthId);

    }

    @Test
    public void shouldNotUpdateIdAndPhoneNumberMappingsWhenExistingValueIsEmptyAndNewValueIsNull() {
        String existingNid = "";
        String existingBrn = "";
        String existingUid = "";
        String existingReligion = "01";
        PhoneNumber existingPhoneNumber = new PhoneNumber();
        existingPhoneNumber.setCountryCode("");
        existingPhoneNumber.setAreaCode("");
        existingPhoneNumber.setNumber("");
        existingPhoneNumber.setExtension("");

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId(existingNid);
        patient.setBirthRegistrationNumber(existingBrn);
        patient.setUid(existingUid);
        patient.setReligion(existingReligion);
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(existingPhoneNumber);
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertIdAndPhoneNumberMappingsEmpty();

        String newNid = null;
        String newBrn = null;
        String newUid = null;
        PhoneNumber newPhoneNumber = null;
        String newReligion = "02";

        PatientData updateRequest = initPatientData();
        updateRequest.setReligion(newReligion);
        updateRequest.setNationalId(newNid);
        updateRequest.setBirthRegistrationNumber(newBrn);
        updateRequest.setUid(newUid);
        updateRequest.setPhoneNumber(newPhoneNumber);
        patientRepository.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newReligion, updatedPatient.getReligion());
        assertEquals(existingNid, updatedPatient.getNationalId());
        assertEquals(existingBrn, updatedPatient.getBirthRegistrationNumber());
        assertEquals(existingUid, updatedPatient.getUid());
        assertEquals(existingPhoneNumber, updatedPatient.getPhoneNumber());
        assertTrue(isEmpty(updatedPatient.getPendingApprovals()));

        assertTrue(isEmpty(cassandraOps.select(select().from(CF_PENDING_APPROVAL_MAPPING).toString(), PendingApprovalMapping.class)));
        assertIdAndPhoneNumberMappingsEmpty();
    }

    @Test
    public void shouldNotUpdateIdAndPhoneMappingsWhenExistingValueIsEmptyAndNewValueIsEmpty() {
        String existingNid = "";
        String existingBrn = "";
        String existingUid = "";
        String existingReligion = "01";
        PhoneNumber existingPhoneNumber = new PhoneNumber();
        existingPhoneNumber.setCountryCode("");
        existingPhoneNumber.setAreaCode("");
        existingPhoneNumber.setNumber("");
        existingPhoneNumber.setExtension("");

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId(existingNid);
        patient.setBirthRegistrationNumber(existingBrn);
        patient.setUid(existingUid);
        patient.setReligion(existingReligion);
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(existingPhoneNumber);
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertIdAndPhoneNumberMappingsEmpty();

        String newNid = "";
        String newBrn = "";
        String newUid = "";
        String newReligion = "02";
        PhoneNumber newPhoneNumber = new PhoneNumber();
        newPhoneNumber.setCountryCode("");
        newPhoneNumber.setAreaCode("");
        newPhoneNumber.setNumber("");
        newPhoneNumber.setExtension("");

        PatientData updateRequest = initPatientData();
        updateRequest.setReligion(newReligion);
        updateRequest.setNationalId(newNid);
        updateRequest.setBirthRegistrationNumber(newBrn);
        updateRequest.setUid(newUid);
        updateRequest.setPhoneNumber(newPhoneNumber);
        patientRepository.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newReligion, updatedPatient.getReligion());
        assertEquals(existingNid, updatedPatient.getNationalId());
        assertEquals(existingBrn, updatedPatient.getBirthRegistrationNumber());
        assertEquals(existingUid, updatedPatient.getUid());
        assertTrue(isEmpty(updatedPatient.getPendingApprovals()));

        assertTrue(isEmpty(cassandraOps.select(select().from(CF_PENDING_APPROVAL_MAPPING).toString(), PendingApprovalMapping.class)));
        assertIdAndPhoneNumberMappingsEmpty();
    }

    @Test
    public void shouldUpdateIdAndPhoneNumberMappingsWhenExistingValueIsEmptyAndNewValueIsNotEmpty() {
        String existingNid = "";
        String existingBrn = "";
        String existingUid = "";
        String existingReligion = "01";
        PhoneNumber existingPhoneNumber = new PhoneNumber();
        existingPhoneNumber.setCountryCode("");
        existingPhoneNumber.setAreaCode("");
        existingPhoneNumber.setNumber("");
        existingPhoneNumber.setExtension("");

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId(existingNid);
        patient.setBirthRegistrationNumber(existingBrn);
        patient.setUid(existingUid);
        patient.setReligion(existingReligion);
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(existingPhoneNumber);
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertIdAndPhoneNumberMappingsEmpty();

        String newNid = "1000000000000";
        String newBrn = "10000000000000000";
        String newUid = "10000000000";
        String newReligion = "02";
        PhoneNumber newPhoneNumber = new PhoneNumber();
        newPhoneNumber.setCountryCode("91");
        newPhoneNumber.setAreaCode("80");
        newPhoneNumber.setNumber("10002000");
        newPhoneNumber.setExtension("");

        PatientData updateRequest = initPatientData();
        updateRequest.setReligion(newReligion);
        updateRequest.setNationalId(newNid);
        updateRequest.setBirthRegistrationNumber(newBrn);
        updateRequest.setUid(newUid);
        updateRequest.setPhoneNumber(newPhoneNumber);
        patientRepository.update(updateRequest, healthId);

        PatientData approvalRequest = initPatientData();
        approvalRequest.setHealthId(healthId);
        approvalRequest.setPhoneNumber(newPhoneNumber);
        patientRepository.processPendingApprovals(approvalRequest, patientRepository.findByHealthId(healthId), true);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newReligion, updatedPatient.getReligion());
        assertEquals(newNid, updatedPatient.getNationalId());
        assertEquals(newBrn, updatedPatient.getBirthRegistrationNumber());
        assertEquals(newUid, updatedPatient.getUid());
        assertEquals(newPhoneNumber, updatedPatient.getPhoneNumber());

        assertIdAndPhoneNumberMappingsExist();
        assertSearchByNid(newNid, healthId);
        assertSearchByBrn(newBrn, healthId);
        assertSearchByUid(newUid, healthId);
        assertSearchByPhoneNumber(newPhoneNumber, healthId);
    }

    private void assertIdAndPhoneNumberMappingsEmpty() {
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_NID_MAPPING).toString(), NidMapping.class)));
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_BRN_MAPPING).toString(), BrnMapping.class)));
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_UID_MAPPING).toString(), UidMapping.class)));
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_PHONE_NUMBER_MAPPING).toString(), PhoneNumberMapping.class)));
    }

    private void assertIdAndPhoneNumberMappingsExist() {
        List<NidMapping> nidMappings = cassandraOps.select(select().from(CF_NID_MAPPING).toString(), NidMapping.class);
        assertTrue(isNotEmpty(nidMappings));
        assertEquals(1, nidMappings.size());

        List<BrnMapping> brnMappings = cassandraOps.select(select().from(CF_BRN_MAPPING).toString(), BrnMapping.class);
        assertTrue(isNotEmpty(brnMappings));
        assertEquals(1, brnMappings.size());

        List<UidMapping> uidMappings = cassandraOps.select(select().from(CF_UID_MAPPING).toString(), UidMapping.class);
        assertTrue(isNotEmpty(uidMappings));
        assertEquals(1, uidMappings.size());

        List<PhoneNumberMapping> phoneNumberMappings = cassandraOps.select(select().from(CF_PHONE_NUMBER_MAPPING).toString(),
                PhoneNumberMapping.class);
        assertTrue(isNotEmpty(phoneNumberMappings));
        assertEquals(1, phoneNumberMappings.size());
    }

    private void assertSearchByNid(String nid, String healthId) {
        SearchQuery query = new SearchQuery();
        query.setNid(nid);
        List<PatientData> patients = patientRepository.findAllByQuery(query);
        assertTrue(isNotEmpty(patients));
        assertEquals(1, patients.size());
        assertEquals(healthId, patients.get(0).getHealthId());
    }

    private void assertSearchByBrn(String brn, String healthId) {
        SearchQuery query = new SearchQuery();
        query.setBin_brn(brn);
        List<PatientData> patients = patientRepository.findAllByQuery(query);
        assertTrue(isNotEmpty(patients));
        assertEquals(1, patients.size());
        assertEquals(healthId, patients.get(0).getHealthId());
    }

    private void assertSearchByUid(String uid, String healthId) {
        SearchQuery query = new SearchQuery();
        query.setUid(uid);
        List<PatientData> patients = patientRepository.findAllByQuery(query);
        assertTrue(isNotEmpty(patients));
        assertEquals(1, patients.size());
        assertEquals(healthId, patients.get(0).getHealthId());
    }

    private void assertSearchByPhoneNumber(PhoneNumber phoneNumber, String healthId) {
        assertNotNull(phoneNumber);
        SearchQuery query = new SearchQuery();
        query.setPhone_no(phoneNumber.getNumber());
        List<PatientData> patients = patientRepository.findAllByQuery(query);
        assertTrue(isNotEmpty(patients));
        assertEquals(1, patients.size());
        assertEquals(healthId, patients.get(0).getHealthId());
    }

    private void assertSearchByHouseholdCode(String householdCode, String healthId) {
        assertNotNull(householdCode);
        List<PatientData> patients = getPatientDatasByHousehold(householdCode);
        assertTrue(isNotEmpty(patients));
        assertEquals(1, patients.size());
        assertEquals(healthId, patients.get(0).getHealthId());
    }

    private List<PatientData> getPatientDatasByHousehold(String householdCode) {
        SearchQuery query = new SearchQuery();
        query.setHousehold_code(householdCode);
        return patientRepository.findAllByQuery(query);
    }


    @Test
    public void shouldUpdateNameMappingWhenGivenNameIsUpdated() {
        String existingGivenName = "John";
        PatientData existingPatient = initPatientData();
        existingPatient.setGivenName(existingGivenName);
        existingPatient.setSurName("Doe");
        existingPatient.setAddress(createAddress("10", "20", "30"));
        String healthId = patientRepository.create(existingPatient).getId();
        assertNotNull(healthId);

        String newGivenName = "Jane";
        PatientData updateRequest = initPatientData();
        updateRequest.setGivenName(newGivenName);
        patientRepository.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newGivenName, updatedPatient.getGivenName());

        assertSearchByNameAndAddressEmpty(existingGivenName, "102030");
        assertSearchByNameAndAddressExists(newGivenName, "102030", healthId);
    }

    @Test
    public void shouldUpdateNameMappingWhenPresentAddressIsUpdated() {
        String existingGivenName = "John";

        PatientData existingPatient = initPatientData();
        existingPatient.setGivenName(existingGivenName);
        existingPatient.setSurName("Doe");
        existingPatient.setAddress(createAddress("10", "20", "30"));
        String healthId = patientRepository.create(existingPatient).getId();
        assertNotNull(healthId);

        PatientData updateRequest = initPatientData();
        Address newAddress = createAddress("11", "22", "33");
        updateRequest.setAddress(newAddress);
        patientRepository.update(updateRequest, healthId);

        PatientData approvalRequest = initPatientData();
        approvalRequest.setHealthId(healthId);
        approvalRequest.setAddress(newAddress);
        patientRepository.processPendingApprovals(approvalRequest, patientRepository.findByHealthId(healthId), true);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(existingGivenName, updatedPatient.getGivenName());
        assertEquals(newAddress, updatedPatient.getAddress());

        assertSearchByNameAndAddressEmpty(existingGivenName, "102030");
        assertSearchByNameAndAddressExists(existingGivenName, "112233", healthId);
    }

    @Test
    public void shouldUpdateNameMappingWhenBothGivenNameAndPresentAddressAreUpdated() {
        String existingGivenName = "John";
        PatientData existingPatient = initPatientData();
        existingPatient.setGivenName(existingGivenName);
        existingPatient.setSurName("Doe");
        existingPatient.setAddress(createAddress("10", "20", "30"));
        String healthId = patientRepository.create(existingPatient).getId();
        assertNotNull(healthId);

        String newGivenName = "Jane";
        PatientData updateRequest = initPatientData();
        updateRequest.setGivenName(newGivenName);
        Address newAddress = createAddress("11", "22", "33");
        updateRequest.setAddress(newAddress);
        patientRepository.update(updateRequest, healthId);

        PatientData approvalRequest = initPatientData();
        approvalRequest.setHealthId(healthId);
        approvalRequest.setAddress(newAddress);
        patientRepository.processPendingApprovals(approvalRequest, patientRepository.findByHealthId(healthId), true);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newGivenName, updatedPatient.getGivenName());
        assertEquals(newAddress, updatedPatient.getAddress());

        assertSearchByNameAndAddressEmpty(existingGivenName, "102030");
        assertSearchByNameAndAddressExists(newGivenName, "112233", healthId);
    }

    private void assertSearchByNameAndAddressEmpty(String givenName, String address) {
        SearchQuery query = new SearchQuery();
        query.setGiven_name(givenName);
        query.setPresent_address(address);
        assertTrue(isEmpty(patientRepository.findAllByQuery(query)));
    }


    private void assertSearchByNameAndAddressExists(String givenName, String address, String healthId) {
        SearchQuery query = new SearchQuery();
        query.setGiven_name(givenName);
        query.setPresent_address(address);
        List<PatientData> patients = patientRepository.findAllByQuery(query);
        assertTrue(isNotEmpty(patients));
        assertEquals(1, patients.size());
        assertEquals(healthId, patients.get(0).getHealthId());
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
        assertNotNull(savedPatient.getUpdatedAt());
        assertNotNull(savedPatient.getCreatedAt());
    }

    @Test
    public void shouldCreateAsManyPendingApprovalMappingsAsNumberOfPossibleCatchments() {
        PatientData createPatientData = data;
        Address address = createAddress("10", "20", "30");
        address.setCityCorporationId("40");
        address.setUnionOrUrbanWardId("50");
        address.setRuralWardId("60");
        createPatientData.setAddress(address);
        String healthId = patientRepository.create(createPatientData).getId();

        PatientData patientData = initPatientData();
        patientData.setGender("F");
        patientRepository.update(patientData, healthId);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        assertPendingApprovalMappings(healthId, address, pendingApprovals);
    }

    private void assertPendingApprovalMappings(String healthId, Address address, TreeSet<PendingApproval> pendingApprovals) {
        Catchment catchment = buildCatchment(address);
        List<String> catchmentIds = catchment.getAllIds();

        List<PendingApprovalMapping> mappings = findAllPendingApprovalMappings();
        assertEquals(catchmentIds.size(), mappings.size());

        UUID uuid = patientRepository.findLatestUuid(pendingApprovals);
        for (PendingApprovalMapping mapping : mappings) {
            assertTrue(catchmentIds.contains(mapping.getCatchmentId()));
            assertEquals(healthId, mapping.getHealthId());
            assertEquals(uuid, mapping.getLastUpdated());
        }
    }

    private List<PendingApprovalMapping> findAllPendingApprovalMappings() {
        return cassandraOps.select(select().from(CF_PENDING_APPROVAL_MAPPING), PendingApprovalMapping.class);
    }

    private Catchment buildCatchment(Address address) {
        Catchment catchment = new Catchment(address.getDivisionId(), address.getDistrictId(), address.getUpazilaId());
        catchment.setCityCorpId(address.getCityCorporationId());
        catchment.setUnionOrUrbanWardId(address.getUnionOrUrbanWardId());
        catchment.setRuralWardId(address.getRuralWardId());
        return catchment;
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

        Date date1 = new Date(unixTimestamp(mapping1.getLastUpdated()));
        Date date2 = new Date(unixTimestamp(mapping2.getLastUpdated()));
        Date date3 = new Date(unixTimestamp(mapping3.getLastUpdated()));

        assertTrue(date1.before(date2));
        assertTrue(date2.before(date3));
    }

    @Test
    public void shouldFindPendingApprovalMappingsAfterGivenTime() throws Exception {
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
    }

    @Test
    public void shouldFindPendingApprovalMappingsBeforeGivenTime() throws Exception {
        List<PendingApprovalMapping> entities = asList(buildPendingApprovalMapping("30", "h101"),
                buildPendingApprovalMapping("30", "h102"),
                buildPendingApprovalMapping("30", "h103"),
                buildPendingApprovalMapping("30", "h104"),
                buildPendingApprovalMapping("30", "h105"));
        cassandraOps.insert(entities);

        UUID before = entities.get(4).getLastUpdated();
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), null,
                before, 3);
        assertEquals(3, mappings.size());
        assertEquals("h102", mappings.get(0).getHealthId());
        assertEquals("h103", mappings.get(1).getHealthId());
        assertEquals("h104", mappings.get(2).getHealthId());
    }

    @Test
    public void shouldFindPendingApprovalMappingsBetweenGivenTimes() throws Exception {
        List<PendingApprovalMapping> entities = asList(buildPendingApprovalMapping("30", "h101"),
                buildPendingApprovalMapping("30", "h102"),
                buildPendingApprovalMapping("30", "h103"),
                buildPendingApprovalMapping("30", "h104"),
                buildPendingApprovalMapping("30", "h105"));
        cassandraOps.insert(entities);

        UUID after = entities.get(0).getLastUpdated();
        UUID before = entities.get(4).getLastUpdated();
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), after,
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

    private PendingApprovalMapping buildPendingApprovalMapping(String upazilaId, String healthId) throws InterruptedException {
        Thread.sleep(0, 10);
        PendingApprovalMapping mapping = new PendingApprovalMapping();
        mapping.setCatchmentId(new Catchment("10", "20", upazilaId).getId());
        mapping.setHealthId(healthId);
        mapping.setLastUpdated(timeBased());
        return mapping;
    }

    @Test
    public void shouldCreatePendingApprovalInPatientAndMappingTables_IfPatientDoesNotHaveExistingPendingApproval() throws Exception {
        String healthId = patientRepository.create(data).getId();
        PatientData patientData = initPatientData();
        patientData.setGender("F");
        patientData.setRequester("Bahmni", "Dr. Monika");
        patientRepository.update(patientData, healthId);
        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        PendingApproval pendingApproval = pendingApprovals.iterator().next();
        assertEquals("gender", pendingApproval.getName());
        assertNull(pendingApproval.getCurrentValue());

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
        assertNotNull(fieldDetailsMap);
        assertEquals(1, fieldDetailsMap.size());
        PendingApprovalFieldDetails fieldDetails = fieldDetailsMap.values().iterator().next();
        assertEquals("F", fieldDetails.getValue());
        assertEquals(new Requester("Bahmni", "Dr. Monika"), fieldDetails.getRequestedBy());

        List<PendingApprovalMapping> mappings = findAllPendingApprovalMappings();
        List<String> catchmentIds = buildCatchment(data.getAddress()).getAllIds();
        assertEquals(catchmentIds.size(), mappings.size());
        PendingApprovalMapping mapping = mappings.get(0);
        assertEquals(healthId, mapping.getHealthId());
        assertTrue(catchmentIds.contains(mapping.getCatchmentId()));
        assertEquals(fieldDetailsMap.keySet().iterator().next(), mapping.getLastUpdated());
    }

    @Test
    public void shouldAddPendingApprovalInPatientAndUpdateMappingTables_IfPatientHasAnyPendingApproval() throws Exception {
        String healthId = patientRepository.create(initPatientData(data)).getId();

        PatientData updateRequest = initPatientData();
        updateRequest.setGender("F");
        updateRequest.setRequester(FACILITY, "Dr. Monika");
        patientRepository.update(updateRequest, healthId);

        Thread.sleep(0, 10);

        updateRequest = initPatientData();
        updateRequest.setGender("O");
        updateRequest.setRequester(FACILITY, "Dr. Seuss");
        patientRepository.update(updateRequest, healthId);
        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        PendingApproval pendingApproval = pendingApprovals.iterator().next();
        assertEquals("gender", pendingApproval.getName());
        assertEquals(null, pendingApproval.getCurrentValue());

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
        assertNotNull(fieldDetailsMap);
        assertEquals(2, fieldDetailsMap.size());

        Iterator<PendingApprovalFieldDetails> fieldDetailsIterator = fieldDetailsMap.values().iterator();
        PendingApprovalFieldDetails fieldDetails1 = fieldDetailsIterator.next();
        assertEquals("O", fieldDetails1.getValue());
        assertEquals(new Requester(FACILITY, "Dr. Seuss"), fieldDetails1.getRequestedBy());

        PendingApprovalFieldDetails fieldDetails2 = fieldDetailsIterator.next();
        assertEquals("F", fieldDetails2.getValue());
        assertEquals(new Requester(FACILITY, "Dr. Monika"), fieldDetails2.getRequestedBy());

        List<PendingApprovalMapping> mappings = findAllPendingApprovalMappings();
        List<String> catchmentIds = buildCatchment(data.getAddress()).getAllIds();
        assertEquals(catchmentIds.size(), mappings.size());
        PendingApprovalMapping mapping = mappings.get(0);
        assertEquals(healthId, mapping.getHealthId());
        assertTrue(catchmentIds.contains(mapping.getCatchmentId()));
        assertEquals(fieldDetailsMap.keySet().iterator().next(), mapping.getLastUpdated());
    }

    @Test
    public void shouldBeAbleToAddPendingApprovalsForMultipleFields() {
        String healthId = patientRepository.create(data).getId();

        PatientData patientData = initPatientData();
        patientData.setGivenName("John Doe");
        patientData.setGender("O");
        patientData.setOccupation("07");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("40000000777");
        patientData.setPhoneNumber(phoneNumber);

        patientRepository.update(patientData, healthId);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(3, pendingApprovals.size());

        Iterator<PendingApproval> pendingApprovalIterator = pendingApprovals.iterator();
        assertEquals(GENDER, pendingApprovalIterator.next().getName());
        assertEquals(OCCUPATION, pendingApprovalIterator.next().getName());
        assertEquals(PHONE_NUMBER, pendingApprovalIterator.next().getName());
    }

    @Test
    public void shouldBeAbleToAcceptPendingApprovalsWhenPatientHasOnePendingApproval() {
        String healthId = processPendingApprovalsWhenPatientHasOnePendingApproval(true);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertEquals("F", patient.getGender());
        assertTrue(isEmpty(patient.getPendingApprovals()));

        assertTrue(isEmpty(findAllPendingApprovalMappings()));
    }

    @Test
    public void shouldBeAbleToRejectPendingApprovalsWhenPatientHasOnePendingApproval() throws Exception {
        String healthId = processPendingApprovalsWhenPatientHasOnePendingApproval(false);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertEquals(data.getGender(), patient.getGender());
        assertTrue(isEmpty(patient.getPendingApprovals()));

        assertTrue(isEmpty(findAllPendingApprovalMappings()));
    }

    private String processPendingApprovalsWhenPatientHasOnePendingApproval(boolean shouldAccept) {
        String healthId = patientRepository.create(data).getId();

        PatientData patientData = initPatientData();
        patientData.setGender("F");
        patientRepository.update(patientData, healthId);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        PendingApproval pendingApproval = pendingApprovals.iterator().next();
        assertEquals(GENDER, pendingApproval.getName());
        assertNull(pendingApproval.getCurrentValue());

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
        assertNotNull(fieldDetailsMap);
        assertEquals(1, fieldDetailsMap.size());

        PendingApprovalFieldDetails fieldDetails = fieldDetailsMap.values().iterator().next();
        assertEquals("F", fieldDetails.getValue());

        assertPendingApprovalMappings(healthId, data.getAddress(), pendingApprovals);

        patientData = initPatientData();
        patientData.setHealthId(healthId);
        patientData.setGender("F");
        PatientData existingPatientData = patientRepository.findByHealthId(healthId);
        return patientRepository.processPendingApprovals(patientData, existingPatientData, shouldAccept);
    }

    @Test
    public void shouldBeAbleToAcceptPendingApprovalsWhenPatientHasOnePendingApprovalEachForMultipleFields() {
        String healthId = processPendingApprovalsWhenPatientHasOnePendingApprovalEachForMultipleFields(true);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertEquals("F", patient.getGender());
        assertEquals(data.getOccupation(), patient.getOccupation());
        assertEquals("22334455", patient.getCellNo());

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        PendingApproval pendingApproval = patient.getPendingApprovals().iterator().next();
        assertEquals(OCCUPATION, pendingApproval.getName());
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
        assertNotNull(fieldDetailsMap);
        assertEquals(1, fieldDetailsMap.size());
        PendingApprovalFieldDetails fieldDetails = fieldDetailsMap.values().iterator().next();
        assertEquals("09", fieldDetails.getValue());

        assertPendingApprovalMappings(healthId, data.getAddress(), pendingApprovals);
    }

    @Test
    public void shouldBeAbleToRejectPendingApprovalsWhenPatientHasOnePendingApprovalEachForMultipleFields() {
        String healthId = processPendingApprovalsWhenPatientHasOnePendingApprovalEachForMultipleFields(false);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertEquals(data.getGender(), patient.getGender());
        assertEquals(data.getOccupation(), patient.getOccupation());
        assertEquals(data.getPhoneNumber().getNumber(), patient.getCellNo());

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());
        PendingApproval pendingApproval = patient.getPendingApprovals().iterator().next();
        assertEquals(OCCUPATION, pendingApproval.getName());
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
        assertNotNull(fieldDetailsMap);
        assertEquals(1, fieldDetailsMap.size());
        PendingApprovalFieldDetails fieldDetails = fieldDetailsMap.values().iterator().next();
        assertEquals("09", fieldDetails.getValue());

        assertPendingApprovalMappings(healthId, data.getAddress(), pendingApprovals);
    }

    private String processPendingApprovalsWhenPatientHasOnePendingApprovalEachForMultipleFields(boolean shouldAccept) {
        String healthId = patientRepository.create(data).getId();

        PatientData patientData = initPatientData();
        patientData.setGender("F");
        patientData.setOccupation("09");
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("22334455");
        patientData.setPhoneNumber(phoneNumber);
        patientRepository.update(patientData, healthId);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(3, pendingApprovals.size());

        assertPendingApprovalMappings(healthId, data.getAddress(), pendingApprovals);

        patientData = initPatientData();
        patientData.setHealthId(healthId);
        patientData.setGender("F");
        patientData.setPhoneNumber(phoneNumber);
        PatientData existingPatientData = patientRepository.findByHealthId(healthId);
        return patientRepository.processPendingApprovals(patientData, existingPatientData, shouldAccept);
    }

    @Test
    public void shouldBeAbleToAcceptPendingApprovalsWhenPatientHasMultiplePendingApprovalsForMultipleFields() throws Exception {
        String healthId = processPendingApprovalsWhenPatientHasMultiplePendingApprovalsForMultipleFields(true);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertEquals("F", patient.getGender());
        assertEquals(data.getOccupation(), patient.getOccupation());

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        PendingApproval pendingApproval = pendingApprovals.iterator().next();
        assertNotNull(pendingApproval);
        assertEquals(OCCUPATION, pendingApproval.getName());

        assertPendingApprovalMappings(healthId, data.getAddress(), pendingApprovals);
    }

    @Test
    public void shouldBeAbleToRejectPendingApprovalsWhenPatientHasMultiplePendingApprovalsForMultipleFields() throws Exception {
        String healthId = processPendingApprovalsWhenPatientHasMultiplePendingApprovalsForMultipleFields(false);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertEquals(data.getGender(), patient.getGender());
        assertEquals(data.getOccupation(), patient.getOccupation());

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(2, pendingApprovals.size());

        for (PendingApproval pendingApproval : pendingApprovals) {
            if (GENDER.equals(pendingApproval.getName())) {
                assertNotNull(pendingApproval.getFieldDetails());
                Collection<PendingApprovalFieldDetails> fieldDetails = pendingApproval.getFieldDetails().values();
                assertNotNull(fieldDetails);
                assertEquals(1, fieldDetails.size());
                assertEquals("O", fieldDetails.iterator().next().getValue());

            } else if (OCCUPATION.equals(pendingApproval.getName())) {
                assertNotNull(pendingApproval.getFieldDetails());
                Collection<PendingApprovalFieldDetails> fieldDetails = pendingApproval.getFieldDetails().values();
                assertNotNull(fieldDetails);
                assertEquals(2, fieldDetails.size());
                for (PendingApprovalFieldDetails fieldDetail : fieldDetails) {
                    assertTrue(asList("05", "06").contains(fieldDetail.getValue()));
                }

            } else {
                fail("Invalid pending approval.");
            }
        }

        assertPendingApprovalMappings(healthId, data.getAddress(), pendingApprovals);
    }

    private String processPendingApprovalsWhenPatientHasMultiplePendingApprovalsForMultipleFields(boolean shouldAccept) throws Exception {
        String healthId = patientRepository.create(data).getId();

        PatientData patientData = initPatientData();
        patientData.setGender("F");
        patientData.setOccupation("05");
        patientRepository.update(patientData, healthId);
        Thread.sleep(0, 10);

        patientData = initPatientData();
        patientData.setGender("O");
        patientData.setOccupation("06");
        patientRepository.update(patientData, healthId);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(2, pendingApprovals.size());

        for (PendingApproval pendingApproval : pendingApprovals) {
            TreeMap<UUID, PendingApprovalFieldDetails> fieldDetails = pendingApproval.getFieldDetails();
            assertNotNull(fieldDetails);
            assertEquals(2, fieldDetails.size());
        }

        assertPendingApprovalMappings(healthId, data.getAddress(), pendingApprovals);

        patientData = initPatientData();
        patientData.setHealthId(healthId);
        patientData.setGender("F");
        PatientData existingPatientData = patientRepository.findByHealthId(healthId);
        return patientRepository.processPendingApprovals(patientData, existingPatientData, shouldAccept);
    }

    @Test
    public void shouldBeAbleToAcceptPendingApprovalsWhenPatientHasBlockPendingApprovals() {
        String healthId = processPendingApprovalsWhenPatientHasBlockPendingApprovals(true);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertEquals("", defaultString(patient.getPhoneNumberCountryCode()));
        assertEquals("011", defaultString(patient.getPhoneNumberAreaCode()));
        assertEquals("10002001", defaultString(patient.getCellNo()));
        assertEquals("", defaultString(patient.getPhoneNumberExtension()));

        assertTrue(isEmpty(patient.getPendingApprovals()));
        assertEquals(0, findAllPendingApprovalMappings().size());
    }

    @Test
    public void shouldBeAbleToRejectPendingApprovalsWhenPatientHasBlockPendingApprovals() {
        String healthId = processPendingApprovalsWhenPatientHasBlockPendingApprovals(false);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertEquals("91", patient.getPhoneNumberCountryCode());
        assertEquals("080", patient.getPhoneNumberAreaCode());
        assertEquals("10002000", patient.getCellNo());
        assertEquals("999", patient.getPhoneNumberExtension());

        assertTrue(isEmpty(patient.getPendingApprovals()));
        assertEquals(0, findAllPendingApprovalMappings().size());
    }


    private String processPendingApprovalsWhenPatientHasBlockPendingApprovals(boolean shouldAccept) {
        PatientData p = data;
        PhoneNumber phoneNo = new PhoneNumber();
        phoneNo.setCountryCode("91");
        phoneNo.setAreaCode("080");
        phoneNo.setNumber("10002000");
        phoneNo.setExtension("999");
        p.setPhoneNumber(phoneNo);
        String healthId = patientRepository.create(p).getId();

        PatientData patientData = initPatientData();
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setAreaCode("011");
        phoneNumber.setNumber("10002001");
        patientData.setPhoneNumber(phoneNumber);
        patientRepository.update(patientData, healthId);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        assertPendingApprovalMappings(healthId, data.getAddress(), pendingApprovals);

        patientData = initPatientData();
        patientData.setHealthId(healthId);
        patientData.setPhoneNumber(phoneNumber);
        PatientData existingPatientData = patientRepository.findByHealthId(healthId);
        return patientRepository.processPendingApprovals(patientData, existingPatientData, shouldAccept);
    }

    @Test
    public void shouldUpdatePendingApprovalMappingWhenUpdateAddressRequestIsApproved() throws Exception {
        Address existingAddress = createAddress("10", "20", "30");
        existingAddress.setCityCorporationId("40");
        PatientData existingPatient = initPatientData();
        existingPatient.setGivenName("John");
        existingPatient.setSurName("Doe");
        existingPatient.setOccupation("01");
        existingPatient.setAddress(existingAddress);
        String healthId = patientRepository.create(existingPatient).getId();
        assertNotNull(healthId);

        Address newAddress = createAddress("11", "22", "33");
        PatientData updateRequest = initPatientData();
        updateRequest.setOccupation("02");
        updateRequest.setAddress(newAddress);
        patientRepository.update(updateRequest, healthId);
        assertPendingApprovalMappings(existingPatient.getCatchment().getAllIds());

        PatientData approvalRequest = initPatientData();
        approvalRequest.setHealthId(healthId);
        approvalRequest.setAddress(newAddress);
        patientRepository.processPendingApprovals(approvalRequest, patientRepository.findByHealthId(healthId), true);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newAddress, updatedPatient.getAddress());
        assertPendingApprovalMappings(updatedPatient.getCatchment().getAllIds());
    }

    @Test
    public void shouldNotUpdatePendingApprovalMappingWhenUpdateAddressRequestIsRejected() throws Exception {
        Address existingAddress = createAddress("10", "20", "30");
        existingAddress.setCityCorporationId("40");
        PatientData existingPatient = initPatientData();
        existingPatient.setGivenName("John");
        existingPatient.setSurName("Doe");
        existingPatient.setOccupation("01");
        existingPatient.setAddress(existingAddress);
        String healthId = patientRepository.create(existingPatient).getId();
        assertNotNull(healthId);

        Address newAddress = createAddress("11", "22", "33");
        PatientData updateRequest = initPatientData();
        updateRequest.setGivenName("Jane");
        updateRequest.setOccupation("02");
        updateRequest.setAddress(newAddress);
        patientRepository.update(updateRequest, healthId);
        assertPendingApprovalMappings(existingPatient.getCatchment().getAllIds());

        PatientData approvalRequest = initPatientData();
        approvalRequest.setHealthId(healthId);
        approvalRequest.setAddress(newAddress);
        patientRepository.processPendingApprovals(approvalRequest, patientRepository.findByHealthId(healthId), false);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(existingAddress, updatedPatient.getAddress());
        assertPendingApprovalMappings(existingPatient.getCatchment().getAllIds());
    }

    private void assertPendingApprovalMappings(List<String> catchmentIds) {
        List<PendingApprovalMapping> mappings = findAllPendingApprovalMappings();
        assertEquals(catchmentIds.size(), mappings.size());
        for (PendingApprovalMapping mapping : mappings) {
            assertTrue(catchmentIds.contains(mapping.getCatchmentId()));
        }
    }

    private void assertHouseholdCodeMappingEmpty() {
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_HOUSEHOLD_CODE_MAPPING).toString(), HouseholdCodeMapping.class)));
    }

    private PatientData initPatientData() {
        PatientData patient = new PatientData();
        patient.setRequester(FACILITY, null);
        patient.setHealthId(String.valueOf(new Date().getTime()));
        return patient;
    }

    private PatientData initPatientData(PatientData data) {
        PatientData patient = data;
        patient.setRequester(FACILITY, null);
        patient.setHealthId(String.valueOf(new Date().getTime()));
        return patient;
    }

    @After
    public void tearDown() {
        truncateAllColumnFamilies(cassandraOps);
    }
}
