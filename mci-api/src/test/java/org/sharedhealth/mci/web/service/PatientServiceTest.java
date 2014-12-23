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

import java.util.*;

import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

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
        UUID after = UUIDs.timeBased();

        List<PendingApprovalMapping> mappings = asList(buildPendingApprovalMapping("hid-100"),
                buildPendingApprovalMapping("hid-200"),
                buildPendingApprovalMapping("hid-300"));

        when(patientRepository.findPendingApprovalMapping(catchment, after, null, 25)).thenReturn(mappings);
        when(patientRepository.findByHealthId("hid-100")).thenReturn(buildPatient("hid-100"));
        when(patientRepository.findByHealthId("hid-200")).thenReturn(buildPatient("hid-200"));
        when(patientRepository.findByHealthId("hid-300")).thenReturn(buildPatient("hid-300"));
        when(settingService.getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT")).thenReturn(25);

        List<PendingApprovalListResponse> pendingApprovals = patientService.findPendingApprovalList(catchment, after, null);

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

    private PendingApprovalMapping buildPendingApprovalMapping(String healthId) throws InterruptedException {
        PendingApprovalMapping mapping = new PendingApprovalMapping();
        mapping.setHealthId(healthId);
        mapping.setDivisionId("10");
        mapping.setDistrictId("20");
        mapping.setUpazilaId("30");
        mapping.setLastUpdated(UUIDs.timeBased());
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
        TreeSet<PendingApproval> actualResponse = patientService.findPendingApprovalDetails(healthId);
        verify(patientRepository).findByHealthId(healthId);

        assertEquals(6, actualResponse.size());
        for (int i = 0; i < 6; i++) {
            PendingApproval lhs = pendingApprovals.iterator().next();
            PendingApproval rhs = actualResponse.iterator().next();
            assertTrue(reflectionEquals(lhs, rhs));
        }
    }

    private TreeSet<PendingApproval> buildPendingApprovalRequestMap(List<UUID> uuids, PhoneNumber phoneNumber, Address address) {
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApprovalField(GIVEN_NAME, "Harry", uuids));
        pendingApprovals.add(buildPendingApprovalField(SUR_NAME, "Potter", uuids));
        pendingApprovals.add(buildPendingApprovalField(BLOOD_GROUP, "As if I care!", uuids));
        pendingApprovals.add(buildPendingApprovalField(OCCUPATION, "Wizard", uuids.get(3), "facility-4", "Jobless"));

        PhoneNumber newPhoneNumber = new PhoneNumber();
        newPhoneNumber.setCountryCode("91");
        newPhoneNumber.setAreaCode("033");
        newPhoneNumber.setNumber("30001234");
        pendingApprovals.add(buildPendingApprovalField(PHONE_NUMBER, phoneNumber, uuids.get(0), "facility-1", newPhoneNumber));

        Address newAddress = new Address();
        newAddress.setDivisionId("10");
        newAddress.setDistrictId("21");
        newAddress.setUpazilaId("31");
        pendingApprovals.add(buildPendingApprovalField(PRESENT_ADDRESS, address, uuids.get(0), "facility-1", newAddress));
        return pendingApprovals;
    }

    private List<UUID> generateUUIDs() throws Exception {
        List<UUID> uuids = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            uuids.add(UUIDs.timeBased());
            Thread.sleep(0, 10);
        }
        return uuids;
    }

    private PendingApproval buildPendingApprovalField(String name, String currentValue, List<UUID> uuids) {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(name);
        pendingApproval.setCurrentValue(currentValue);

        TreeMap<UUID, PendingApprovalFieldDetails> detailsMap = new TreeMap<>();

        PendingApprovalFieldDetails details1 = new PendingApprovalFieldDetails();
        details1.setFacilityId("facility-1");
        details1.setValue("A." + name);
        details1.setCreatedAt(unixTimestamp(uuids.get(0)));
        detailsMap.put(uuids.get(0), details1);

        PendingApprovalFieldDetails details2 = new PendingApprovalFieldDetails();
        details2.setFacilityId("facility-2");
        details2.setValue("B." + name);
        details2.setCreatedAt(unixTimestamp(uuids.get(1)));
        detailsMap.put(uuids.get(1), details2);

        PendingApprovalFieldDetails details3 = new PendingApprovalFieldDetails();
        details3.setFacilityId("facility-3");
        details3.setValue("C." + name);
        details3.setCreatedAt(unixTimestamp(uuids.get(2)));
        detailsMap.put(uuids.get(2), details3);

        pendingApproval.setFieldDetails(detailsMap);
        return pendingApproval;
    }

    private PendingApproval buildPendingApprovalField(String name, Object currentValue, UUID uuid, String facilityId, Object value) {
        PendingApproval fieldDetails = new PendingApproval();
        fieldDetails.setName(name);
        fieldDetails.setCurrentValue(currentValue);

        TreeMap<UUID, PendingApprovalFieldDetails> detailsMap = new TreeMap<>();
        PendingApprovalFieldDetails details = new PendingApprovalFieldDetails();
        details.setFacilityId(facilityId);
        details.setValue(value);
        details.setCreatedAt(unixTimestamp(uuid));
        detailsMap.put(uuid, details);

        fieldDetails.setFieldDetails(detailsMap);
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