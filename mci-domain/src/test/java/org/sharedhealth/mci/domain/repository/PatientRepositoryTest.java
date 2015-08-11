package org.sharedhealth.mci.domain.repository;

import org.junit.Before;
import org.junit.Test;
import org.sharedhealth.mci.domain.model.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;

public class PatientRepositoryTest {

    private PatientRepository patientRepository;

    @Before
    public void setUp() throws Exception {
        patientRepository = new PatientRepository(null, null, null);
    }

    @Test
    public void shouldFindLatestUuid() throws Exception {
        UUID uuid = null;
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();

        for (int i = 0; i < 5; i++) {
            uuid = timeBased();
            PendingApproval pendingApproval = new PendingApproval();
            pendingApproval.setName("name" + i);

            TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
            fieldDetailsMap.put(uuid, new PendingApprovalFieldDetails());
            pendingApproval.setFieldDetails(fieldDetailsMap);

            pendingApprovals.add(pendingApproval);
            Thread.sleep(0, 10);
        }
        assertEquals(uuid, patientRepository.findLatestUuid(pendingApprovals));
    }

    @Test
    public void shouldRemoveEntirePendingApprovalWhenAnyValueIsAccepted() {
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApprovalForOccupation());
        pendingApprovals.add(buildPendingApprovalForGender());

        PatientData patient = new PatientData();
        patient.setOccupation("02");

        TreeSet<PendingApproval> result = patientRepository.updatePendingApprovals(pendingApprovals, patient, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(GENDER, result.iterator().next().getName());
    }

    @Test
    public void shouldRemoveTheMatchingValueWhenRejected() {
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApprovalForOccupation());
        pendingApprovals.add(buildPendingApprovalForGender());

        PatientData patient = new PatientData();
        patient.setOccupation("02");

        TreeSet<PendingApproval> result = patientRepository.updatePendingApprovals(pendingApprovals, patient, false);
        assertNotNull(result);
        assertEquals(2, result.size());

