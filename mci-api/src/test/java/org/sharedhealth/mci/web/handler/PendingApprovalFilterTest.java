package org.sharedhealth.mci.web.handler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.service.ApprovalFieldService;
import org.sharedhealth.mci.domain.service.PendingApprovalFilter;
import org.sharedhealth.mci.web.exception.NonUpdatableFieldUpdateException;

import java.text.ParseException;
import java.util.*;

import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;
import static org.sharedhealth.mci.domain.util.DateUtil.toIsoMillisFormat;

public class PendingApprovalFilterTest {

    @Mock
    private ApprovalFieldService approvalFieldService;

    private PendingApprovalFilter pendingApprovalFilter;

    @Before
    public void setUp() {
        initMocks(this);
        pendingApprovalFilter = new PendingApprovalFilter(approvalFieldService);
    }

    private void setUpApprovalFieldServiceFor(String key, String value) {
        Mockito.when(approvalFieldService.getProperty(key)).thenReturn(value);
    }

    @Test
    public void shouldAddFieldsToPendingApprovalsWhenMarkedForApproval() throws ParseException {
        setUpApprovalFieldServiceFor(GENDER, "NA");
        setUpApprovalFieldServiceFor(DATE_OF_DEATH, "NA");
        setUpApprovalFieldServiceFor(OCCUPATION, "NA");
        setUpApprovalFieldServiceFor(RELIGION, "NA");

        PatientData existingPatient = buildPatientData();
        existingPatient.setGender("M");
        existingPatient.setReligion("1");

        PatientData updateRequest = buildPatientData();
        updateRequest.setGender("F");
        updateRequest.setReligion("2");
        updateRequest.setRequester("Bahmni", "Dr. Monika");

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        verify(approvalFieldService, atLeastOnce()).getProperty(GENDER);
        assertPendingApprovals(existingPatient, newPatient, 2, updateRequest.getRequester());
    }

    @Test
    public void shouldAddFieldsToExistingPendingApprovalsWhenMarkedForApproval() throws ParseException {
        setUpApprovalFieldServiceFor(GENDER, "NA");

        PatientData updateRequest = buildPatientData();
        updateRequest.setGender("F");
        updateRequest.setRequester("Bahmni", "Dr. Monika");

        PatientData existingPatient = buildPatientData();
        existingPatient.setGender("M");


        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        assertPendingApprovals(existingPatient, newPatient, 1, updateRequest.getRequester());

        updateRequest = buildPatientData();
        updateRequest.setGender("O");

        newPatient = pendingApprovalFilter.filter(newPatient, updateRequest);
        TreeSet<PendingApproval> pendingApprovals = newPatient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        PendingApproval pendingApproval = pendingApprovals.iterator().next();
        assertEquals(GENDER, pendingApproval.getName());

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
        assertNotNull(fieldDetailsMap);
        assertEquals(2, fieldDetailsMap.size());

        for (PendingApprovalFieldDetails fieldDetails : fieldDetailsMap.values()) {
            assertTrue(asList("F", "O").contains(fieldDetails.getValue()));
        }
        assertEquals(existingPatient.getGender(), newPatient.getGender());

        verify(approvalFieldService, Mockito.times(2)).getProperty(GENDER);
    }

    @Test
    public void shouldAddFieldsToPendingApprovalsWhenMarkedForApprovalAndExistingValueIsNull() throws ParseException {
        setUpApprovalFieldServiceFor(GENDER, "NA");
        setUpApprovalFieldServiceFor(PHONE_NUMBER, "NA");
        setUpApprovalFieldServiceFor(PRESENT_ADDRESS, "NA");
        setUpApprovalFieldServiceFor(PERMANENT_ADDRESS, "NA");

        PatientData existingPatient = buildPatientData();
        existingPatient.setGender(null);
        existingPatient.setPhoneNumber(null);
        existingPatient.setAddress(null);
        existingPatient.setPermanentAddress(null);

        PatientData updateRequest = buildPatientData();
        updateRequest.setGender("F");
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("123");
        updateRequest.setPhoneNumber(phoneNumber);
        Address presentAddress = new Address("10", "20", "30");
        updateRequest.setAddress(presentAddress);
        Address permanentAddress = new Address("11", "22", "33");
        updateRequest.setPermanentAddress(permanentAddress);
        updateRequest.setRequester("Bahmni", "Dr. Monika");

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        verify(approvalFieldService).getProperty(GENDER);
        verify(approvalFieldService).getProperty(PHONE_NUMBER);
        verify(approvalFieldService).getProperty(PRESENT_ADDRESS);
        verify(approvalFieldService).getProperty(PERMANENT_ADDRESS);

        assertPendingApprovals(existingPatient, newPatient, 4, updateRequest.getRequester());
    }

