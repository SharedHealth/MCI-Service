package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.exception.HealthIDExistException;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.*;
import static org.sharedhealth.mci.web.utils.JsonConstants.PHONE_NUMBER;
import static org.sharedhealth.mci.web.utils.PatientDataConstants.PATIENT_STATUS_ALIVE;
import static org.sharedhealth.mci.web.utils.PatientDataConstants.STRING_NO;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientRepositoryIT {
    @SuppressWarnings("SpringJavaAutowiringInspection")

    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cassandraOps;

    @Autowired
    private PatientRepository patientRepository;

    private PatientData data;
    private String nationalId = "1234567890123";
    private String birthRegistrationNumber = "12345678901234567";
    private String uid = "12345678901";
    private String givenName = "Scott";
    public String surname = "Tiger";
    public String phoneNumber = "999900000";
    public String divisionId = "10";
    public String districtId = "04";
    public String upazilaId = "09";

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        data = createPatient();
    }

    @After
    public void tearDown() {
        cassandraOps.execute("truncate " + CF_PATIENT);
        cassandraOps.execute("truncate " + CF_NID_MAPPING);
        cassandraOps.execute("truncate " + CF_BRN_MAPPING);
        cassandraOps.execute("truncate " + CF_UID_MAPPING);
        cassandraOps.execute("truncate " + CF_PHONE_NUMBER_MAPPING);
        cassandraOps.execute("truncate " + CF_NAME_MAPPING);
        cassandraOps.execute("truncate " + CF_PENDING_APPROVAL_MAPPING);
        cassandraOps.execute("truncate " + CF_CATCHMENT_MAPPING);
    }

    private PatientData createPatient() {
        PatientData data = new PatientData();
        data.setNationalId(nationalId);
        data.setBirthRegistrationNumber(birthRegistrationNumber);
        data.setUid(uid);
        data.setGivenName(givenName);
        data.setSurName(surname);
        data.setDateOfBirth("2014-12-01");
        data.setGender("M");
        data.setOccupation("03");
        data.setEducationLevel("BA");
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber(phoneNumber);
        data.setPhoneNumber(phone);

        Address address = createAddress(divisionId, districtId, upazilaId, "20", "01");
        data.setAddress(address);

        return data;
    }

    private Address createAddress(String division, String district, String upazila, String cityCorp, String ward) {
        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId(division);
        address.setDistrictId(district);
        address.setUpazilaId(upazila);
        address.setCityCorporationId(cityCorp);
        address.setUnionOrUrbanWardId(ward);

        return address;
    }

    @Test
    public void shouldCreateMappingsWhenPatientIsCreated() {
        String id = patientRepository.create(data).getId();
        assertNotNull(id);

        String healthId = cassandraOps.queryForObject(buildFindByNidStmt(nationalId), String.class);
        assertEquals(id, healthId);

        healthId = cassandraOps.queryForObject(buildFindByBrnStmt(birthRegistrationNumber), String.class);
        assertEquals(id, healthId);

        healthId = cassandraOps.queryForObject(buildFindByUidStmt(uid), String.class);
        assertEquals(id, healthId);

        healthId = cassandraOps.queryForObject(buildFindByPhoneNumberStmt(phoneNumber), String.class);
        assertEquals(id, healthId);

        healthId = cassandraOps.queryForObject(buildFindByNameStmt(divisionId, districtId, upazilaId,
                givenName.toLowerCase(), surname.toLowerCase()), String.class);
        assertEquals(id, healthId);

        assertCatchmentMappings(patientRepository.findByHealthId(healthId));
    }

    private void assertCatchmentMappings(PatientData patient) {
        List<CatchmentMapping> catchmentMappings = cassandraOps.select(buildFindCatchmentMappingsStmt(patient),
                CatchmentMapping.class);
        assertNotNull(catchmentMappings);

        List<String> catchmentIds = patient.getCatchment().getAllIds();
        assertEquals(catchmentIds.size(), catchmentMappings.size());

        for (CatchmentMapping catchmentMapping : catchmentMappings) {
            assertTrue(catchmentIds.contains(catchmentMapping.getCatchmentId()));
            assertEquals(patient.getHealthId(), catchmentMapping.getHealthId());
            assertEquals(patient.getUpdatedAt(), catchmentMapping.getLastUpdated());
        }
    }

    private static String buildFindCatchmentMappingsStmt(PatientData patient) {
        List<String> catchmentIds = patient.getCatchment().getAllIds();
        return select().from(CF_CATCHMENT_MAPPING)
                .where(in(CATCHMENT_ID, catchmentIds.toArray(new String[catchmentIds.size()])))
                .and(eq(LAST_UPDATED, patient.getUpdatedAt()))
                .and(eq(HEALTH_ID, patient.getHealthId())).toString();
    }

    @Test
    public void shouldNotCreateNidMappingWhenPatientIsCreatedWithoutNationalId() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setAddress(new Address("1", "2", "3"));
        assertNotNull(patientRepository.create(patient).getId());

        String cql = select().from(CF_NID_MAPPING).toString();
        assertTrue(isEmpty(cassandraOps.select(cql, NidMapping.class)));
    }

    @Test
    public void shouldNotCreateBrnMappingWhenPatientIsCreatedWithoutBrn() {
        PatientData patient = new PatientData();
        patient.setGivenName("Jane");
        patient.setSurName("Doe");
        patient.setAddress(new Address("1", "2", "3"));
        assertNotNull(patientRepository.create(patient).getId());

        String cql = select().from(CF_BRN_MAPPING).toString();
        assertTrue(isEmpty(cassandraOps.select(cql, BrnMapping.class)));
    }

    @Test
    public void shouldNotCreateUidMappingWhenPatientIsCreatedWithoutUid() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setAddress(new Address("1", "2", "3"));
        assertNotNull(patientRepository.create(patient).getId());

        String cql = select().from(CF_UID_MAPPING).toString();
        assertTrue(isEmpty(cassandraOps.select(cql, UidMapping.class)));
    }

    @Test
    public void shouldNotCreatePhoneNumberMappingWhenPatientIsCreatedWithoutPhoneNumber() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setAddress(new Address("1", "2", "3"));
        assertNotNull(patientRepository.create(patient).getId());

        String cql = select().from(CF_PHONE_NUMBER_MAPPING).toString();
        assertTrue(isEmpty(cassandraOps.select(cql, PhoneNumber.class)));
    }

    @Test
    public void shouldFindPatientWithMatchingGeneratedHealthId() throws ExecutionException, InterruptedException {
        MCIResponse mciResponse = patientRepository.create(data);
        PatientData p = patientRepository.findByHealthId(mciResponse.id);
        assertNotNull(p);
        data.setHealthId(mciResponse.id);
        data.setCreatedAt(p.getCreatedAt());
        data.setUpdatedAt(p.getUpdatedAt());
        data.setStatus(PATIENT_STATUS_ALIVE);
        data.setConfidential(STRING_NO);

        Address address = p.getAddress();
        address.setRuralWardId(null);
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

        assertEquals(data, p);
    }

    @Test(expected = PatientNotFoundException.class)
    public void shouldThrowException_IfPatientDoesNotExistForGivenHealthId() {
        patientRepository.findByHealthId(UUID.randomUUID().toString());
    }

    @Test(expected = HealthIDExistException.class)
    public void shouldThrowException_IfHealthIdProvidedForCreate() throws ExecutionException, InterruptedException {
        data.setHealthId("12");
        patientRepository.create(data);
    }

    @Test(expected = PatientNotFoundException.class)
    public void shouldThrowErrorIfPatientNotFound() throws Exception {
        patientRepository.update(new PatientData(), "1");
    }

    @Test
    public void shouldUpdatePatient() throws Exception {
        PatientData data = createPatient();
        MCIResponse mciResponseForCreate = patientRepository.create(data);
        assertEquals(201, mciResponseForCreate.getHttpStatus());
        String healthId = mciResponseForCreate.getId();
        data.setHealthId(healthId);
        data.setGivenName("Danny");
        MCIResponse mciResponseForUpdate = patientRepository.update(data, data.getHealthId());
        assertEquals(202, mciResponseForUpdate.getHttpStatus());
        PatientData savedPatient = patientRepository.findByHealthId(healthId);

        Address address = savedPatient.getAddress();
        address.setRuralWardId(null);
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
    public void shouldUpdateNidMappingWhenNidIsUpdated() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        String nid1 = "1000000000000";
        patient.setNationalId(nid1);
        patient.setAddress(new Address("1", "2", "3"));
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        String nid2 = "2000000000000";
        patient.setNationalId(nid2);
        patientRepository.update(patient, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(nid2, updatedPatient.getNationalId());

        SearchQuery query = new SearchQuery();
        query.setNid(nid1);
        assertTrue(isEmpty(patientRepository.findAllByQuery(query)));

        assertSearchByNid(nid2, healthId);
    }

    @Test
    public void shouldUpdateAppropriateNidMappingWhenMultiplePatientsWithSameNidExist() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        String nid1 = "1000000000000";
        patient.setNationalId(nid1);
        patient.setAddress(new Address("1", "2", "3"));
        String healthId1 = patientRepository.create(patient).getId();
        assertNotNull(healthId1);

        patient.setGivenName("Jane");
        String healthId2 = patientRepository.create(patient).getId();
        assertNotNull(healthId2);
        assertFalse(healthId1.equals(healthId2));

        String nid2 = "2000000000000";
        patient.setNationalId(nid2);
        patientRepository.update(patient, healthId2);
        PatientData patient2 = patientRepository.findByHealthId(healthId2);
        assertNotNull(patient2);
        assertEquals(nid2, patient2.getNationalId());

        assertSearchByNid(nid1, healthId1);
        assertSearchByNid(nid2, healthId2);
    }

    private void assertSearchByNid(String nid, String healthId) {
        SearchQuery query = new SearchQuery();
        query.setNid(nid);
        List<PatientData> patients = patientRepository.findAllByQuery(query);
        assertTrue(isNotEmpty(patients));
        assertEquals(1, patients.size());
        assertEquals(healthId, patients.get(0).getHealthId());
    }

    @Test
    public void shouldUpdateBrnMappingWhenBrnIsUpdated() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        String brn1 = "10000000000000000";
        patient.setBirthRegistrationNumber(brn1);
        patient.setAddress(new Address("1", "2", "3"));
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        String brn2 = "20000000000000000";
        patient.setBirthRegistrationNumber(brn2);
        patientRepository.update(patient, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(brn2, updatedPatient.getBirthRegistrationNumber());

        SearchQuery query = new SearchQuery();
        query.setBin_brn(brn1);
        assertTrue(isEmpty(patientRepository.findAllByQuery(query)));

        assertSearchByBrn(brn2, healthId);
    }

    @Test
    public void shouldUpdateAppropriateBrnMappingWhenMultiplePatientsWithSameBrnExist() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        String brn1 = "10000000000000000";
        patient.setBirthRegistrationNumber(brn1);
        patient.setAddress(new Address("1", "2", "3"));
        String healthId1 = patientRepository.create(patient).getId();
        assertNotNull(healthId1);

        patient.setGivenName("Jane");
        String healthId2 = patientRepository.create(patient).getId();
        assertNotNull(healthId2);
        assertFalse(healthId1.equals(healthId2));

        String brn2 = "20000000000000000";
        patient.setBirthRegistrationNumber(brn2);
        patientRepository.update(patient, healthId2);
        PatientData patient2 = patientRepository.findByHealthId(healthId2);
        assertNotNull(patient2);
        assertEquals(brn2, patient2.getBirthRegistrationNumber());

        assertSearchByBrn(brn1, healthId1);
        assertSearchByBrn(brn2, healthId2);
    }

    private void assertSearchByBrn(String brn, String healthId) {
        SearchQuery query = new SearchQuery();
        query.setBin_brn(brn);
        List<PatientData> patients = patientRepository.findAllByQuery(query);
        assertTrue(isNotEmpty(patients));
        assertEquals(1, patients.size());
        assertEquals(healthId, patients.get(0).getHealthId());
    }

    @Test
    public void shouldUpdateUidMappingWhenUidIsUpdated() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        String uid1 = "10000000000";
        patient.setUid(uid1);
        patient.setAddress(new Address("1", "2", "3"));
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        String uid2 = "20000000000";
        patient.setUid(uid2);
        patientRepository.update(patient, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(uid2, updatedPatient.getUid());

        SearchQuery query = new SearchQuery();
        query.setUid(uid1);
        assertTrue(isEmpty(patientRepository.findAllByQuery(query)));

        assertSearchByUid(uid2, healthId);
    }

    @Test
    public void shouldUpdateAppropriateUidMappingWhenMultiplePatientsWithSameUidExist() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        String uid1 = "10000000000";
        patient.setUid(uid1);
        patient.setAddress(new Address("1", "2", "3"));
        String healthId1 = patientRepository.create(patient).getId();
        assertNotNull(healthId1);

        patient.setGivenName("Jane");
        String healthId2 = patientRepository.create(patient).getId();
        assertNotNull(healthId2);
        assertFalse(healthId1.equals(healthId2));

        String uid2 = "20000000000";
        patient.setUid(uid2);
        patientRepository.update(patient, healthId2);
        PatientData patient2 = patientRepository.findByHealthId(healthId2);
        assertNotNull(patient2);
        assertEquals(uid2, patient2.getUid());

        assertSearchByUid(uid1, healthId1);
        assertSearchByUid(uid2, healthId2);
    }

    private void assertSearchByUid(String uid, String healthId) {
        SearchQuery query = new SearchQuery();
        query.setUid(uid);
        List<PatientData> patients = patientRepository.findAllByQuery(query);
        assertTrue(isNotEmpty(patients));
        assertEquals(1, patients.size());
        assertEquals(healthId, patients.get(0).getHealthId());
    }

    @Test
    public void shouldUpdatePhoneNumberMappingWhenPhoneNumberIsUpdated() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        PhoneNumber phoneNumber1 = new PhoneNumber();
        phoneNumber1.setNumber("100000000");
        patient.setPhoneNumber(phoneNumber1);
        patient.setAddress(new Address("1", "2", "3"));
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        PhoneNumber phoneNumber2 = new PhoneNumber();
        phoneNumber2.setNumber("200000000");
        patient.setPhoneNumber(phoneNumber2);
        patientRepository.update(patient, healthId);

        PatientData requestData = new PatientData();
        requestData.setHealthId(healthId);
        requestData.setPhoneNumber(phoneNumber2);
        PatientData existingPatient = patientRepository.findByHealthId(healthId);
        patientRepository.processPendingApprovals(requestData, existingPatient, true);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertNotNull(updatedPatient.getPhoneNumber());
        assertEquals(phoneNumber2, updatedPatient.getPhoneNumber());

        SearchQuery query = new SearchQuery();
        query.setPhone_no(phoneNumber1.getNumber());
        assertTrue(isEmpty(patientRepository.findAllByQuery(query)));

        assertSearchByPhoneNumber(phoneNumber2, healthId);
    }

    @Test
    public void shouldUpdateAppropriatePhoneNumberMappingWhenMultiplePatientsWithSamePhoneNumberExist() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        PhoneNumber phoneNumber1 = new PhoneNumber();
        phoneNumber1.setNumber("100000000");
        patient.setPhoneNumber(phoneNumber1);
        patient.setAddress(new Address("1", "2", "3"));
        String healthId1 = patientRepository.create(patient).getId();
        assertNotNull(healthId1);

        patient.setGivenName("Jane");
        String healthId2 = patientRepository.create(patient).getId();
        assertNotNull(healthId2);

        PhoneNumber phoneNumber2 = new PhoneNumber();
        phoneNumber2.setNumber("200000000");
        patient.setPhoneNumber(phoneNumber2);
        patientRepository.update(patient, healthId2);

        PatientData requestData = new PatientData();
        requestData.setHealthId(healthId2);
        requestData.setPhoneNumber(phoneNumber2);
        PatientData existingPatient = patientRepository.findByHealthId(healthId2);
        patientRepository.processPendingApprovals(requestData, existingPatient, true);

        PatientData patient2 = patientRepository.findByHealthId(healthId2);
        assertNotNull(patient2);
        assertEquals(phoneNumber2, patient2.getPhoneNumber());

        assertSearchByPhoneNumber(phoneNumber1, healthId1);
        assertSearchByPhoneNumber(phoneNumber2, healthId2);
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

    @Test
    public void shouldUpdateNameMappingWhenGivenNameIsUpdated() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setAddress(new Address("10", "20", "30"));
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        patient.setGivenName("Jane");
        patientRepository.update(patient, healthId);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals("Jane", updatedPatient.getGivenName());

        SearchQuery query = new SearchQuery();
        query.setGiven_name("John");
        query.setPresent_address("102030");
        assertTrue(isEmpty(patientRepository.findAllByQuery(query)));

        assertSearchByNameAndAddress("Jane", "102030", healthId);
    }

    @Test
    public void shouldUpdateNameMappingWhenPresentAddressIsUpdated() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        patient.setAddress(new Address("10", "20", "30"));
        String healthId = patientRepository.create(patient).getId();
        assertNotNull(healthId);

        Address address = new Address("11", "22", "33");
        patient.setAddress(address);
        patientRepository.update(patient, healthId);

        PatientData requestData = new PatientData();
        requestData.setHealthId(healthId);
        requestData.setAddress(address);
        patientRepository.processPendingApprovals(requestData, patientRepository.findByHealthId(healthId), true);

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        assertNotNull(updatedPatient);
        assertEquals(address, updatedPatient.getAddress());

        SearchQuery query = new SearchQuery();
        query.setGiven_name("John");
        query.setPresent_address("102030");
        assertTrue(isEmpty(patientRepository.findAllByQuery(query)));

        assertSearchByNameAndAddress("John", "112233", healthId);
    }

    @Test
    public void shouldUpdateAppropriateNameMappingWhenMultiplePatientsWithSameNameAndAddressExist() {
        PatientData patient = new PatientData();
        patient.setGivenName("John");
        patient.setSurName("Doe");
        Address address = new Address("10", "20", "30");
        patient.setAddress(address);
        String healthId1 = patientRepository.create(patient).getId();
        assertNotNull(healthId1);

        String healthId2 = patientRepository.create(patient).getId();
        assertNotNull(healthId2);

        patient.setGivenName("Jane");
        patientRepository.update(patient, healthId2);

        assertSearchByNameAndAddress("John", "102030", healthId1);
        assertSearchByNameAndAddress("Jane", "102030", healthId2);
    }

    private void assertSearchByNameAndAddress(String givenName, String address, String healthId) {
        SearchQuery query = new SearchQuery();
        query.setGiven_name(givenName);
        query.setPresent_address(address);
        List<PatientData> patients = patientRepository.findAllByQuery(query);
        assertTrue(isNotEmpty(patients));
        assertEquals(1, patients.size());
        assertEquals(healthId, patients.get(0).getHealthId());
    }

    @Test
    public void shouldCreateUpdateLogsWhenPatientIsUpdated() {
        PatientData patient = createPatient();
        String healthId = patientRepository.create(patient).getId();
        Date since = new Date();

        assertUpdateLogEntry(healthId, since, false);

        patient.setHealthId(healthId);
        patient.setGivenName("Harry");
        patient.setAddress(new Address("99", "88", "77"));
        patientRepository.update(patient, patient.getHealthId());

        assertUpdateLogEntry(healthId, since, true);
    }

    private void assertUpdateLogEntry(String healthId, Date since, boolean shouldfind) {
        List<PatientUpdateLog> patientUpdateLogs = cassandraOps.select(buildFindUpdateLogStmt(since, 1),
                PatientUpdateLog.class);
        if (shouldfind) {
            assertTrue(healthId.equals(patientUpdateLogs.get(0).getHealthId()));
        } else {
            assertEquals(0, patientUpdateLogs.size());
        }
    }

    @Test
    public void shouldFindByHealthIdsInTheOrderIdsArePassed() {
        PatientData patient = createPatient();
        patient.setNationalId(null);
        patient.setBirthRegistrationNumber(null);
        patient.setUid(null);

        String healthId1 = patientRepository.create(patient).getId();
        assertTrue(isNotBlank(healthId1));
        String healthId2 = patientRepository.create(patient).getId();
        assertTrue(isNotBlank(healthId2));
        String healthId3 = patientRepository.create(patient).getId();
        assertTrue(isNotBlank(healthId3));

        List<PatientData> patients = patientRepository.findByHealthId(asList(healthId1, healthId2, healthId3));
        assertEquals(healthId1, patients.get(0).getHealthId());
        assertEquals(healthId2, patients.get(1).getHealthId());
        assertEquals(healthId3, patients.get(2).getHealthId());
    }

    @Test
    public void shouldFindAllPatientsByCatchment() throws Exception {
        List<String> healthIds = new ArrayList<>();
        PatientData patient = createPatient();
        Address address = new Address("10", "20", "30");
        address.setCityCorporationId("40");

        for (int i = 1; i <= 5; i++) {
            address.setUnionOrUrbanWardId("5" + i);
            address.setRuralWardId("6" + i);
            patient.setAddress(address);
            healthIds.add(patientRepository.create(patient).getId());
            Thread.sleep(0, 10);
        }


        Catchment catchment = new Catchment("10", "20", "30");
        catchment.setCityCorpId("40");
        Date after = cassandraOps.selectOneById(Patient.class, healthIds.get(0)).getUpdatedAt();
        int limit = 3;
        List<PatientData> patients = patientRepository.findAllByCatchment(catchment, after, limit);

        assertTrue(isNotEmpty(patients));
        assertEquals(limit, patients.size());
        assertEquals(healthIds.get(1), patients.get(0).getHealthId());
        assertEquals(healthIds.get(2), patients.get(1).getHealthId());
        assertEquals(healthIds.get(3), patients.get(2).getHealthId());
    }

    @Test
    public void shouldReturnEmptyCollectionIfNoPatientFoundInCatchment() {
        Catchment catchment = new Catchment("10", "20", "30");
        catchment.setCityCorpId("40");
        List<PatientData> patients = patientRepository.findAllByCatchment(catchment, new Date(), 100);
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
        Address address = new Address("10", "20", "30");
        address.setCityCorporationId("40");
        address.setUnionOrUrbanWardId("50");
        address.setRuralWardId("60");
        createPatientData.setAddress(address);
        String healthId = patientRepository.create(createPatientData).getId();

        PatientData patientData = new PatientData();
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

        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), null, null, 25);
        assertEquals(3, mappings.size());
        PendingApprovalMapping mapping1 = mappings.get(0);
        PendingApprovalMapping mapping2 = mappings.get(1);
        PendingApprovalMapping mapping3 = mappings.get(2);

        assertEquals("h102", mapping1.getHealthId());
        assertEquals("h103", mapping2.getHealthId());
        assertEquals("h105", mapping3.getHealthId());

        Date date1 = new Date(UUIDs.unixTimestamp(mapping1.getLastUpdated()));
        Date date2 = new Date(UUIDs.unixTimestamp(mapping2.getLastUpdated()));
        Date date3 = new Date(UUIDs.unixTimestamp(mapping3.getLastUpdated()));

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
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), after, null, 25);
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

        UUID before = entities.get(3).getLastUpdated();
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), null, before, 25);
        assertEquals(3, mappings.size());
        assertEquals("h101", mappings.get(0).getHealthId());
        assertEquals("h102", mappings.get(1).getHealthId());
        assertEquals("h103", mappings.get(2).getHealthId());
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
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), after, before, 25);
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
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), null, null, 5);
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
        PatientData patientData = new PatientData();
        patientData.setGender("F");
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
        assertEquals("10000059", fieldDetails.getFacilityId());

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
        String healthId = patientRepository.create(data).getId();
        PatientData patientData = new PatientData();
        patientData.setGender("F");
        patientRepository.update(patientData, healthId);
        Thread.sleep(0, 10);
        patientData = new PatientData();
        patientData.setGender("O");
        patientRepository.update(patientData, healthId);
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
        assertEquals("10000059", fieldDetails1.getFacilityId());

        PendingApprovalFieldDetails fieldDetails2 = fieldDetailsIterator.next();
        assertEquals("F", fieldDetails2.getValue());
        assertEquals("10000059", fieldDetails2.getFacilityId());

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

        PatientData patientData = new PatientData();
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

        PatientData patientData = new PatientData();
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

        patientData = new PatientData();
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

        PatientData patientData = new PatientData();
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

        patientData = new PatientData();
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

        PatientData patientData = new PatientData();
        patientData.setGender("F");
        patientData.setOccupation("05");
        patientRepository.update(patientData, healthId);
        Thread.sleep(0, 10);

        patientData = new PatientData();
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

        patientData = new PatientData();
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

        PatientData patientData = new PatientData();
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

        patientData = new PatientData();
        patientData.setHealthId(healthId);
        patientData.setPhoneNumber(phoneNumber);
        PatientData existingPatientData = patientRepository.findByHealthId(healthId);
        return patientRepository.processPendingApprovals(patientData, existingPatientData, shouldAccept);
    }

    @Test
    public void shouldUpdateCatchmentMappingWhenPresentAddressIsMarkedForApprovalAndUpdatedAfterApproval() {
        String healthId = patientRepository.create(data).getId();
        List<PatientData> patients = patientRepository.findAllByCatchment(data.getCatchment(), null, 100);
        assertTrue(isNotEmpty(patients));
        assertEquals(1, patients.size());
        assertEquals(healthId, patients.get(0).getHealthId());

        PatientData updateRequest = new PatientData();
        Address newAddress = new Address("10", "20", "30");
        updateRequest.setAddress(newAddress);
        updateRequest.setGender("O");
        patientRepository.update(updateRequest, healthId);

        assertTrue(isNotEmpty(patientRepository.findAllByCatchment(data.getCatchment(), null, 100)));
        assertTrue(isEmpty(patientRepository.findAllByCatchment(updateRequest.getCatchment(), null, 100)));

        PatientData updatedPatient = patientRepository.findByHealthId(healthId);
        patientRepository.processPendingApprovals(updateRequest, updatedPatient, true);

        assertTrue(isEmpty(patientRepository.findAllByCatchment(data.getCatchment(), null, 100)));

        List<CatchmentMapping> catchmentMappings = cassandraOps.select
                (buildFindCatchmentMappingsStmt(patientRepository.findByHealthId(healthId)), CatchmentMapping.class);
        assertTrue(isNotEmpty(catchmentMappings));
        assertEquals(2, catchmentMappings.size());
        assertEquals(healthId, catchmentMappings.iterator().next().getHealthId());
    }
}