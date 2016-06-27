package org.sharedhealth.mci.web.service;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.config.EnvironmentMock;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.sharedhealth.mci.domain.util.BaseRepositoryIT;
import org.sharedhealth.mci.domain.util.TestUtil;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.domain.constant.JsonConstants.PHONE_NUMBER;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;
import static org.sharedhealth.mci.domain.util.TestUtil.setupApprovalsConfig;
import static org.sharedhealth.mci.domain.util.TestUtil.truncateAllColumnFamilies;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientServiceIT extends BaseRepositoryIT {

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
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @After
    public void tearDown() {
        truncateAllColumnFamilies(cassandraOps);
    }

    @Test
    public void shouldUpdatePatient() throws Exception {
        PatientData data = buildPatient();
        MCIResponse mciResponseForCreate = patientRepository.create(data);
        String healthId = mciResponseForCreate.getId();
        data.setHealthId(healthId);
        data.setGivenName("Danny");

        MCIResponse mciResponseForUpdate = patientService.update(data, data.getHealthId());

        assertEquals(202, mciResponseForUpdate.getHttpStatus());
        PatientData savedPatient = patientService.findByHealthId(healthId);

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
    public void shouldNotMappingsWhenExistingValueIsNullAndNewValueIsNull() {
        String existingReligion = "01";

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId(null);
        patient.setBirthRegistrationNumber(null);
        patient.setUid(null);
        patient.setReligion(existingReligion);
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(null);
        patient.setHouseholdCode(null);
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertMappingsEmpty();

        String newReligion = "02";
        PatientData updateRequest = initPatientData();
        updateRequest.setReligion(newReligion);
        updateRequest.setNationalId(null);
        updateRequest.setBirthRegistrationNumber(null);
        updateRequest.setUid(null);
        patient.setHouseholdCode(null);
        updateRequest.setPhoneNumber(null);

        patientService.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newReligion, updatedPatient.getReligion());
        assertNull(updatedPatient.getNationalId());
        assertNull(updatedPatient.getBirthRegistrationNumber());
        assertNull(updatedPatient.getUid());
        assertNull(updatedPatient.getPhoneNumber());
        assertNull(updatedPatient.getHouseholdCode());

        assertTrue(isEmpty(updatedPatient.getPendingApprovals()));
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_PENDING_APPROVAL_MAPPING).toString(), PendingApprovalMapping.class)));

        assertMappingsEmpty();
    }

    @Test
    public void shouldNotUpdateMappingsWhenExistingValueIsNullAndNewValueIsEmpty() {
        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId(null);
        patient.setBirthRegistrationNumber(null);
        patient.setUid(null);
        patient.setPhoneNumber(null);
        patient.setHouseholdCode(null);
        patient.setAddress(createAddress("10", "20", "30"));

        String healthId = patientRepository.create(patient).getId();

        assertNotNull(healthId);
        assertMappingsEmpty();

        PhoneNumber newPhoneNumber = new PhoneNumber();
        newPhoneNumber.setCountryCode("");
        newPhoneNumber.setAreaCode("");
        newPhoneNumber.setNumber("");
        newPhoneNumber.setExtension("");
        PatientData updateRequest = initPatientData();
        updateRequest.setNationalId("");
        updateRequest.setBirthRegistrationNumber("");
        updateRequest.setUid("");
        updateRequest.setPhoneNumber(newPhoneNumber);
        updateRequest.setHouseholdCode("");
        patientService.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals("", updatedPatient.getNationalId());
        assertEquals("", updatedPatient.getBirthRegistrationNumber());
        assertEquals("", updatedPatient.getUid());
        assertEquals(newPhoneNumber, updatedPatient.getPhoneNumber());
        assertEquals("", updatedPatient.getHouseholdCode());

        assertMappingsEmpty();
    }

    @Test
    public void shouldUpdateIdAndPhoneNumberMappingsWhenExistingValueIsNullAndNewValueIsNotEmpty() {
        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId(null);
        patient.setBirthRegistrationNumber(null);
        patient.setUid(null);
        patient.setHouseholdCode(null);
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(null);

        String healthId = patientRepository.create(patient).getId();

        assertNotNull(healthId);
        assertMappingsEmpty();

        String newNid = "1000000000000";
        String newBrn = "10000000000000000";
        String newUid = "10000000000";
        String newHouseHoldCode = "1234";
        PhoneNumber newPhoneNumber = new PhoneNumber();
        newPhoneNumber.setCountryCode("91");
        newPhoneNumber.setAreaCode("80");
        newPhoneNumber.setNumber("10002000");
        newPhoneNumber.setExtension("");

        PatientData updateRequest = initPatientData();
        updateRequest.setNationalId(newNid);
        updateRequest.setBirthRegistrationNumber(newBrn);
        updateRequest.setUid(newUid);
        updateRequest.setPhoneNumber(newPhoneNumber);
        updateRequest.setHouseholdCode(newHouseHoldCode);

        patientService.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newNid, updatedPatient.getNationalId());
        assertEquals(newBrn, updatedPatient.getBirthRegistrationNumber());
        assertEquals(newUid, updatedPatient.getUid());
        assertEquals(newPhoneNumber, updatedPatient.getPhoneNumber());
        assertEquals(newHouseHoldCode, updatedPatient.getHouseholdCode());

        assertSearchByNid(newNid, healthId);
        assertSearchByBrn(newBrn, healthId);
        assertSearchByUid(newUid, healthId);
        assertSearchByPhoneNumber(newPhoneNumber, healthId);
        assertSearchByHouseholdCode(newHouseHoldCode, healthId);

    }

    @Test
    public void shouldNotUpdateMappingsWhenExistingValueIsEmptyAndNewValueIsNull() {
        PhoneNumber existingPhoneNumber = new PhoneNumber();
        existingPhoneNumber.setCountryCode("");
        existingPhoneNumber.setAreaCode("");
        existingPhoneNumber.setNumber("");
        existingPhoneNumber.setExtension("");

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId("");
        patient.setBirthRegistrationNumber("");
        patient.setUid("");
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(existingPhoneNumber);
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertMappingsEmpty();

        PatientData updateRequest = initPatientData();
        updateRequest.setNationalId(null);
        updateRequest.setBirthRegistrationNumber(null);
        updateRequest.setUid(null);
        updateRequest.setPhoneNumber(null);

        patientService.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals("", updatedPatient.getNationalId());
        assertEquals("", updatedPatient.getBirthRegistrationNumber());
        assertEquals("", updatedPatient.getUid());
        assertEquals(existingPhoneNumber, updatedPatient.getPhoneNumber());
        assertTrue(isEmpty(updatedPatient.getPendingApprovals()));

        assertMappingsEmpty();
    }

    @Test
    public void shouldUpdateIdAndPhoneNumberMappingsWhenExistingValueIsEmptyAndNewValueIsNotEmpty() {
        PhoneNumber existingPhoneNumber = new PhoneNumber();
        existingPhoneNumber.setCountryCode("");
        existingPhoneNumber.setAreaCode("");
        existingPhoneNumber.setNumber("");
        existingPhoneNumber.setExtension("");

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId("");
        patient.setBirthRegistrationNumber("");
        patient.setUid("");
        patient.setHouseholdCode("");
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(existingPhoneNumber);
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        assertMappingsEmpty();

        String newNid = "1000000000000";
        String newBrn = "10000000000000000";
        String newUid = "10000000000";
        String newHouseHoldCode = "1234";
        PhoneNumber newPhoneNumber = new PhoneNumber();
        newPhoneNumber.setCountryCode("91");
        newPhoneNumber.setAreaCode("80");
        newPhoneNumber.setNumber("10002000");
        newPhoneNumber.setExtension("");

        PatientData updateRequest = initPatientData();
        updateRequest.setNationalId(newNid);
        updateRequest.setBirthRegistrationNumber(newBrn);
        updateRequest.setUid(newUid);
        updateRequest.setHouseholdCode(newHouseHoldCode);
        updateRequest.setPhoneNumber(newPhoneNumber);
        patientService.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newNid, updatedPatient.getNationalId());
        assertEquals(newBrn, updatedPatient.getBirthRegistrationNumber());
        assertEquals(newUid, updatedPatient.getUid());
        assertEquals(newHouseHoldCode, updatedPatient.getHouseholdCode());
        assertEquals(newPhoneNumber, updatedPatient.getPhoneNumber());

        assertSearchByNid(newNid, healthId);
        assertSearchByBrn(newBrn, healthId);
        assertSearchByUid(newUid, healthId);
        assertSearchByPhoneNumber(newPhoneNumber, healthId);
        assertSearchByHouseholdCode(newHouseHoldCode, healthId);
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
        patientService.update(updateRequest, healthId);

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
        patientService.update(updateRequest, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(existingGivenName, updatedPatient.getGivenName());
        assertEquals(newAddress, updatedPatient.getAddress());

        assertSearchByNameAndAddressEmpty(existingGivenName, "102030");
        assertSearchByNameAndAddressExists(existingGivenName, "112233", healthId);
    }

    @Test
    public void shouldCreateAsManyPendingApprovalMappingsAsNumberOfPossibleCatchments() {
        setupApprovalsConfig(cassandraOps);

        PatientData createPatientData = buildPatient();
        Address address = createAddress("10", "20", "30");
        address.setCityCorporationId("40");
        address.setUnionOrUrbanWardId("50");
        address.setRuralWardId("60");
        createPatientData.setAddress(address);
        String healthId = patientRepository.create(createPatientData).getId();

        PatientData patientData = initPatientData();
        patientData.setGender("F");
        patientService.update(patientData, healthId);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        assertPendingApprovalMappings(healthId, address, pendingApprovals);
    }

    @Test
    public void shouldAddPendingApprovalInPatientAndApprovalMappingTables_IfPatientHasAnyPendingApproval() throws Exception {
        setupApprovalsConfig(cassandraOps);

        PatientData data = buildPatient();
        String healthId = patientRepository.create(initPatientData(data)).getId();

        PatientData updateRequest = initPatientData();
        updateRequest.setGender("F");
        updateRequest.setRequester(FACILITY, "Dr. Monika");

        patientService.update(updateRequest, healthId);

        updateRequest = initPatientData();
        updateRequest.setGender("O");
        updateRequest.setRequester(FACILITY, "Dr. Seuss");
        patientService.update(updateRequest, healthId);
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
        setupApprovalsConfig(cassandraOps);

        PatientData data = buildPatient();
        String healthId = patientRepository.create(data).getId();

        PatientData patientData = initPatientData();
        patientData.setGivenName("John Doe");
        patientData.setGender("O");
        patientData.setOccupation("07");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("40000000777");
        patientData.setPhoneNumber(phoneNumber);

        patientService.update(patientData, healthId);

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
    public void shouldBeAbleToAcceptPendingApprovalsWhenPatientHasOnePendingApprovalEachForMultipleFields() {
        TestUtil.setupApprovalsConfig(cassandraOps);

        PatientData data = buildPatient();
        String healthId = processPendingApprovalsWhenPatientHasOnePendingApprovalEachForMultipleFields(data, true);

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
        TestUtil.setupApprovalsConfig(cassandraOps);

        PatientData data = buildPatient();
        String healthId = processPendingApprovalsWhenPatientHasOnePendingApprovalEachForMultipleFields(data, false);

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

    @Test
    public void shouldBeAbleToAcceptPendingApprovalsWhenPatientHasMultiplePendingApprovalsForMultipleFields() throws Exception {
        TestUtil.setupApprovalsConfig(cassandraOps);

        PatientData data = buildPatient();
        String healthId = processPendingApprovalsWhenPatientHasMultiplePendingApprovalsForMultipleFields(data, true);

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
        TestUtil.setupApprovalsConfig(cassandraOps);

        PatientData data = buildPatient();
        String healthId = processPendingApprovalsWhenPatientHasMultiplePendingApprovalsForMultipleFields(data, false);

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

    @Test
    public void shouldBeAbleToAcceptPendingApprovalsWhenPatientHasBlockPendingApprovals() {
        TestUtil.setupApprovalsConfig(cassandraOps);

        PatientData data = buildPatient();
        String healthId = processPendingApprovalsWhenPatientHasBlockPendingApprovals(data, true);

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
        PatientData data = buildPatient();
        String healthId = processPendingApprovalsWhenPatientHasBlockPendingApprovals(data, false);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertEquals("91", patient.getPhoneNumberCountryCode());
        assertEquals("080", patient.getPhoneNumberAreaCode());
        assertEquals("10002000", patient.getCellNo());
        assertEquals("999", patient.getPhoneNumberExtension());

        assertTrue(isEmpty(patient.getPendingApprovals()));
        assertEquals(0, findAllPendingApprovalMappings().size());
    }
    @Test
    public void shouldUpdatePendingApprovalMappingWhenUpdateAddressRequestIsApproved() throws Exception {
        TestUtil.setupApprovalsConfig(cassandraOps);

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
        patientService.update(updateRequest, healthId);
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
        TestUtil.setupApprovalsConfig(cassandraOps);

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
        patientService.update(updateRequest, healthId);
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

//
//    @Test
//    public void shouldNotUpdatePatientIfThereAreNoDifferenceInPatientData() throws Exception {
//        Address existingAddress = createAddress("10", "20", "30");
//        existingAddress.setCityCorporationId("40");
//        PatientData patient = initPatientData();
//        patient.setGivenName("John");
//        patient.setSurName("Doe");
//        patient.setOccupation("01");
//        patient.setAddress(existingAddress);
//        String healthId = patientRepository.create(patient).getId();
//        assertNotNull(healthId);
//
//        Patient savedPatient = cassandraOps.selectOneById(Patient.class, healthId);
//        assertNotNull(savedPatient.getUpdatedAt());
//
//        patientRepository.update(patient, healthId);
//
//        Patient updatedPatient = cassandraOps.selectOneById(Patient.class, healthId);
//        assertNotNull(updatedPatient.getUpdatedAt());
//
//        assertEquals(updatedPatient.getUpdatedAt(), savedPatient.getUpdatedAt());
//    }
//
//    @Test
//    public void shouldNotUpdatePatientIfExistingAndNewPatientDataHasDefaultValues() throws Exception {
//        Address existingAddress = createAddress("10", "20", "30");
//        existingAddress.setCityCorporationId("40");
//        PatientData patient = initPatientData();
//        patient.setGivenName("John");
//        patient.setSurName("Doe");
//        patient.setOccupation("01");
//        patient.setAddress(existingAddress);
//        String healthId = patientRepository.create(patient).getId();
//        assertNotNull(healthId);
//
//        Patient savedPatient = cassandraOps.selectOneById(Patient.class, healthId);
//        assertNotNull(savedPatient.getUpdatedAt());
//
//        PatientStatus patientStatus = new PatientStatus();
//        patientStatus.setType(MCIConstants.PATIENT_STATUS_ALIVE);
//        patient.setPatientStatus(patientStatus);
//        patient.setDobType(PatientMapper.DEFAULT_DOB_TYPE);
//        patient.setConfidential(MCIConstants.STRING_NO);
//
//        patient.getAddress().setCountryCode(null);
//
//        patientRepository.update(patient, healthId);
//
//        Patient updatedPatient = cassandraOps.selectOneById(Patient.class, healthId);
//        assertNotNull(updatedPatient.getUpdatedAt());
//
//        assertEquals(updatedPatient.getUpdatedAt(), savedPatient.getUpdatedAt());
//    }
//
//    private void assertPendingApprovalMappings(List<String> catchmentIds) {
//        List<PendingApprovalMapping> mappings = findAllPendingApprovalMappings();
//        assertEquals(catchmentIds.size(), mappings.size());
//        for (PendingApprovalMapping mapping : mappings) {
//            assertTrue(catchmentIds.contains(mapping.getCatchmentId()));
//        }
//    }
//
//    private void assertHouseholdCodeMappingEmpty() {
//        assertTrue(isEmpty(cassandraOps.select(select().from(CF_HOUSEHOLD_CODE_MAPPING).toString(), HouseholdCodeMapping.class)));
//    }

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

    private void assertPendingApprovalMappings(List<String> catchmentIds) {
        List<PendingApprovalMapping> mappings = findAllPendingApprovalMappings();
        assertEquals(catchmentIds.size(), mappings.size());
        for (PendingApprovalMapping mapping : mappings) {
            assertTrue(catchmentIds.contains(mapping.getCatchmentId()));
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

    private void assertMappingsEmpty() {
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_NID_MAPPING).toString(), NidMapping.class)));
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_BRN_MAPPING).toString(), BrnMapping.class)));
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_UID_MAPPING).toString(), UidMapping.class)));
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_PHONE_NUMBER_MAPPING).toString(), PhoneNumberMapping.class)));
        assertTrue(isEmpty(cassandraOps.select(select().from(CF_HOUSEHOLD_CODE_MAPPING).toString(), HouseholdCodeMapping.class)));
    }

    private String processPendingApprovalsWhenPatientHasOnePendingApprovalEachForMultipleFields(PatientData data, boolean shouldAccept) {
        TestUtil.setupApprovalsConfig(cassandraOps);

        String healthId = patientRepository.create(data).getId();

        PatientData patientData = initPatientData();
        patientData.setGender("F");
        patientData.setOccupation("09");
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("22334455");
        patientData.setPhoneNumber(phoneNumber);
        patientService.update(patientData, healthId);

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

    private String processPendingApprovalsWhenPatientHasBlockPendingApprovals(PatientData data, boolean shouldAccept) {
        PhoneNumber phoneNo = new PhoneNumber();
        phoneNo.setCountryCode("91");
        phoneNo.setAreaCode("080");
        phoneNo.setNumber("10002000");
        phoneNo.setExtension("999");
        data.setPhoneNumber(phoneNo);
        String healthId = patientRepository.create(data).getId();

        PatientData patientData = initPatientData();
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setAreaCode("011");
        phoneNumber.setNumber("10002001");
        patientData.setPhoneNumber(phoneNumber);
        patientService.update(patientData, healthId);

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

    private String processPendingApprovalsWhenPatientHasMultiplePendingApprovalsForMultipleFields(PatientData data, boolean shouldAccept) throws Exception {
        TestUtil.setupApprovalsConfig(cassandraOps);

        String healthId = patientRepository.create(data).getId();

        PatientData patientData = initPatientData();
        patientData.setGender("F");
        patientData.setOccupation("05");
        patientService.update(patientData, healthId);

        patientData = initPatientData();
        patientData.setGender("O");
        patientData.setOccupation("06");
        patientService.update(patientData, healthId);

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


}