    @Test
    public void shouldAddFieldsToPendingApprovalsWhenMarkedForApprovalButValueSameAsExisting() throws Exception {
        setUpApprovalFieldServiceFor(PHONE_NUMBER, "NA");
        setUpApprovalFieldServiceFor(PRESENT_ADDRESS, "NA");
        setUpApprovalFieldServiceFor(PERMANENT_ADDRESS, "NA");

        PatientData existingPatient = buildPatientData();

        PhoneNumber existingPhoneNumber = new PhoneNumber();
        existingPhoneNumber.setCountryCode("");
        existingPhoneNumber.setAreaCode("");
        existingPhoneNumber.setNumber("123");
        existingPhoneNumber.setExtension("");
        existingPatient.setPhoneNumber(existingPhoneNumber);

        Address existingPresentAddress = new Address("10", "20", "30");
        existingPresentAddress.setCityCorporationId("");
        existingPresentAddress.setUnionOrUrbanWardId("");
        existingPresentAddress.setRuralWardId("");
        existingPatient.setAddress(existingPresentAddress);

        Address existingPermanentAddress = new Address("11", "22", "33");
        existingPermanentAddress.setCityCorporationId("");
        existingPermanentAddress.setUnionOrUrbanWardId("");
        existingPermanentAddress.setRuralWardId("");
        existingPatient.setPermanentAddress(existingPermanentAddress);

        PatientData updateRequest = buildPatientData();
        PhoneNumber phoneNumberToBeUpdated = new PhoneNumber();
        phoneNumberToBeUpdated.setNumber("123");
        updateRequest.setPhoneNumber(phoneNumberToBeUpdated);
        updateRequest.setAddress(new Address("10", "20", "30"));
        updateRequest.setPermanentAddress(new Address("11", "22", "33"));

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        verify(approvalFieldService).getProperty(PHONE_NUMBER);
        verify(approvalFieldService).getProperty(PRESENT_ADDRESS);
        verify(approvalFieldService).getProperty(PERMANENT_ADDRESS);

        assertTrue(isEmpty(newPatient.getPendingApprovals()));
        assertEquals(existingPatient.getPhoneNumber(), newPatient.getPhoneNumber());
        assertEquals(existingPatient.getAddress(), newPatient.getAddress());
        assertEquals(existingPatient.getPermanentAddress(), newPatient.getPermanentAddress());
    }

    @Test(expected = NonUpdatableFieldUpdateException.class)
    public void shouldThrowExceptionWhenTryingToUpdateNonUpdatableField() throws ParseException {
        setUpApprovalFieldServiceFor(DATE_OF_BIRTH, "NU");

        PatientData existingPatient = buildPatientData();
        existingPatient.setDateOfBirth(parseDate("2000-02-10"));

        PatientData updateRequest = buildPatientData();
        updateRequest.setDateOfBirth(parseDate("2000-02-11"));

        pendingApprovalFilter.filter(existingPatient, updateRequest);

        verify(approvalFieldService).getProperty(DATE_OF_BIRTH);
    }

    @Test
    public void shouldNotAddFieldsToPendingApprovalsWhenNotMarkedNonUpdatable() throws ParseException {
        setUpApprovalFieldServiceFor(DATE_OF_BIRTH, "NU");

        PatientData existingPatient = buildPatientData();
        existingPatient.setDateOfBirth(parseDate("2000-02-10"));

        PatientData updateRequest = buildPatientData();
        updateRequest.setDateOfBirth(parseDate("2000-02-10"));

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        verify(approvalFieldService).getProperty(DATE_OF_BIRTH);

        assertTrue(isEmpty(newPatient.getPendingApprovals()));
        assertEquals(existingPatient.getDateOfBirth(), newPatient.getDateOfBirth());
    }