        for (PendingApproval pendingApproval : result) {
            if (pendingApproval.getName().equals(GENDER)) {
                TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
                assertNotNull(fieldDetailsMap);
                assertEquals(1, fieldDetailsMap.size());

            } else if (pendingApproval.getName().equals(OCCUPATION)) {
                TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
                assertNotNull(fieldDetailsMap);
                assertEquals(2, fieldDetailsMap.size());
                for (PendingApprovalFieldDetails fieldDetails : fieldDetailsMap.values()) {
                    assertTrue(asList("01", "03").contains(fieldDetails.getValue()));
                }

            } else {
                fail("Invalid pending approval");
            }
        }
    }

    @Test
    public void shouldRemoveEntirePendingApprovalWhenAllValuesAreRejected() {
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApprovalForGender());

        PatientData patient = new PatientData();
        patient.setGender("F");

        TreeSet<PendingApproval> result = patientRepository.updatePendingApprovals(pendingApprovals, patient, false);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void shouldFindRequestedBy() {
        PatientData requestData = new PatientData();
        requestData.setGivenName("John");
        requestData.setAddress(new Address("10", "20", "30"));

        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApprovalForGivenName());
        pendingApprovals.add(buildPendingApprovalForAddress());

        Map<String, Set<Requester>> requesters = patientRepository.findRequestedBy(pendingApprovals, requestData);

        assertNotNull(requesters);
        assertEquals(2, requesters.size());

        assertNotNull(requesters.get(GIVEN_NAME));
        assertEquals(3, requesters.get(GIVEN_NAME).size());
        assertTrue(requesters.get(GIVEN_NAME).containsAll(asList(new Requester("Bahmni1", "Dr. Seuss1"),
                new Requester("Bahmni2", "Dr. Seuss2"),
                new Requester("Bahmni3", "Dr. Seuss3"))));

        assertNotNull(requesters.get(PRESENT_ADDRESS));
        assertEquals(3, requesters.get(PRESENT_ADDRESS).size());
        assertTrue(requesters.get(PRESENT_ADDRESS).containsAll(asList(new Requester("CHW1", "Dr. Monika1"),
                new Requester("CHW2", "Dr. Monika2"),
                new Requester("CHW3", "Dr. Monika3"))));
    }

    public PendingApproval buildPendingApprovalForGivenName() {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(GIVEN_NAME);
        pendingApproval.setCurrentValue("Harry");
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        for (int i = 1; i <= 3; i++) {
            PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
            fieldDetails.setValue("John");
            fieldDetails.setRequestedBy(new Requester("Bahmni" + i, "Dr. Seuss" + i));
            fieldDetailsMap.put(timeBased(), fieldDetails);
        }
        PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
        fieldDetails.setValue("Joe");
        fieldDetails.setRequestedBy(new Requester("Bahmni", "Dr. Monika"));
        fieldDetailsMap.put(timeBased(), fieldDetails);

        pendingApproval.setFieldDetails(fieldDetailsMap);
        return pendingApproval;
    }

    public PendingApproval buildPendingApprovalForAddress() {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(PRESENT_ADDRESS);
        pendingApproval.setCurrentValue("Harry");
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        for (int i = 1; i <= 3; i++) {
            PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
            fieldDetails.setValue(new Address("10", "20", "30"));
            fieldDetails.setRequestedBy(new Requester("CHW" + i, "Dr. Monika" + i));
            fieldDetailsMap.put(timeBased(), fieldDetails);
        }
        PendingApprovalFieldDetails fieldDetails1 = new PendingApprovalFieldDetails();
        fieldDetails1.setValue(new Address("10", "20", "30"));
        fieldDetails1.setRequestedBy(new Requester("CHW1", "Dr. Monika1"));
        fieldDetailsMap.put(timeBased(), fieldDetails1);

        PendingApprovalFieldDetails fieldDetails2 = new PendingApprovalFieldDetails();
        fieldDetails2.setValue(new Address("10", "20", "31"));
        fieldDetails2.setRequestedBy(new Requester("CHW", "Dr. Monika"));
        fieldDetailsMap.put(timeBased(), fieldDetails2);

        pendingApproval.setFieldDetails(fieldDetailsMap);
        return pendingApproval;
    }

    @Test
    public void shouldFilterWithSearchableFieldsInMemory() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final SearchQuery searchQuery = createSearchQuery("1");
        searchQuery.setPresent_address("100409990101");
        List<PatientData> patientDataList = getFilteredPatientsListBySearchQuery(searchQuery);

        assertNotNull(patientDataList);
        assertEquals(1, patientDataList.size());
        assertEquals("hid1", patientDataList.get(0).getHealthId());
    }

    @Test
    public void shouldFilterReturnEmptyIfNidDoesNotMatched() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final SearchQuery searchQuery = createSearchQuery("1");
        searchQuery.setNid("nid2");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);
    }

    @Test
    public void shouldFilterReturnEmptyIfUidDoesNotMatched() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final SearchQuery searchQuery = createSearchQuery("1");
        searchQuery.setUid("uid2");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);
    }

    @Test
    public void shouldFilterReturnEmptyIfBrnDoesNotMatched() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final SearchQuery searchQuery = createSearchQuery("1");
        searchQuery.setBin_brn("brn2");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);
    }

    @Test
    public void shouldFilterReturnEmptyIfHouseholdDoesNotMatched() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final SearchQuery searchQuery = createSearchQuery("1");
        searchQuery.setHousehold_code("householdCode2");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);
    }

    @Test
    public void shouldFilterReturnEmptyIfGivenNameDoesNotMatched() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final SearchQuery searchQuery = createSearchQuery("1");
        searchQuery.setGiven_name("givenName2");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);
    }

    @Test
    public void shouldFilterReturnEmptyIfSurnameDoesNotMatched() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final SearchQuery searchQuery = createSearchQuery("1");
        searchQuery.setSur_name("surname2");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);
    }

    @Test
    public void shouldFilterReturnEmptyIfPhoneDoesNotMatched() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final SearchQuery searchQuery = createSearchQuery("1");
        searchQuery.setPhone_no("0172");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);
    }

    @Test
    public void shouldFilterReturnEmptyIfAreaCodeDoesNotMatched() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final SearchQuery searchQuery = createSearchQuery("1");
        searchQuery.setArea_code("082");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);
    }

    @Test
    public void shouldFilterReturnEmptyIfAddressDoesNotMatched() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final SearchQuery searchQuery = createSearchQuery("1");

        searchQuery.setPresent_address("11");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);

        searchQuery.setPresent_address("1002");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);

        searchQuery.setPresent_address("100402");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);

        searchQuery.setPresent_address("10040992");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);

        searchQuery.setPresent_address("1004099902");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);

        searchQuery.setPresent_address("100409990102");
        assertEmptyResultForUnmatchedSearchQuery(searchQuery);
    }

    private void assertEmptyResultForUnmatchedSearchQuery(SearchQuery searchQuery) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<PatientData> patientDataList = getFilteredPatientsListBySearchQuery(searchQuery);

        assertNotNull(patientDataList);
        assertEquals(0, patientDataList.size());
    }

    private List<PatientData> getFilteredPatientsListBySearchQuery(SearchQuery searchQuery) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = PatientRepository.class.getDeclaredMethod("filterPatients", List.class, SearchQuery.class);
        method.setAccessible(true);
        return (List<PatientData>) method.invoke(patientRepository, createPatientLists(), searchQuery);
    }

    private SearchQuery createSearchQuery(String index) {
        return createSearchQuery(index, index, index, index, index, index, index);
    }

    private SearchQuery createSearchQuery(String nid, String uid, String brn, String household, String givenname, String surname, String phn) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setNid("nid" + nid);
        searchQuery.setUid("uid" + uid);
        searchQuery.setBin_brn("brn" + brn);
        searchQuery.setHousehold_code("householdCode" + household);
        searchQuery.setGiven_name("givenName" + givenname);
        searchQuery.setSur_name("surname" + surname);
        searchQuery.setPhone_no("017" + phn);
        searchQuery.setArea_code("08" + phn);
        return searchQuery;
    }

    private List<PatientData> createPatientLists() {
        return asList(
                createPatient("1"),
                createPatient("2"),
                createPatient("3"),
                createPatient("4"),
                createPatient("6")
        );
    }

    private PatientData createPatient(String index) {
        PatientData data = new PatientData();
        data.setHealthId("hid" + index);
        data.setNationalId("nid" + index);
        data.setBirthRegistrationNumber("brn" + index);
        data.setUid("uid" + index);
        data.setGivenName("givenName" + index);
        data.setSurName("surname" + index);
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber("017" + index);
        phone.setAreaCode("08" + index);
        data.setPhoneNumber(phone);
        data.setHouseholdCode("householdCode" + index);
        data.setAddress(createAddress("100409990" + index + "0" + index));
        return data;
    }

    private Address createAddress(String geoCode) {
        String division, district, upazila, cityCorp, union, ruralWard;
        division = district = upazila = cityCorp = union = ruralWard = null;

        if (geoCode.length() > 1) division = geoCode.substring(0, 2);
        if (geoCode.length() > 3) district = geoCode.substring(2, 4);
        if (geoCode.length() > 5) upazila = geoCode.substring(4, 6);
        if (geoCode.length() > 7) cityCorp = geoCode.substring(6, 8);
        if (geoCode.length() > 9) union = geoCode.substring(8, 10);
        if (geoCode.length() > 11) ruralWard = geoCode.substring(10, 12);

        Address address = new Address();
        address.setDivisionId(division);
        address.setDistrictId(district);
        address.setUpazilaId(upazila);
        address.setCityCorporationId(cityCorp);
        address.setUnionOrUrbanWardId(union);
        address.setRuralWardId(ruralWard);

        return address;
    }

    private PendingApproval buildPendingApprovalForOccupation() {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(OCCUPATION);
        pendingApproval.setCurrentValue("00");
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        for (int i = 1; i <= 3; i++) {
            PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
            fieldDetails.setValue("0" + i);
            fieldDetails.setRequestedBy(new Requester("Bahmni" + i, "Dr. Monika" + i));
            fieldDetailsMap.put(timeBased(), fieldDetails);
        }
        pendingApproval.setFieldDetails(fieldDetailsMap);
        return pendingApproval;
    }

    private PendingApproval buildPendingApprovalForGender() {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(GENDER);
        pendingApproval.setCurrentValue("M");
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
        fieldDetails.setValue("F");
        fieldDetails.setRequestedBy(new Requester("Bahmni", "Dr. Monika"));
        fieldDetailsMap.put(timeBased(), fieldDetails);
        pendingApproval.setFieldDetails(fieldDetailsMap);
        return pendingApproval;
    }
}
