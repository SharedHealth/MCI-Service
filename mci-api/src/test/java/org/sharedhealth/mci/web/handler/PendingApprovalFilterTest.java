package org.sharedhealth.mci.web.handler;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sharedhealth.mci.domain.exception.NonUpdatableFieldUpdateException;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.service.ApprovalFieldService;
import org.sharedhealth.mci.domain.service.PendingApprovalFilter;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;

import java.text.ParseException;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;
import static org.sharedhealth.mci.domain.constant.MCIConstants.*;
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
        updateRequest.setRelations(asList(createRelation("SPS", "Mehzabin")));
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
        updateRequest.setRelations(asList(createRelation("SPS", "Mehzabin")));
        updateRequest.setRequester("Bahmni", "Dr. Monika");

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        verify(approvalFieldService, atLeastOnce()).getProperty(RELATIONS);
        assertPendingApprovals(existingPatient, newPatient, 1, updateRequest.getRequester());

        updateRequest = buildPatientData();
        updateRequest.setRelations(asList(createRelation("FTH", "Kareem")));

        newPatient = pendingApprovalFilter.filter(newPatient, updateRequest);
        TreeSet<PendingApproval> pendingApprovals = newPatient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        PendingApproval pendingApproval = pendingApprovals.iterator().next();
        assertEquals(RELATIONS, pendingApproval.getName());

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
        assertNotNull(fieldDetailsMap);
        assertEquals(2, fieldDetailsMap.size());

        assertTrue(containsRelationFieldDetails(fieldDetailsMap, "SPS", "Mehzabin"));
        assertTrue(containsRelationFieldDetails(fieldDetailsMap, "FTH", "Kareem"));

        verify(approvalFieldService, Mockito.times(2)).getProperty(RELATIONS);
    }

    @Test
    public void shouldNotAddEmptyRelationsToNewPatientData() throws Exception {
        PatientData existingPatient = buildPatientData();

        UUID motherRelationId = UUID.randomUUID();
        UUID fatherRelationId = UUID.randomUUID();

        PatientData updateRequest = buildPatientData();

        Relation motherRelation = new Relation();
        motherRelation.setId(motherRelationId.toString());
        motherRelation.setType(RELATION_MOTHER);

        Relation fatherRelation = new Relation();
        fatherRelation.setId(fatherRelationId.toString());
        fatherRelation.setType(RELATION_FATHER);

        updateRequest.setRelations(asList(motherRelation, fatherRelation));
        updateRequest.setRequester("Bahmni", "Dr. Monika");

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        assertTrue(CollectionUtils.isEmpty(newPatient.getRelations()));
    }

    @Test
    public void shouldAddEmptyRelationsWhenThereSameRelationAlreadyExist() throws Exception {
        PatientData existingPatient = buildPatientData();
        String motherRelationId = UUID.randomUUID().toString();
        String fatherRelationId = UUID.randomUUID().toString();

        Relation existingMotherRelation = createRelation(RELATION_MOTHER, "Nagma");
        existingMotherRelation.setId(motherRelationId);
        Relation existingFatherRelation = createRelation(RELATION_FATHER, "Nagmo");
        existingFatherRelation.setId(fatherRelationId);
        existingPatient.setRelations(asList(existingMotherRelation, existingFatherRelation));

        PatientData updateRequest = new PatientData();

        Relation motherRelation = new Relation();
        motherRelation.setId(motherRelationId);
        motherRelation.setType(RELATION_MOTHER);

        Relation fatherRelation = new Relation();
        fatherRelation.setId(fatherRelationId);
        fatherRelation.setType(RELATION_FATHER);

        updateRequest.setRelations(asList(motherRelation, fatherRelation));
        updateRequest.setRequester("Bahmni", "Dr. Monika");

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        List<Relation> relations = newPatient.getRelations();
        assertEquals(2, relations.size());
        assertTrue(relations.get(0).isEmpty());
        assertTrue(relations.get(1).isEmpty());
    }

    @Test
    public void shouldAddRelationsWhenThereIsAnUpdate() throws Exception {
        PatientData existingPatient = buildPatientData();
        String motherRelationId = UUID.randomUUID().toString();
        String fatherRelationId = UUID.randomUUID().toString();

        Relation existingMotherRelation = createRelation(RELATION_MOTHER, "Nagma");
        existingMotherRelation.setId(motherRelationId);
        Relation existingFatherRelation = createRelation(RELATION_FATHER, "Nazmul");
        existingFatherRelation.setId(fatherRelationId);
        existingPatient.setRelations(asList(existingMotherRelation, existingFatherRelation));

        PatientData updateRequest = new PatientData();

        Relation motherRelation = createRelation(RELATION_MOTHER, "Pogo");
        motherRelation.setId(motherRelationId);
        motherRelation.setType(RELATION_MOTHER);

        Relation fatherRelation = createRelation(RELATION_FATHER, "Ramlal");
        fatherRelation.setId(fatherRelationId);
        fatherRelation.setType(RELATION_FATHER);

        updateRequest.setRelations(asList(motherRelation, fatherRelation));
        updateRequest.setRequester("Bahmni", "Dr. Monika");

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        assertEquals(2, newPatient.getRelations().size());
        assertEquals(fatherRelation, newPatient.getRelationById(fatherRelationId));
        assertEquals(motherRelation, newPatient.getRelationById(motherRelationId));
    }

    @Test
    public void shouldIgnoreUpdateRequestForHIDCardStatusIfEmpty() throws Exception {
        PatientData existingPatient = new PatientData();
        PatientData updateRequest = new PatientData();
        updateRequest.setHidCardStatus("");

        existingPatient.setHidCardStatus(HID_CARD_STATUS_REGISTERED);
        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        assertEquals(existingPatient.getHidCardStatus(), newPatient.getHidCardStatus());

        existingPatient.setHidCardStatus(null);
        newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        assertEquals(existingPatient.getHidCardStatus(), newPatient.getHidCardStatus());

        existingPatient.setHidCardStatus(HID_CARD_STATUS_ISSUED);
        newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        assertEquals(existingPatient.getHidCardStatus(), newPatient.getHidCardStatus());
    }

    @Test
    public void shouldIgnoreUpdateRequestForHIDCardStatusIfExistingStatusIsIssued() throws Exception {
        PatientData existingPatient = new PatientData();
        existingPatient.setHidCardStatus(HID_CARD_STATUS_ISSUED);
        PatientData updateRequest = new PatientData();

        updateRequest.setHidCardStatus("");
        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        assertEquals(existingPatient.getHidCardStatus(), newPatient.getHidCardStatus());

        updateRequest.setHidCardStatus(null);
        newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        assertEquals(existingPatient.getHidCardStatus(), newPatient.getHidCardStatus());

        updateRequest.setHidCardStatus(HID_CARD_STATUS_REGISTERED);
        newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        assertEquals(existingPatient.getHidCardStatus(), newPatient.getHidCardStatus());
    }

    private boolean containsRelationFieldDetails(TreeMap<UUID, PendingApprovalFieldDetails> relations, String type, String givenName) {
        for (PendingApprovalFieldDetails pendingApprovalFieldDetails : relations.values()) {
            List<Relation> relationList = (List<Relation>) pendingApprovalFieldDetails.getValue();
            if (createRelation(type, givenName).equals(relationList.get(0))) return true;
        }
        return false;
    }

    private void assertRelationFieldDetails(TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap, Relation value, Requester requestedBy) {
        assertNotNull(fieldDetailsMap);
        assertEquals(1, fieldDetailsMap.size());
        PendingApprovalFieldDetails fieldDetails = fieldDetailsMap.values().iterator().next();
        List<Relation> relations = (List<Relation>) fieldDetails.getValue();

        assertEquals(value.getType(), relations.get(0).getType());
        assertEquals(requestedBy, fieldDetails.getRequestedBy());
        long expectedCreatedAt = TimeUuidUtil.getTimeFromUUID(fieldDetailsMap.keySet().iterator().next());
        assertEquals(toIsoMillisFormat(expectedCreatedAt), fieldDetails.getCreatedAt());
    }

    private Relation createRelation(String type, String givenName) {
        Relation relation = new Relation();
        relation.setType(type);
        relation.setNameBangla("মেহজাবীন খান");
        relation.setGivenName(givenName);
        relation.setSurName("Khan");
        relation.setNationalId("1990567890163");
        relation.setUid("38761111111");
        relation.setBirthRegistrationNumber("52345678901633456");
        relation.setMarriageId("12345678");
        relation.setRelationalStatus("3");
        return relation;
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
        long expectedCreatedAt = TimeUuidUtil.getTimeFromUUID(fieldDetailsMap.keySet().iterator().next());
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
}