    @Test
    public void shouldUpdateFieldsThatAreNeitherMarkedForApprovalNorMarkedAsNonUpdatable() throws ParseException {
        PatientData existingPatient = buildPatientData();
        String oldDateOfBirth = "2000-02-10T12:10:18.382+06:00";
        existingPatient.setDateOfBirth(parseDate(oldDateOfBirth));

        PatientData updateRequest = buildPatientData();
        String newDateOfBith = "2001-02-10T12:10:18.382+06:00";
        updateRequest.setDateOfBirth(parseDate(newDateOfBith));

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        assertTrue(isEmpty(newPatient.getPendingApprovals()));
        assertEquals(toIsoMillisFormat(newDateOfBith), toIsoMillisFormat(newPatient.getDateOfBirth()));

        verify(approvalFieldService, atLeastOnce()).getProperty(Mockito.anyString());
    }

    private void assertPendingApprovals(PatientData existingPatient, PatientData newPatient, int pendingApprovalsCount, Requester requestedBy) {
        TreeSet<PendingApproval> pendingApprovals = newPatient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(pendingApprovalsCount, pendingApprovals.size());

        for (PendingApproval pendingApproval : pendingApprovals) {
            if (GENDER.equals(pendingApproval.getName())) {
                assertFieldDetails(pendingApproval.getFieldDetails(), "F", requestedBy);
                assertEquals(existingPatient.getGender(), newPatient.getGender());

            } else if (PHONE_NUMBER.equals(pendingApproval.getName())) {
                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setNumber("123");
                assertFieldDetails(pendingApproval.getFieldDetails(), phoneNumber, requestedBy);
                assertEquals(existingPatient.getPhoneNumber(), newPatient.getPhoneNumber());

            } else if (PRESENT_ADDRESS.equals(pendingApproval.getName())) {
                assertFieldDetails(pendingApproval.getFieldDetails(), new Address("10", "20", "30"), requestedBy);
                assertEquals(existingPatient.getAddress(), newPatient.getAddress());

            } else if (PERMANENT_ADDRESS.equals(pendingApproval.getName())) {
                assertFieldDetails(pendingApproval.getFieldDetails(), new Address("11", "22", "33"), requestedBy);
                assertEquals(existingPatient.getPermanentAddress(), newPatient.getPermanentAddress());

            } else if (RELIGION.equals(pendingApproval.getName())) {
                assertFieldDetails(pendingApproval.getFieldDetails(), "2", requestedBy);
                assertEquals(existingPatient.getReligion(), newPatient.getReligion());

            } else if (RELATIONS.equals(pendingApproval.getName())) {
                Relation relation = new Relation();
                relation.setType("SPS");
                assertRelationFieldDetails(pendingApproval.getFieldDetails(), relation, requestedBy);

            } else {
                fail("Invalid pending approval.");
            }
        }
    }

    private void assertFieldDetails(TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap, Object value, Requester requestedBy) {
        assertNotNull(fieldDetailsMap);
        assertEquals(1, fieldDetailsMap.size());
        PendingApprovalFieldDetails fieldDetails = fieldDetailsMap.values().iterator().next();
        assertEquals(value, fieldDetails.getValue());
        assertEquals(requestedBy, fieldDetails.getRequestedBy());
        long expectedCreatedAt = unixTimestamp(fieldDetailsMap.keySet().iterator().next());
        assertEquals(toIsoMillisFormat(expectedCreatedAt), fieldDetails.getCreatedAt());
    }

    private PatientData buildPatientData() throws ParseException {
        PatientData patient = new PatientData();
        patient.setNationalId("1234567890123");
        patient.setBirthRegistrationNumber("12345678901234567");
        patient.setGivenName("Scott");
        patient.setSurName("Tiger");
        patient.setGender("M");
        patient.setDateOfBirth(parseDate("2014-12-01"));

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazilaId("09");
        address.setCityCorporationId("20");
        address.setVillage("10");
        address.setRuralWardId("01");
        address.setCountryCode("050");

        patient.setAddress(address);

        Address presentAddress = new Address();
        presentAddress.setAddressLine("house-10");
        presentAddress.setDivisionId("10");
        presentAddress.setDistrictId("04");
        presentAddress.setUpazilaId("09");
        presentAddress.setCityCorporationId("20");
        presentAddress.setVillage("10");
        presentAddress.setRuralWardId("01");
        presentAddress.setCountryCode("050");

        patient.setPermanentAddress(presentAddress);

        LocationData location = new LocationData();

        location.setGeoCode("1004092001");
        location.setDivisionId("10");
        location.setDistrictId("04");
        location.setUpazilaId("09");
        location.setCityCorporationId("20");
        location.setUnionOrUrbanWardId("01");

        patient.setRequester("Bahmni", null);
        return patient;
    }

