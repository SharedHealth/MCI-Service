package org.sharedhealth.mci.searchmapping.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.sharedhealth.mci.domain.util.BaseIntegrationTest;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;

@RunWith(SpringJUnit4ClassRunner.class)
public class PatientSearchMappingRepositoryIT extends BaseIntegrationTest {
    private static final String FACILITY = "Bahmni";

    @Autowired
    private PatientSearchMappingRepository searchMappingRepository;
    @Autowired
    private PatientRepository patientRepository;

    @Test
    public void shouldCreateMappingsForPatientSearch() throws Exception {
        String healthId = patientRepository.create(buildPatient()).getId();
        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId));

        assertNotNull(getNidMappings("1234567890123", healthId).get(0));
        assertNotNull(getBrnMappings("12345678901234567", healthId).get(0));
    }

    @Test
    public void shouldFindAllPatientsByCatchmentWithLastMarkerParam() throws Exception {
        List<String> healthIds = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            PatientData patientData = buildPatient();
            Address address = createAddress("10", "20", "30");
            address.setCityCorporationId("40");
            address.setUnionOrUrbanWardId("5" + i);
            address.setRuralWardId("6" + i);
            patientData.setAddress(address);
            String healthId = String.valueOf(new Date().getTime());
            patientData.setHealthId(healthId);
            healthIds.add(patientRepository.create(patientData).getId());

            searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId));
        }

        Catchment catchment = new Catchment("10", "20", "30");
        catchment.setCityCorpId("40");
        UUID updatedAt = cassandraOps.selectOneById(Patient.class, healthIds.get(0)).getUpdatedAt();
        assertNotNull(updatedAt);
        int limit = 3;
        List<PatientData> patients = patientRepository.findAllByCatchment(catchment, null, updatedAt, limit);

        assertTrue(isNotEmpty(patients));
        assertEquals(limit, patients.size());
        assertEquals(healthIds.get(1), patients.get(0).getHealthId());
        assertEquals(healthIds.get(2), patients.get(1).getHealthId());
        assertEquals(healthIds.get(3), patients.get(2).getHealthId());
    }

    @Test
    public void shouldFindAllPatientsByCatchmentWithSinceParam() throws Exception {
        List<String> healthIds = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            PatientData patientData = buildPatient();
            Address address = createAddress("10", "20", "30");
            address.setCityCorporationId("40");

            patientData.setHealthId(String.valueOf(new Date().getTime()));
            address.setUnionOrUrbanWardId("5" + i);
            address.setRuralWardId("6" + i);
            patientData.setAddress(address);
            String healthId = patientRepository.create(patientData).getId();
            healthIds.add(healthId);
            searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId));
        }

        Catchment catchment = new Catchment("10", "20", "30");
        catchment.setCityCorpId("40");
        UUID updatedAt = cassandraOps.selectOneById(Patient.class, healthIds.get(0)).getUpdatedAt();
        assertNotNull(updatedAt);
        int limit = 3;
        Date since = new Date(TimeUuidUtil.getTimeFromUUID(updatedAt));
        List<PatientData> patients = patientRepository.findAllByCatchment(catchment, since, null, limit);

        assertTrue(isNotEmpty(patients));
        assertEquals(limit, patients.size());
        assertEquals(healthIds.get(0), patients.get(0).getHealthId());
        assertEquals(healthIds.get(1), patients.get(1).getHealthId());
        assertEquals(healthIds.get(2), patients.get(2).getHealthId());
    }
    
    @Test
    public void shouldNotDeleteMappingsWhenOtherFieldsAreUpdated() {
        String nid = "1000000000000";
        String brn = "10000000000000000";
        String uid = "10000000000";
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("100000000");

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId(nid);
        patient.setBirthRegistrationNumber(brn);
        patient.setUid(uid);
        patient.setReligion("01");
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(phoneNumber);
        String healthId = patientRepository.create(patient).getId();

        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId));
        assertNotNull(healthId);

        assertEquals(1, getNidMappings(nid, healthId).size());
        assertEquals(1, getBrnMappings(brn, healthId).size());

        PatientData updateRequest = initPatientData();
        updateRequest.setHealthId(healthId);
        updateRequest.setReligion("02");
        patientRepository.update(updateRequest, patientRepository.findByHealthId(healthId), new Requester());

        assertEquals(1, getNidMappings(nid, healthId).size());
        assertEquals(1, getBrnMappings(brn, healthId).size());
    }

    @Test
    public void shouldUpdateAppropriateIdAndPhoneNumberMappingsWhenMultiplePatientsWithSameIdsExist() {
        String nid1 = "1000000000000";
        String brn1 = "10000000000000000";
        String uid1 = "10000000000";
        PhoneNumber phoneNumber1 = new PhoneNumber();
        phoneNumber1.setNumber("100000000");

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setNationalId(nid1);
        patient.setBirthRegistrationNumber(brn1);
        patient.setUid(uid1);
        patient.setAddress(createAddress("10", "20", "30"));
        patient.setPhoneNumber(phoneNumber1);
        String healthId1 = patientRepository.create(patient).getId();

        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId1));
        assertNotNull(healthId1);

        patient.setGivenName("Jane");

        patient.setHealthId(String.valueOf(new Date().getTime()));
        String healthId2 = patientRepository.create(patient).getId();

        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId2));
        assertNotNull(healthId2);
        assertFalse(healthId1.equals(healthId2));

        String nid2 = "2000000000000";
        String brn2 = "20000000000000000";
        String uid2 = "20000000000";
        PhoneNumber phoneNumber2 = new PhoneNumber();
        phoneNumber2.setNumber("200000000");

        PatientData updateRequest = initPatientData();
        updateRequest.setHealthId(healthId2);
        updateRequest.setNationalId(nid2);
        updateRequest.setBirthRegistrationNumber(brn2);
        updateRequest.setUid(uid2);
        updateRequest.setPhoneNumber(phoneNumber2);
        patientRepository.update(updateRequest, patientRepository.findByHealthId(healthId2), new Requester());

        PatientData patient2 = patientRepository.findByHealthId(healthId2);
        assertNotNull(patient2);
        assertEquals(nid2, patient2.getNationalId());
        assertEquals(brn2, patient2.getBirthRegistrationNumber());
        assertEquals(uid2, patient2.getUid());
        assertEquals(phoneNumber2, patient2.getPhoneNumber());

        assertThat(getNidMappings(nid1, healthId1).size(), is(1));
        assertThat(getNidMappings(nid2, healthId2).size(), is(1));

        assertThat(getBrnMappings(brn1, healthId1).size(), is(1));
        assertThat(getBrnMappings(brn2, healthId2).size(), is(1));

        assertThat(getUidMappings(uid1, healthId1).size(), is(1));
        assertThat(getUidMappings(uid2, healthId2).size(), is(1));

        assertThat(getPatientsByPhoneNumber(phoneNumber1).size(), is(1));
        assertThat(getPatientsByPhoneNumber(phoneNumber2).size(), is(1));
    }

    @Test
    public void shouldUpdateAppropriateNameMappingWhenMultiplePatientsWithSameNameAndAddressExist() {
        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        Address address = createAddress("10", "20", "30");
        patient.setAddress(address);
        String healthId1 = patientRepository.create(patient).getId();

        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId1));
        assertNotNull(healthId1);

        patient.setHealthId(String.valueOf(new Date().getTime()));
        String healthId2 = patientRepository.create(patient).getId();

        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId2));
        assertNotNull(healthId2);

        PatientData updateRequest = initPatientData();
        updateRequest.setGivenName("Jane");
        patientRepository.update(updateRequest, patientRepository.findByHealthId(healthId2), new Requester());

        assertSearchByNameAndAddressExists("John", "102030", healthId1);
        assertSearchByNameAndAddressExists("Jane", "102030", healthId2);
    }

    @Test
    public void shouldUpdateHouseholdCodeMappingsWhenExistingValueIsNotEmptyAndNewValueIsNotEmptyAndBothValuesAreDifferent() {
        String existingHouseholdCode = "12345";

        PatientData patient = initPatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setHouseholdCode(existingHouseholdCode);
        patient.setAddress(createAddress("10", "20", "30"));
        String healthId = patientRepository.create(patient).getId();

        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId));
        assertNotNull(healthId);

        assertThat(getHouseholdCodeMappings(existingHouseholdCode, healthId).size(), is(1));

        String newHouseholdCode = "5678";
        final String newReligion = "02";

        PatientData updateRequest = initPatientData();
        updateRequest.setReligion(newReligion);
        updateRequest.setHouseholdCode(newHouseholdCode);
        patientRepository.update(updateRequest, patientRepository.findByHealthId(healthId), new Requester());

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(newReligion, updatedPatient.getReligion());
        assertEquals(newHouseholdCode, updatedPatient.getHouseholdCode());

        assertTrue(isEmpty(getHouseholdCodeMappings(existingHouseholdCode, healthId)));
        assertThat(getHouseholdCodeMappings(newHouseholdCode, healthId).size(), is(1));
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

    private List<NidMapping> getNidMappings(String nid, String healthId) {
        return cassandraOps.select(select().from(CF_NID_MAPPING).where(eq(NATIONAL_ID, nid)).and(eq(HEALTH_ID, healthId)).toString(), NidMapping.class);
    }

    private List<BrnMapping> getBrnMappings(String brn, String healthId) {
        return cassandraOps.select(select().from(CF_BRN_MAPPING).where(eq(BIN_BRN, brn)).and(eq(HEALTH_ID, healthId)).toString(), BrnMapping.class);
    }

    private List<UidMapping> getUidMappings(String uid, String healthId) {
        return cassandraOps.select(select().from(CF_UID_MAPPING).where(eq(UID, uid)).and(eq(HEALTH_ID, healthId)).toString(), UidMapping.class);
    }

    private List<PatientData> getPatientsByPhoneNumber(PhoneNumber phoneNumber) {
        assertNotNull(phoneNumber);
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setPhone_no(phoneNumber.getNumber());
        return patientRepository.findAllByQuery(searchQuery);
    }

    private List<HouseholdCodeMapping> getHouseholdCodeMappings(String householdCode, String healthId) {
        return cassandraOps.select(select().from(CF_HOUSEHOLD_CODE_MAPPING).where(eq(HOUSEHOLD_CODE, householdCode)).and(eq(HEALTH_ID, healthId)).toString(), HouseholdCodeMapping.class);
    }

    private PatientData initPatientData() {
        PatientData patient = new PatientData();
        patient.setRequester(FACILITY, null);
        patient.setHealthId(String.valueOf(new Date().getTime()));
        return patient;
    }

    private PatientData buildPatient() {
        PatientData patient = new PatientData();
        patient.setHealthId(String.valueOf(new Date().getTime()));
        patient.setNationalId("1234567890123");
        patient.setBirthRegistrationNumber("12345678901234567");
        patient.setUid("12345678901");
        patient.setGivenName("Happy");
        patient.setSurName("Rotter");
        patient.setDateOfBirth(parseDate("2014-12-01"));
        patient.setGender("M");
        patient.setOccupation("01");
        patient.setEducationLevel("01");
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber("22334455");
        patient.setPhoneNumber(phone);
        patient.setHouseholdCode("12345");
        patient.setAddress(new Address("10", "20", "30"));
        patient.setPermanentAddress(new Address("10", "20", "30"));
        patient.setRequester("Bahmni", null);
        return patient;
    }

    private Address createAddress(String division, String district, String upazila) {
        Address address = new Address(division, district, upazila);
        address.setCountryCode("050");
        return address;
    }
}