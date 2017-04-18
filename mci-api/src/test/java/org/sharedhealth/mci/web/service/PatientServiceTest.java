package org.sharedhealth.mci.web.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.repository.PatientFeedRepository;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.sharedhealth.mci.domain.service.PendingApprovalFilter;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.sharedhealth.mci.web.exception.InsufficientPrivilegeException;
import org.sharedhealth.mci.web.mapper.PendingApprovalListResponse;
import org.springframework.http.HttpStatus;

import java.util.*;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;

public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PatientFeedRepository feedRepository;
    @Mock
    private SettingService settingService;
    @Mock
    private HealthIdService healthIdService;
    @Mock
    private MCIResponse mciResponse;
    @Mock
    private PendingApprovalFilter pendingApprovalFilter;

    private PatientService patientService;
    private String expectedMessage;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        patientService = new PatientService(patientRepository, feedRepository, settingService, healthIdService, pendingApprovalFilter);
    }

    @Test
    public void shouldCreateNewPatient() throws Exception {
        PatientData patient = new PatientData();
        when(healthIdService.getNextHealthId()).thenReturn("FUBAR");

        when(mciResponse.getHttpStatus()).thenReturn(201);
        when(patientRepository.create(patient)).thenReturn(mciResponse);

        patientService.createPatientForMCI(patient);
        InOrder inOrder = inOrder(patientRepository);
        inOrder.verify(patientRepository).create(patient);

        verify(healthIdService).getNextHealthId();
    }

    @Test
    public void shouldMarkHidUsedIfCretePatientIsSuccessful() throws Exception {
        PatientData existingPatient = new PatientData();
        String hid = "FUBAR";
        when(healthIdService.getNextHealthId()).thenReturn(hid);
        when(mciResponse.getHttpStatus()).thenReturn(201);
        when(patientRepository.create(existingPatient)).thenReturn(mciResponse);

        patientService.createPatientForMCI(existingPatient);
        InOrder inOrder = inOrder(patientRepository);
        inOrder.verify(patientRepository).create(existingPatient);

        verify(healthIdService).getNextHealthId();
        verify(healthIdService, times(0)).putBackHealthId(any(String.class));
    }

    @Test
    public void shouldReturnHidToHidBlockIfCretePatientFailed() throws Exception {
        PatientData existingPatient = new PatientData();
        String hid = "FUBAR";

        when(healthIdService.getNextHealthId()).thenReturn(hid);
        when(mciResponse.getHttpStatus()).thenReturn(403);
        when(patientRepository.create(existingPatient)).thenReturn(mciResponse);

        patientService.createPatientForMCI(existingPatient);
        InOrder inOrder = inOrder(patientRepository);
        inOrder.verify(patientRepository).create(existingPatient);

        verify(healthIdService).getNextHealthId();
        verify(healthIdService, times(1)).putBackHealthId(hid);
    }

    @Test
    public void shouldUpdateInsteadOfCreatingWhenMatchingPatientExists() throws Exception {
        PatientData existingPatient = new PatientData();
        String healthId = "hid-100";
        existingPatient.setHealthId(healthId);
        existingPatient.setNationalId("nid-100");
        existingPatient.setBirthRegistrationNumber("brn-100");

        SearchQuery searchByNidQuery = new SearchQuery();
        searchByNidQuery.setNid("nid-100");
        when(patientRepository.findAllByQuery(searchByNidQuery)).thenReturn(asList(existingPatient));

        SearchQuery searchByBrnQuery = new SearchQuery();
        searchByBrnQuery.setBin_brn("brn-100");
        when(patientRepository.findAllByQuery(searchByBrnQuery)).thenReturn(asList(existingPatient));

        PatientData requestData = new PatientData();
        requestData.setNationalId("nid-100");
        requestData.setBirthRegistrationNumber("brn-100");

        when(patientRepository.findByHealthId(healthId)).thenReturn(existingPatient);

        PatientData newPatientData = new PatientData();
        newPatientData.setHouseholdCode("1234");
        when(pendingApprovalFilter.filter(existingPatient, requestData)).thenReturn(newPatientData);

        patientService.createPatientForMCI(requestData);
        InOrder inOrder = inOrder(patientRepository);
        inOrder.verify(patientRepository).findAllByQuery(searchByNidQuery);
        inOrder.verify(patientRepository).findAllByQuery(searchByBrnQuery);
        inOrder.verify(patientRepository, times(2)).findByHealthId(healthId);
        inOrder.verify(patientRepository).update(eq(newPatientData), eq(existingPatient), any(Requester.class));
        inOrder.verify(patientRepository, never()).create(any(PatientData.class));
    }

    @Test
    public void shouldCreatePatientForGivenOrganization() throws Exception {
        String clientId = "12345";
        String healthId = "12312";
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);
        mciResponse = new MCIResponse(healthId, HttpStatus.CREATED);

        when(patientRepository.create(patient)).thenReturn(mciResponse);
        when(healthIdService.validateHIDForOrg(healthId, clientId)).thenReturn(getAvailability(true, null));

        MCIResponse mciResponse = patientService.createPatientForOrg(patient, clientId);

        assertEquals(healthId, mciResponse.getId());
        assertEquals(HttpStatus.CREATED.value(), mciResponse.getHttpStatus());
        InOrder inOrder = inOrder(patientRepository, healthIdService);
        inOrder.verify(healthIdService).validateHIDForOrg(healthId, clientId);
        inOrder.verify(patientRepository).create(patient);
    }

    @Test
    public void shouldNotCreatePatientsWithSameHealthID() throws Exception {
        String clientId = "12345";
        String healthId = "12312";
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);

        when(patientRepository.patientExists(healthId)).thenReturn(true);

        MCIResponse mciResponse = patientService.createPatientForOrg(patient, clientId);

        assertEquals(HttpStatus.CONFLICT.value(), mciResponse.getHttpStatus());
        assertEquals("Patient with healthId " + healthId + " already exits." , mciResponse.getId());
        verify(patientRepository, times(1)).patientExists(healthId);
        verify(healthIdService, never()).validateHIDForOrg(healthId, clientId);
        verify(patientRepository, never()).create(patient);
    }

    @Test
    public void shouldNotCreatePatientWhenHIDServiceGivesUnavailable() throws Exception {
        String clientId = "12345";
        String healthId = "12312";
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);
        String expectedMessage = "The HealthId is not for given organization";

        when(healthIdService.validateHIDForOrg(healthId, clientId)).thenReturn(getAvailability(false, expectedMessage));
        MCIResponse mciResponse = patientService.createPatientForOrg(patient, clientId);

        assertEquals(expectedMessage, mciResponse.getId());
        assertEquals(HttpStatus.BAD_REQUEST.value(), mciResponse.getHttpStatus());

        InOrder inOrder = inOrder(patientRepository, healthIdService);
        inOrder.verify(healthIdService).validateHIDForOrg(healthId, clientId);
        inOrder.verify(patientRepository, never()).create(patient);
    }

    @Test
    public void shouldFindPatientsByCatchmentWithoutDateAndLastMarker() {
        Catchment catchment = new Catchment("30", "26", "18");
        Date since = null;
        UUID lastMarker = null;
        int limit = 25;
        PatientData patient = new PatientData();
        String healthId = "h101";
        patient.setHealthId(healthId);

        HashMap<String, Object> catchmentEventMap = new HashMap<>();
        catchmentEventMap.put("patientData", catchmentEventMap);

        when(settingService.getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT")).thenReturn(limit);
        when(patientRepository.findAllByCatchment(catchment, since, lastMarker, limit)).thenReturn(Arrays.<Map<String, Object>>asList(catchmentEventMap));

        List<Map<String, Object>> catchmentsEvents = patientService.findAllByCatchment(catchment, since, lastMarker);

        verify(settingService).getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT");
        verify(patientRepository).findAllByCatchment(catchment, since, lastMarker, limit);

        assertNotNull(catchmentsEvents);
        assertEquals(1, catchmentsEvents.size());
        assertEquals(healthId, patient.getHealthId());
    }

    @Test
    public void shouldFindPatientsByCatchment() {
        Catchment catchment = new Catchment("30", "26", "18");
        Date since = new Date();
        UUID lastMarker = TimeUuidUtil.uuidForDate(new Date());
        int limit = 25;
        PatientData patient = new PatientData();
        String healthId = "h101";
        patient.setHealthId(healthId);

        HashMap<String, Object> catchmentEventMap = new HashMap<>();
        catchmentEventMap.put("patientData", patient);

        when(settingService.getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT")).thenReturn(limit);
        when(patientRepository.findAllByCatchment(catchment, since, lastMarker, limit)).thenReturn(Arrays.<Map<String, Object>>asList(catchmentEventMap));

        List<Map<String, Object>> catchmentEvents = patientService.findAllByCatchment(catchment, since, lastMarker);

        verify(settingService).getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT");
        verify(patientRepository).findAllByCatchment(catchment, since, lastMarker, limit);

        assertNotNull(catchmentEvents);
        assertEquals(1, catchmentEvents.size());
        assertEquals(healthId, patient.getHealthId());
    }

    @Test
    public void shouldFindPendingApprovalListByCatchment() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        UUID after = TimeUuidUtil.uuidForDate(new Date());

        List<PendingApprovalMapping> mappings = asList(buildPendingApprovalMapping("hid-100"),
                buildPendingApprovalMapping("hid-200"),
                buildPendingApprovalMapping("hid-300"));

        when(patientRepository.findPendingApprovalMapping(catchment, after, null, 25)).thenReturn(mappings);
        when(patientRepository.findByHealthId("hid-100")).thenReturn(buildPatient("hid-100"));
        when(patientRepository.findByHealthId("hid-200")).thenReturn(buildPatient("hid-200"));
        when(patientRepository.findByHealthId("hid-300")).thenReturn(buildPatient("hid-300"));
        when(settingService.getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT")).thenReturn(25);

        List<PendingApprovalListResponse> pendingApprovals = patientService.findPendingApprovalList(catchment, after, null, 25);

        InOrder inOrder = inOrder(patientRepository);
        inOrder.verify(patientRepository).findPendingApprovalMapping(catchment, after, null, 25);
        inOrder.verify(patientRepository).findByHealthId("hid-100");
        inOrder.verify(patientRepository).findByHealthId("hid-200");
        inOrder.verify(patientRepository).findByHealthId("hid-300");

        assertNotNull(pendingApprovals);
        assertEquals(3, pendingApprovals.size());

        PendingApprovalListResponse pendingApproval = pendingApprovals.iterator().next();
        String healthId = pendingApproval.getHealthId();
        assertNotNull("hid-100", healthId);
        assertEquals("Scott-" + healthId, pendingApproval.getGivenName());
        assertEquals("Tiger-" + healthId, pendingApproval.getSurname());
        assertEquals(mappings.get(0).getLastUpdated(), pendingApproval.getLastUpdated());
    }

    @Test(expected = InsufficientPrivilegeException.class)
    public void shouldNotFindPendingApprovalDetailsThatDoesNotBelongToGivenCatchment() {
        String healthId = "healthId-100";
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        Address address = new Address();
        address.setDivisionId("10");
        address.setDistrictId("20");
        patient.setAddress(address);

        when(patientRepository.findByHealthId(healthId)).thenReturn(patient);
        Catchment catchment = new Catchment("11", "22", "33");
        patientService.findPendingApprovalDetails(healthId, catchment);
    }

    @Test
    public void shouldFindPendingApprovalsByHealthId() throws Exception {
        String healthId = "healthId-100";
        PatientData patient = new PatientData();
        patient.setGivenName("Harry");
        patient.setSurName("Potter");
        patient.setBloodGroup("As if I care!");
        patient.setOccupation("Wizard");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setCountryCode("91");
        phoneNumber.setAreaCode("033");
        phoneNumber.setNumber("20001234");
        patient.setPhoneNumber(phoneNumber);

        Address address = new Address();
        address.setDivisionId("10");
        address.setDistrictId("20");
        address.setUpazilaId("30");
        patient.setAddress(address);

        List<UUID> uuids = generateUUIDs();
        TreeSet<PendingApproval> pendingApprovals = buildPendingApprovalRequestMap(uuids, phoneNumber, address);
        patient.setPendingApprovals(pendingApprovals);

        when(patientRepository.findByHealthId(healthId)).thenReturn(patient);
        Catchment catchment = new Catchment("10", "20");
        TreeSet<PendingApproval> actualResponse = patientService.findPendingApprovalDetails(healthId, catchment);
        verify(patientRepository).findByHealthId(healthId);

        assertEquals(6, actualResponse.size());
        for (int i = 0; i < 6; i++) {
            PendingApproval lhs = pendingApprovals.iterator().next();
            PendingApproval rhs = actualResponse.iterator().next();
            assertTrue(reflectionEquals(lhs, rhs));
        }
    }

    @Test
    public void shouldProcessPendingApprovalWhenAllFieldsMarkedForApprovalAreAccepted() throws Exception {
        PatientData patient = new PatientData();
        patient.setHealthId("hid-100");
        patient.setGivenName("Happy Rotter");
        patient.setGender("F");
        Address address = new Address("1", "2", "3");
        address.setAddressLine("house no. 10");
        patient.setAddress(address);

        Catchment catchment = new Catchment("1", "2", "3");

        PatientData existingPatient = new PatientData();
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApproval(GIVEN_NAME, "Happy Rotter"));
        pendingApprovals.add(buildPendingApproval(GENDER, "F"));
        pendingApprovals.add(buildPendingApproval(PRESENT_ADDRESS, address));
        existingPatient.setPendingApprovals(pendingApprovals);
        existingPatient.setAddress(address);

        when(patientRepository.findByHealthId("hid-100")).thenReturn(existingPatient);
        patientService.processPendingApprovals(patient, catchment, true);

        verify(patientRepository).processPendingApprovals(patient, existingPatient, true);
    }

    @Test
    public void shouldProcessPendingApprovalWhenSomeOfTheFieldsMarkedForApprovalAreAccepted() throws Exception {
        PatientData patient = new PatientData();
        patient.setHealthId("hid-100");
        patient.setGivenName("Happy Rotter");
        Address address = new Address("1", "2", "3");
        address.setAddressLine("house no. 10");
        patient.setAddress(address);

        Catchment catchment = new Catchment("1", "2", "3");

        PatientData existingPatient = new PatientData();
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApproval(GIVEN_NAME, "Happy Rotter"));
        pendingApprovals.add(buildPendingApproval(GENDER, "O"));
        pendingApprovals.add(buildPendingApproval(PRESENT_ADDRESS, address));
        existingPatient.setPendingApprovals(pendingApprovals);
        existingPatient.setAddress(address);

        when(patientRepository.findByHealthId("hid-100")).thenReturn(existingPatient);
        patientService.processPendingApprovals(patient, catchment, true);

        verify(patientRepository).processPendingApprovals(patient, existingPatient, true);
    }

    @Test(expected = InsufficientPrivilegeException.class)
    public void shouldNotProcessPendingApprovalsThatDoesNotBelongToGivenCatchment() {
        PatientData patient = new PatientData();
        patient.setHealthId("hid-100");
        Address address = new Address("10", "20", "30");
        address.setAddressLine("house no. 10");
        patient.setAddress(address);

        Catchment catchment = new Catchment("10", "20");
        catchment.setUpazilaId("40");

        PatientData existingPatient = new PatientData();
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApproval(GIVEN_NAME, "Happy Rotter"));
        existingPatient.setAddress(address);
        existingPatient.setPendingApprovals(pendingApprovals);

        when(patientRepository.findByHealthId("hid-100")).thenReturn(existingPatient);
        patientService.processPendingApprovals(patient, catchment, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptPendingApprovalWhenExistingPendingApprovalForFieldNameDoesNotExist() {
        PatientData patient = new PatientData();
        patient.setHealthId("hid-100");
        patient.setGivenName("Happy Rotter");
        patient.setGender("F");
        Address address = new Address("1", "2", "3");
        address.setAddressLine("house no. 10");
        patient.setAddress(address);

        Catchment catchment = new Catchment("1", "2", "3");

        PatientData existingPatient = new PatientData();
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApproval(GIVEN_NAME, "Happy Rotter"));
        existingPatient.setPendingApprovals(pendingApprovals);

        when(patientRepository.findByHealthId("hid-100")).thenReturn(existingPatient);
        patientService.processPendingApprovals(patient, catchment, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptPendingApprovalWhenNoExistingPendingApproval() throws Exception {
        PatientData patient = new PatientData();
        patient.setHealthId("hid-100");
        patient.setGivenName("Happy Rotter");

        Catchment catchment = new Catchment("1", "2", "3");
        PatientData existingPatient = new PatientData();
        when(patientRepository.findByHealthId("hid-100")).thenReturn(existingPatient);

        patientService.processPendingApprovals(patient, catchment, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptPendingApprovalWhenExistingPendingApprovalsDoNotMatch() throws Exception {
        PatientData patient = new PatientData();
        patient.setHealthId("hid-100");
        patient.setGivenName("Happy Rotter");

        Catchment catchment = new Catchment("1", "2", "3");

        PatientData existingPatient = new PatientData();
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApproval(GIVEN_NAME, "Harry Potter"));
        existingPatient.setPendingApprovals(pendingApprovals);

        when(patientRepository.findByHealthId("hid-100")).thenReturn(existingPatient);
        patientService.processPendingApprovals(patient, catchment, true);
    }

    @Test
    public void shouldProcessPendingApprovalWhenRejected() throws Exception {
        PatientData patient = new PatientData();
        patient.setHealthId("hid-100");
        patient.setGivenName("Happy Rotter");
        patient.setGender("F");
        Address address = new Address("1", "2", "3");
        address.setAddressLine("house no. 10");
        patient.setAddress(address);

        Catchment catchment = new Catchment("1", "2", "3");

        PatientData existingPatient = new PatientData();
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApproval(GIVEN_NAME, "Happy Rotter"));
        pendingApprovals.add(buildPendingApproval(GENDER, "F"));
        pendingApprovals.add(buildPendingApproval(PRESENT_ADDRESS, address));
        existingPatient.setPendingApprovals(pendingApprovals);
        existingPatient.setAddress(address);

        when(patientRepository.findByHealthId("hid-100")).thenReturn(existingPatient);
        patientService.processPendingApprovals(patient, catchment, false);

        verify(patientRepository).processPendingApprovals(patient, existingPatient, false);
    }

    private PendingApproval buildPendingApproval(String fieldName, Object value) {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(fieldName);
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
        fieldDetails.setValue(value);
        fieldDetailsMap.put(TimeUuidUtil.uuidForDate(new Date()), fieldDetails);
        pendingApproval.addFieldDetails(fieldDetailsMap);
        return pendingApproval;
    }

    @Test
    public void shouldReturnFalseWhenPatientDoesNotHaveMultipleIds() {
        PatientData patient = new PatientData();
        patient.setNationalId("100");
        patient.setBirthRegistrationNumber("");
        patient.setUid(null);
        assertFalse(patientService.containsMultipleIds(patient));
    }

    @Test
    public void shouldReturnTrueWhenPatientHasNidAndBrn() {
        PatientData patient = new PatientData();
        patient.setNationalId("100");
        patient.setBirthRegistrationNumber("200");
        patient.setUid(null);
        assertTrue(patientService.containsMultipleIds(patient));
    }

    @Test
    public void shouldReturnTrueWhenPatientHasNidAndUid() {
        PatientData patient = new PatientData();
        patient.setNationalId("100");
        patient.setBirthRegistrationNumber(null);
        patient.setUid("300");
        assertTrue(patientService.containsMultipleIds(patient));
    }

    @Test
    public void shouldReturnTrueWhenPatientHasBrnAndUid() {
        PatientData patient = new PatientData();
        patient.setNationalId(null);
        patient.setBirthRegistrationNumber("200");
        patient.setUid("300");
        assertTrue(patientService.containsMultipleIds(patient));
    }

    @Test
    public void shouldFindPatientMatchingMultipleIdsWhenPatientExistWithNidAndBrn() {
        String healthId = "h100";
        String nid = "n100";
        String brn = "b100";
        String uid = "u100";
        PatientData requestData = new PatientData();
        requestData.setHealthId(healthId);
        requestData.setNationalId(nid);
        requestData.setBirthRegistrationNumber(brn);
        requestData.setUid(uid);

        SearchQuery query = new SearchQuery();
        query.setNid(nid);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h101"), buildPatient("h102"), buildPatient("h103")));

        query = new SearchQuery();
        query.setBin_brn(brn);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h101"), buildPatient("h104"), buildPatient("h105")));

        query = new SearchQuery();
        query.setUid(uid);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h106"), buildPatient("h107"), buildPatient("h108")));

        PatientData patient = patientService.findPatientByMultipleIds(requestData);
        assertNotNull(patient);
        assertEquals("h101", patient.getHealthId());
    }

    @Test
    public void shouldFindPatientMatchingMultipleIdsWhenPatientExistWithBrnAndUid() {
        String healthId = "h100";
        String nid = "n100";
        String brn = "b100";
        String uid = "u100";
        PatientData requestData = new PatientData();
        requestData.setHealthId(healthId);
        requestData.setNationalId(nid);
        requestData.setBirthRegistrationNumber(brn);
        requestData.setUid(uid);

        SearchQuery query = new SearchQuery();
        query.setNid(nid);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h101"), buildPatient("h102"), buildPatient("h103")));

        query = new SearchQuery();
        query.setBin_brn(brn);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h104"), buildPatient("h105"), buildPatient("h106")));

        query = new SearchQuery();
        query.setUid(uid);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h106"), buildPatient("h107"), buildPatient("h108")));

        PatientData patient = patientService.findPatientByMultipleIds(requestData);
        assertNotNull(patient);
        assertEquals("h106", patient.getHealthId());
    }

    @Test
    public void shouldFindPatientMatchingMultipleIdsWhenPatientExistWithNidAndUid() {
        String healthId = "h100";
        String nid = "n100";
        String brn = "b100";
        String uid = "u100";
        PatientData requestData = new PatientData();
        requestData.setHealthId(healthId);
        requestData.setNationalId(nid);
        requestData.setBirthRegistrationNumber(brn);
        requestData.setUid(uid);

        SearchQuery query = new SearchQuery();
        query.setNid(nid);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h101"), buildPatient("h102"), buildPatient("h103")));

        query = new SearchQuery();
        query.setBin_brn(brn);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h104"), buildPatient("h105"), buildPatient("h106")));

        query = new SearchQuery();
        query.setUid(uid);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h101"), buildPatient("h107"), buildPatient("h108")));

        PatientData patient = patientService.findPatientByMultipleIds(requestData);
        assertNotNull(patient);
        assertEquals("h101", patient.getHealthId());
    }

    @Test
    public void shouldNotFindPatientMatchingMultipleIdsWhenMultipleIdsDoNotExist() {
        String healthId = "h100";
        String nid = "n100";
        PatientData requestData = new PatientData();
        requestData.setHealthId(healthId);
        requestData.setNationalId(nid);

        PatientData patient = patientService.findPatientByMultipleIds(requestData);
        assertNull(patient);
    }

    @Test
    public void shouldNotFindPatientMatchingMultipleIdsWhenNoPatientExistWithAnyNidAndBrn() {
        String healthId = "h100";
        String nid = "n100";
        String brn = "b100";
        String uid = "u100";
        PatientData requestData = new PatientData();
        requestData.setHealthId(healthId);
        requestData.setNationalId(nid);
        requestData.setBirthRegistrationNumber(brn);
        requestData.setUid(uid);

        SearchQuery query = new SearchQuery();
        query.setNid(nid);
        when(patientRepository.findAllByQuery(query)).thenReturn(new ArrayList<PatientData>());

        query = new SearchQuery();
        query.setBin_brn(brn);
        when(patientRepository.findAllByQuery(query)).thenReturn(null);

        query = new SearchQuery();
        query.setUid(uid);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h107"), buildPatient("h108"), buildPatient("h109")));

        PatientData patient = patientService.findPatientByMultipleIds(requestData);
        assertNull(patient);
    }

    @Test
    public void shouldNotFindPatientMatchingMultipleIdsWhenNoPatientExistWithAnyId() {
        String healthId = "h100";
        String nid = "n100";
        String brn = "b100";
        String uid = "u100";
        PatientData requestData = new PatientData();
        requestData.setHealthId(healthId);
        requestData.setNationalId(nid);
        requestData.setBirthRegistrationNumber(brn);
        requestData.setUid(uid);

        SearchQuery query = new SearchQuery();
        query.setNid(nid);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h101"), buildPatient("h102"), buildPatient("h103")));

        query = new SearchQuery();
        query.setBin_brn(brn);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h104"), buildPatient("h105"), buildPatient("h106")));

        query = new SearchQuery();
        query.setUid(uid);
        when(patientRepository.findAllByQuery(query)).thenReturn(asList(buildPatient("h107"), buildPatient("h108"), buildPatient("h109")));

        PatientData patient = patientService.findPatientByMultipleIds(requestData);
        assertNull(patient);
    }

    @Test
    public void shouldFindUpdateLogsUpdatedSince() {
        UUID eventId = TimeUuidUtil.uuidForDate(new Date());
        Date since = new Date(TimeUuidUtil.getTimeFromUUID(eventId));
        int limit = 25;
        PatientUpdateLog patientLog = new PatientUpdateLog();
        patientLog.setHealthId("h101");
        patientLog.setEventId(eventId);

        when(settingService.getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT")).thenReturn(limit);
        when(feedRepository.findPatientsUpdatedSince(since, limit, null)).thenReturn(asList(patientLog));

        List<PatientUpdateLog> patientLogs = patientService.findPatientsUpdatedSince(since, null);

        verify(settingService).getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT");
        verify(feedRepository).findPatientsUpdatedSince(since, limit, null);

        assertNotNull(patientLogs);
        assertEquals(1, patientLogs.size());
        assertEquals(eventId, patientLog.getEventId());
    }

    @Test
    public void shouldFindUpdateLogsUpdatedAfterLastMarker() {
        UUID eventId = TimeUuidUtil.uuidForDate(new Date());
        int limit = 25;
        PatientUpdateLog patientLog = new PatientUpdateLog();
        patientLog.setHealthId("h101");
        patientLog.setEventId(eventId);

        when(settingService.getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT")).thenReturn(limit);
        when(feedRepository.findPatientsUpdatedSince(null, limit, eventId)).thenReturn(asList(patientLog));

        List<PatientUpdateLog> patientLogs = patientService.findPatientsUpdatedSince(null, eventId);

        verify(settingService).getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT");
        verify(feedRepository).findPatientsUpdatedSince(null, limit, eventId);

        assertNotNull(patientLogs);
        assertEquals(1, patientLogs.size());
        assertEquals(eventId, patientLog.getEventId());
    }

    private PendingApprovalMapping buildPendingApprovalMapping(String healthId) throws InterruptedException {
        PendingApprovalMapping mapping = new PendingApprovalMapping();
        mapping.setHealthId(healthId);
        Catchment catchment = new Catchment("10", "20");
        catchment.setUpazilaId("30");
        mapping.setCatchmentId(catchment.getId());
        mapping.setLastUpdated(TimeUuidUtil.uuidForDate(new Date()));
        return mapping;
    }

    private PatientData buildPatient(String healthId) {
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);
        patient.setGivenName("Scott-" + healthId);
        patient.setSurName("Tiger-" + healthId);
        return patient;
    }

    private TreeSet<PendingApproval> buildPendingApprovalRequestMap(List<UUID> uuids, PhoneNumber phoneNumber, Address address) {
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApprovalField(GIVEN_NAME, "Harry", uuids));
        pendingApprovals.add(buildPendingApprovalField(SUR_NAME, "Potter", uuids));
        pendingApprovals.add(buildPendingApprovalField(BLOOD_GROUP, "As if I care!", uuids));
        Requester requester1 = new Requester("facility-4", "provider-4");
        pendingApprovals.add(buildPendingApprovalField(OCCUPATION, "Wizard", uuids.get(3), requester1, "Jobless"));

        PhoneNumber newPhoneNumber = new PhoneNumber();
        newPhoneNumber.setCountryCode("91");
        newPhoneNumber.setAreaCode("033");
        newPhoneNumber.setNumber("30001234");
        Requester requester2 = new Requester("facility-1", "provider-1");
        pendingApprovals.add(buildPendingApprovalField(PHONE_NUMBER, phoneNumber, uuids.get(0), requester2, newPhoneNumber));

        Address newAddress = new Address();
        newAddress.setDivisionId("10");
        newAddress.setDistrictId("21");
        newAddress.setUpazilaId("31");
        pendingApprovals.add(buildPendingApprovalField(PRESENT_ADDRESS, address, uuids.get(0), requester2, newAddress));
        return pendingApprovals;
    }

    private List<UUID> generateUUIDs() throws Exception {
        List<UUID> uuids = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            uuids.add(TimeUuidUtil.uuidForDate(new Date()));
        }
        return uuids;
    }

    private PendingApproval buildPendingApprovalField(String name, String currentValue, List<UUID> uuids) {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(name);
        pendingApproval.setCurrentValue(currentValue);

        TreeMap<UUID, PendingApprovalFieldDetails> detailsMap = new TreeMap<>();

        PendingApprovalFieldDetails details1 = new PendingApprovalFieldDetails();
        details1.setRequestedBy(new Requester("facility-1", "provider-1"));
        details1.setValue("A." + name);
        details1.setCreatedAt(TimeUuidUtil.getTimeFromUUID(uuids.get(0)));
        detailsMap.put(uuids.get(0), details1);

        PendingApprovalFieldDetails details2 = new PendingApprovalFieldDetails();
        details2.setRequestedBy(new Requester("facility-2", "provider-2"));
        details2.setValue("B." + name);
        details2.setCreatedAt(TimeUuidUtil.getTimeFromUUID(uuids.get(1)));
        detailsMap.put(uuids.get(1), details2);

        PendingApprovalFieldDetails details3 = new PendingApprovalFieldDetails();
        details3.setRequestedBy(new Requester("facility-3", "provider-3"));
        details3.setValue("C." + name);
        details3.setCreatedAt(TimeUuidUtil.getTimeFromUUID(uuids.get(2)));
        detailsMap.put(uuids.get(2), details3);

        pendingApproval.addFieldDetails(detailsMap);
        return pendingApproval;
    }

    private PendingApproval buildPendingApprovalField(String name, Object currentValue, UUID uuid, Requester requester, Object value) {
        PendingApproval fieldDetails = new PendingApproval();
        fieldDetails.setName(name);
        fieldDetails.setCurrentValue(currentValue);

        TreeMap<UUID, PendingApprovalFieldDetails> detailsMap = new TreeMap<>();
        PendingApprovalFieldDetails details = new PendingApprovalFieldDetails();
        details.setRequestedBy(requester);
        details.setValue(value);
        details.setCreatedAt(TimeUuidUtil.getTimeFromUUID(uuid));
        detailsMap.put(uuid, details);

        fieldDetails.addFieldDetails(detailsMap);
        return fieldDetails;
    }

    private Map getAvailability(boolean isAvailable, String reason) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("availability", isAvailable);
        if (StringUtils.isNotEmpty(reason)) {
            map.put("reason", reason);
        }
        return map;
    }

}