    @Test
    public void shouldNotAddFieldsToPendingApprovalsWhenMarkedForApprovalAndRequestByAdmin() throws ParseException {
        setUpApprovalFieldServiceFor(GENDER, "NA");
        setUpApprovalFieldServiceFor(DATE_OF_DEATH, "NA");
        setUpApprovalFieldServiceFor(OCCUPATION, "NA");
        setUpApprovalFieldServiceFor(RELIGION, "NA");

        PatientData existingPatient = buildPatientData();
        existingPatient.setGender("M");
        existingPatient.setReligion("1");

        PatientData updateRequest = buildPatientData();
        updateRequest.setGender("F");
        updateRequest.setReligion("2");
        updateRequest.setRequester("Bahmni", "Dr. Monika", "Mr. Admin");

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        assertTrue(isEmpty(newPatient.getPendingApprovals()));
        assertEquals(newPatient.getGender(), "F");
        assertEquals(newPatient.getReligion(), "2");
    }

    @Test
    public void shouldAddRelationUpdateToPendingApprovalsWhenMarkedForApproval() throws ParseException {
        setUpApprovalFieldServiceFor(RELATIONS, "NA");

        PatientData existingPatient = buildPatientData();

        PatientData updateRequest = buildPatientData();
        updateRequest.setRelations(getRelations());
        updateRequest.setRequester("Bahmni", "Dr. Monika");

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        verify(approvalFieldService, atLeastOnce()).getProperty(RELATIONS);
        assertPendingApprovals(existingPatient, newPatient, 1, updateRequest.getRequester());
    }

    @Test
    public void shouldAddRelationFieldsToExistingPendingApprovalsWhenMarkedForApproval() throws ParseException {
        setUpApprovalFieldServiceFor(RELATIONS, "NA");

        PatientData existingPatient = buildPatientData();

        PatientData updateRequest = buildPatientData();
        updateRequest.setRelations(getRelations());
        updateRequest.setRequester("Bahmni", "Dr. Monika");

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        verify(approvalFieldService, atLeastOnce()).getProperty(RELATIONS);
        assertPendingApprovals(existingPatient, newPatient, 1, updateRequest.getRequester());

        updateRequest = buildPatientData();
        updateRequest.setRelations(getRelations());

        newPatient = pendingApprovalFilter.filter(newPatient, updateRequest);
        TreeSet<PendingApproval> pendingApprovals = newPatient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        PendingApproval pendingApproval = pendingApprovals.iterator().next();
        assertEquals(RELATIONS, pendingApproval.getName());

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
        assertNotNull(fieldDetailsMap);
        assertEquals(2, fieldDetailsMap.size());

        PendingApprovalFieldDetails fieldDetails = fieldDetailsMap.values().iterator().next();
        List<Relation> relations  = (List<Relation>) fieldDetails.getValue();

        assertEquals("SPS", relations.get(0).getType());

        verify(approvalFieldService, Mockito.times(2)).getProperty(RELATIONS);
    }

    private void assertRelationFieldDetails(TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap, Relation value, Requester requestedBy) {
        assertNotNull(fieldDetailsMap);
        assertEquals(1, fieldDetailsMap.size());
        PendingApprovalFieldDetails fieldDetails = fieldDetailsMap.values().iterator().next();
        List<Relation> relations  = (List<Relation>) fieldDetails.getValue();

        assertEquals(value.getType(), relations.get(0).getType());
        assertEquals(requestedBy, fieldDetails.getRequestedBy());
        long expectedCreatedAt = unixTimestamp(fieldDetailsMap.keySet().iterator().next());
        assertEquals(toIsoMillisFormat(expectedCreatedAt), fieldDetails.getCreatedAt());
    }

    private List<Relation> getRelations() {
        Relation relation = new Relation();
        relation.setType("SPS");
        relation.setNameBangla("মেহজাবীন খান");
        relation.setGivenName("Mehzabin");
        relation.setSurName("Khan");
        relation.setNationalId("1990567890163");
        relation.setUid("38761111111");
        relation.setBirthRegistrationNumber("52345678901633456");
        relation.setMarriageId("12345678");
        relation.setRelationalStatus("3");

        List<Relation> relations = new ArrayList<>();
        relations.add(relation);
        return relations;
    }

}