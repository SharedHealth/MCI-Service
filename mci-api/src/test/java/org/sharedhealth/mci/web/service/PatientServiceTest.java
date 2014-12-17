package org.sharedhealth.mci.web.service;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.fr.FacilityRegistryWrapper;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.model.PendingApprovalMapping;
import org.sharedhealth.mci.web.model.PendingApprovalRequest;

import java.util.*;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;

public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    FacilityRegistryWrapper facilityRegistryWrapper;
    @Mock
    SettingService settingService;

    private PatientService patientService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        patientService = new PatientService(patientRepository, facilityRegistryWrapper, settingService);
    }

    @Test
    public void shouldUpdateInsteadofCreatingWhenMatchingPatientExists() throws Exception {
        PatientData patientData = new PatientData();
        patientData.setNationalId("nid-100");
        patientData.setBirthRegistrationNumber("brn-100");

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setNid("nid-100");
        searchQuery.setBin_brn("brn-100");

        PatientData patientDataFromDb = new PatientData();
        patientDataFromDb.setHealthId("hid-100");
        patientDataFromDb.setNationalId("nid-100");
        patientDataFromDb.setBirthRegistrationNumber("brn-100");

        when(patientRepository.findAllByQuery(searchQuery)).thenReturn(asList(patientDataFromDb));

        patientService.create(patientData);
        InOrder inOrder = inOrder(patientRepository);
        inOrder.verify(patientRepository).findAllByQuery(searchQuery);
        inOrder.verify(patientRepository).update(patientData, "hid-100");
        inOrder.verify(patientRepository, never()).create(any(PatientData.class));
    }

    @Test
    public void shouldFindPendingApprovalListByCatchment() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        UUID lastItemId = UUIDs.timeBased();

        List<PendingApprovalMapping> mappings = asList(buildPendingApprovalMapping("hid-100"),
                buildPendingApprovalMapping("hid-200"),
                buildPendingApprovalMapping("hid-300"));
        Collections.reverse(mappings);
        when(settingService.getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT")).thenReturn(25);
        when(patientRepository.findPendingApprovalMapping(catchment, lastItemId, 25)).thenReturn(mappings);

        List<PatientData> patients = asList(buildPatient("hid-300"),
                buildPatient("hid-200"),
                buildPatient("hid-100"));
        when(patientRepository.findByHealthId(asList("hid-300", "hid-200", "hid-100"))).thenReturn(patients);

        PendingApprovalListResponse response = patientService.findPendingApprovalList(catchment, lastItemId);

        InOrder inOrder = inOrder(patientRepository);
        inOrder.verify(patientRepository).findPendingApprovalMapping(catchment, lastItemId, 25);
        inOrder.verify(patientRepository).findByHealthId(asList("hid-300", "hid-200", "hid-100"));

        assertNotNull(response);
        assertEquals(mappings.get(mappings.size() - 1).getCreatedAt(), response.getLastItemId());
        assertNotNull(response.getPendingApprovals());
        assertEquals(3, response.getPendingApprovals().size());

        Map<String, String> metadata = response.getPendingApprovals().get(0);
        String healthId = metadata.get(HID);
        assertNotNull(healthId);
        assertEquals("Scott-" + healthId, metadata.get(GIVEN_NAME));
        assertEquals("Tiger-" + healthId, metadata.get(SUR_NAME));
    }

    private PendingApprovalMapping buildPendingApprovalMapping(String healthId) throws InterruptedException {
        PendingApprovalMapping mapping = new PendingApprovalMapping();
        mapping.setHealthId(healthId);
        mapping.setDivisionId("10");
        mapping.setDistrictId("20");
        mapping.setUpazilaId("30");
        mapping.setCreatedAt(UUIDs.timeBased());
        Thread.sleep(0, 10);
        return mapping;
    }

    private PatientData buildPatient(String healthId) {
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);
        patient.setGivenName("Scott-" + healthId);
        patient.setSurName("Tiger-" + healthId);
        return patient;
    }

    @Test
    public void shouldFindPendingApprovalsByHealthId() throws Exception {
        String healthId = "healthId-100";
        PatientData patient = new PatientData();
        patient.setGivenName("Harry");
        patient.setSurName("Potter");
        patient.setBloodGroup("As if I care!");
        patient.setOccupation("Wizard");

        List<UUID> uuids = generateUUIDs();
        patient.setPendingApprovals(buildPendingApprovalRequestMap(uuids));

        when(patientRepository.findByHealthId(healthId)).thenReturn(patient);
        TreeSet<PendingApprovalDetails> actualResponse = patientService.findPendingApprovals(healthId);
        verify(patientRepository).findByHealthId(healthId);

        TreeSet<PendingApprovalDetails> expectedResponse = new TreeSet<>();
        expectedResponse.add(buildPendingApprovalField(GIVEN_NAME, "Harry", uuids));
        expectedResponse.add(buildPendingApprovalField(SUR_NAME, "Potter", uuids));
        expectedResponse.add(buildPendingApprovalField(BLOOD_GROUP, "As if I care!", uuids));
        expectedResponse.add(buildPendingApprovalField(OCCUPATION, "Wizard", uuids.get(3), "facility-4", "Jobless"));

        assertEquals(4, actualResponse.size());
        for (int i = 0; i < 4; i++) {
            PendingApprovalDetails lhs = expectedResponse.pollFirst();
            PendingApprovalDetails rhs = actualResponse.pollFirst();
            assertTrue(reflectionEquals(lhs, rhs));
        }
    }

    private Map<UUID, String> buildPendingApprovalRequestMap(List<UUID> uuids) {
        Map<UUID, String> requests = new HashMap<>();

        Map<String, String> fields1 = new HashMap<>();
        fields1.put(GIVEN_NAME, "A." + GIVEN_NAME);
        fields1.put(SUR_NAME, "A." + SUR_NAME);
        fields1.put(BLOOD_GROUP, "A." + BLOOD_GROUP);
        requests.put(uuids.get(0), buildPendingApprovalRequest("facility-1", fields1));

        Map<String, String> fields2 = new HashMap<>();
        fields2.put(GIVEN_NAME, "B." + GIVEN_NAME);
        fields2.put(SUR_NAME, "B." + SUR_NAME);
        fields2.put(BLOOD_GROUP, "B." + BLOOD_GROUP);
        requests.put(uuids.get(1), buildPendingApprovalRequest("facility-2", fields2));

        Map<String, String> fields3 = new HashMap<>();
        fields3.put(GIVEN_NAME, "C." + GIVEN_NAME);
        fields3.put(SUR_NAME, "C." + SUR_NAME);
        fields3.put(BLOOD_GROUP, "C." + BLOOD_GROUP);
        requests.put(uuids.get(2), buildPendingApprovalRequest("facility-3", fields3));

        Map<String, String> fields4 = new HashMap<>();
        fields4.put(OCCUPATION, "Jobless");
        requests.put(uuids.get(3), buildPendingApprovalRequest("facility-4", fields4));

        return requests;
    }

    private List<UUID> generateUUIDs() throws Exception {
        List<UUID> uuids = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            uuids.add(UUIDs.timeBased());
            Thread.sleep(0, 10);
        }
        return uuids;
    }

    private String buildPendingApprovalRequest(String facilityId, Map<String, String> fields) {
        PendingApprovalRequest request = new PendingApprovalRequest();
        request.setFacilityId(facilityId);
        request.setFields(fields);
        return writeValueAsString(request);
    }

    private PendingApprovalDetails buildPendingApprovalField(String name, String currentValue, List<UUID> uuids) {
        PendingApprovalDetails fieldDetails = new PendingApprovalDetails();
        fieldDetails.setName(name);
        fieldDetails.setCurrentValue(currentValue);

        TreeMap<UUID, PendingApprovalFieldDetails> detailsMap = new TreeMap<>();

        PendingApprovalFieldDetails details1 = new PendingApprovalFieldDetails();
        details1.setFacilityId("facility-1");
        details1.setValue("A." + name);
        detailsMap.put(uuids.get(0), details1);

        PendingApprovalFieldDetails details2 = new PendingApprovalFieldDetails();
        details2.setFacilityId("facility-2");
        details2.setValue("B." + name);
        detailsMap.put(uuids.get(1), details2);

        PendingApprovalFieldDetails details3 = new PendingApprovalFieldDetails();
        details3.setFacilityId("facility-3");
        details3.setValue("C." + name);
        detailsMap.put(uuids.get(2), details3);

        fieldDetails.setDetails(detailsMap);
        return fieldDetails;
    }

    private PendingApprovalDetails buildPendingApprovalField(String name, String currentValue, UUID uuid, String facilityId, String value) {
        PendingApprovalDetails fieldDetails = new PendingApprovalDetails();
        fieldDetails.setName(name);
        fieldDetails.setCurrentValue(currentValue);

        TreeMap<UUID, PendingApprovalFieldDetails> detailsMap = new TreeMap<>();
        PendingApprovalFieldDetails details = new PendingApprovalFieldDetails();
        details.setFacilityId(facilityId);
        details.setValue(value);
        detailsMap.put(uuid, details);

        fieldDetails.setDetails(detailsMap);
        return fieldDetails;
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
}