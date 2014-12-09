package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfig;
import org.sharedhealth.mci.web.exception.HealthIDExistException;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.model.PendingApprovalMapping;
import org.sharedhealth.mci.web.model.PendingApprovalRequest;
import org.sharedhealth.mci.web.utils.JsonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientRepositoryIT {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cassandraOperations;

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

    @Test
    public void shouldCreatePatientAndMappings() {
        String id = patientRepository.create(data).getId();
        assertNotNull(id);

        String healthId = cassandraOperations.queryForObject(buildFindByNidStmt(nationalId), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOperations.queryForObject(buildFindByBrnStmt(birthRegistrationNumber), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOperations.queryForObject(buildFindByUidStmt(uid), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOperations.queryForObject(buildFindByPhoneNumberStmt(phoneNumber), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOperations.queryForObject(buildFindByNameStmt(divisionId, districtId, upazilaId,
                givenName.toLowerCase(), surname.toLowerCase()), String.class);
        assertEquals(healthId, id);
    }

    @Test
    public void shouldFindPatientWithMatchingGeneratedHealthId() throws ExecutionException, InterruptedException {
        MCIResponse mciResponse = patientRepository.create(data);
        PatientData p = patientRepository.findByHealthId(mciResponse.id);
        assertNotNull(p);
        data.setHealthId(mciResponse.id);
        data.setCreatedAt(p.getCreatedAt());
        data.setUpdatedAt(p.getUpdatedAt());
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
        assertPatient(savedPatient, data);
    }

    @Test
    public void shouldMarkFieldsForApprovalAsConfigured() throws Exception {
        PatientData data = createPatient();
        MCIResponse mciResponseForCreate = patientRepository.create(data);
        assertEquals(201, mciResponseForCreate.getHttpStatus());
        String healthId = mciResponseForCreate.getId();
        data.setHealthId(healthId);

        data.setGender("F");
        MCIResponse mciResponseForUpdate = patientRepository.update(data, data.getHealthId());
        assertEquals(202, mciResponseForUpdate.getHttpStatus());
        PatientData savedPatient = patientRepository.findByHealthId(healthId);
        assertEquals("M", savedPatient.getGender());

        Map<UUID, String> pendingApprovals = savedPatient.getPendingApprovals();
        assertTrue(pendingApprovals != null && pendingApprovals.size() == 1);
        PendingApprovalRequest pendingApprovalRequest = new ObjectMapper().readValue(pendingApprovals.values().iterator().next(), PendingApprovalRequest.class);
        assertNotNull(pendingApprovalRequest);
        Map<String, String> fields = pendingApprovalRequest.getFields();
        assertEquals("F", fields.get("gender"));

        List<PendingApprovalMapping> mappings = cassandraOperations.select(select().from(CF_PENDING_APPROVAL_MAPPING).toString(), PendingApprovalMapping.class);
        assertTrue(isNotEmpty(mappings));
        PendingApprovalMapping mapping = mappings.get(0);
        assertEquals(data.getAddress().getDivisionId(), mapping.getDivisionId());
        assertEquals(data.getAddress().getDistrictId(), mapping.getDistrictId());
        assertEquals(data.getAddress().getUpazilaId(), mapping.getUpazilaId());
        assertEquals(data.getHealthId(), mapping.getHealthId());
    }

    @Test
    public void shouldFindByHealthIdsInTheOrderIdsArePassed() {
        PatientData patient = new PatientData();
        data.setGivenName("Scott");
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
    public void shouldReturnAllPatientsBelongsToSpecificLocation() throws Exception {
        generatePatientSet();

        assertPatientsFoundByCatchment("1004092005", 10);
        assertPatientsFoundByCatchment("1004092001", 5);
        assertPatientsFoundByCatchment("10040920", 15);
        assertPatientsFoundByCatchment("1004092006", 0);
    }

    private void assertPatientsFoundByCatchment(String location, int expectedRecordCount) throws InterruptedException, ExecutionException {
        List<PatientData> patients;
        patients = patientRepository.findAllByLocation(location, null, 0, null);
        assertEquals(expectedRecordCount, patients.size());
    }

    private void generatePatientSet() throws Exception {
        String json = asString("jsons/patient/required_only_payload.json");
        PatientData patientData = new ObjectMapper().readValue(json, PatientData.class);
        createMultiplePatients(patientData, 10);
        patientData.setAddress(createAddress("10", "04", "09", "20", "01"));
        createMultiplePatients(patientData, 5);
    }

    private void createMultiplePatients(PatientData data, int n) throws Exception {
        for (int x = 0; x < n; x++) {
            patientRepository.create(data);
            data.setHealthId(null);
        }
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

    private PatientData createPatient() {
        PatientData data = new PatientData();
        data.setNationalId(nationalId);
        data.setBirthRegistrationNumber(birthRegistrationNumber);
        data.setUid(uid);
        data.setGivenName(givenName);
        data.setSurName(surname);
        data.setDateOfBirth("2014-12-01");
        data.setGender("M");
        data.setOccupation("salaried");
        data.setEducationLevel("BA");
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber(phoneNumber);
        data.setPhoneNumber(phone);

        Address address = createAddress(divisionId, districtId, upazilaId, "20", "01");
        data.setAddress(address);

        return data;
    }

    private Address createAddress(String division, String district, String upazilla, String cityCorp, String ward) {
        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId(division);
        address.setDistrictId(district);
        address.setUpazilaId(upazilla);
        address.setCityCorporationId(cityCorp);
        address.setUnionOrUrbanWardId(ward);

        return address;
    }

    @Test
    public void shouldFindAllPendingApprovalMappingsInDescendingOrderOfCreationTime() throws Exception {
        cassandraOperations.insert(asList(buildPendingApprovalMapping("31", "h101"),
                buildPendingApprovalMapping("30", "h102"),
                buildPendingApprovalMapping("30", "h103"),
                buildPendingApprovalMapping("32", "h104"),
                buildPendingApprovalMapping("30", "h105")));

        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), null, 25);
        assertEquals(3, mappings.size());
        PendingApprovalMapping mapping1 = mappings.get(0);
        PendingApprovalMapping mapping2 = mappings.get(1);
        PendingApprovalMapping mapping3 = mappings.get(2);

        assertEquals("h105", mapping1.getHealthId());
        assertEquals("h103", mapping2.getHealthId());
        assertEquals("h102", mapping3.getHealthId());

        Date date1 = new Date(UUIDs.unixTimestamp(mapping1.getCreatedAt()));
        Date date2 = new Date(UUIDs.unixTimestamp(mapping2.getCreatedAt()));
        Date date3 = new Date(UUIDs.unixTimestamp(mapping3.getCreatedAt()));

        assertTrue(date1.after(date2));
        assertTrue(date2.after(date3));
    }

    @Test
    public void shouldFindPendingApprovalMappingsSinceGivenTime() throws Exception {
        List<PendingApprovalMapping> entities = asList(buildPendingApprovalMapping("30", "h101"),
                buildPendingApprovalMapping("30", "h102"),
                buildPendingApprovalMapping("30", "h103"),
                buildPendingApprovalMapping("30", "h104"),
                buildPendingApprovalMapping("30", "h105"));
        cassandraOperations.insert(entities);

        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(
                new Catchment("10", "20", "30"), entities.get(4).getCreatedAt(), 25);
        assertEquals(4, mappings.size());
        assertEquals("h104", mappings.get(0).getHealthId());
        assertEquals("h103", mappings.get(1).getHealthId());
        assertEquals("h102", mappings.get(2).getHealthId());
        assertEquals("h101", mappings.get(3).getHealthId());
    }

    @Test
    public void shouldFindPendingApprovalMappingsWithLimit() throws Exception {
        List<PendingApprovalMapping> entities = asList(buildPendingApprovalMapping("30", "h101"),
                buildPendingApprovalMapping("30", "h102"),
                buildPendingApprovalMapping("30", "h103"),
                buildPendingApprovalMapping("30", "h104"),
                buildPendingApprovalMapping("30", "h105"));
        cassandraOperations.insert(entities);
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), null, 5);
        assertEquals(5, mappings.size());
        mappings = patientRepository.findPendingApprovalMapping(new Catchment("10", "20", "30"), null, 3);
        assertEquals(3, mappings.size());
    }

    private PendingApprovalMapping buildPendingApprovalMapping(String upazilaId, String healthId) throws InterruptedException {
        Thread.sleep(0, 10);
        PendingApprovalMapping mapping = new PendingApprovalMapping();
        mapping.setHealthId(healthId);
        mapping.setDivisionId("10");
        mapping.setDistrictId("20");
        mapping.setUpazilaId(upazilaId);
        mapping.setCreatedAt(UUIDs.timeBased());
        return mapping;
    }

    @Test
    public void shouldCreatePendingApprovalInPatientAndMappingTables_IfPatientDoesNotHaveAnyPendingApproval() throws Exception {
        String healthId = patientRepository.create(data).getId();
        PatientData patientData = new PatientData();
        patientData.setGender("F");
        patientRepository.update(patientData, healthId);
        Patient patient = cassandraOperations.selectOneById(Patient.class, healthId);

        Map<UUID, String> pendingApprovalsMap = patient.getPendingApprovals();
        assertNotNull(pendingApprovalsMap);
        assertEquals(1, pendingApprovalsMap.size());
        
        String pendingApprovalJson = pendingApprovalsMap.values().iterator().next();
        PendingApprovalRequest pendingApprovalRequest = new ObjectMapper().readValue(pendingApprovalJson.getBytes(), PendingApprovalRequest.class);
        assertEquals("10000059", pendingApprovalRequest.getFacilityId());

        Map<String, String> expectedPendingApprovalFields = new HashMap<>();
        expectedPendingApprovalFields.put(JsonConstants.GENDER, "F");
        assertEquals(expectedPendingApprovalFields, pendingApprovalRequest.getFields());

        List<PendingApprovalMapping> mappings = cassandraOperations.select(select().from(CF_PENDING_APPROVAL_MAPPING), PendingApprovalMapping.class);
        assertEquals(1, mappings.size());
        PendingApprovalMapping mapping = mappings.get(0);
        assertEquals(healthId, mapping.getHealthId());
        assertEquals(data.getAddress().getDivisionId(), mapping.getDivisionId());
        assertEquals(data.getAddress().getDistrictId(), mapping.getDistrictId());
        assertEquals(data.getAddress().getUpazilaId(), mapping.getUpazilaId());
        assertEquals(pendingApprovalsMap.keySet().iterator().next(), mapping.getCreatedAt());
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
        Patient patient = cassandraOperations.selectOneById(Patient.class, healthId);

        Map<UUID, String> pendingApprovalsMap = patient.getPendingApprovals();
        assertNotNull(pendingApprovalsMap);
        assertEquals(2, pendingApprovalsMap.size());

        List<PendingApprovalMapping> mappings = cassandraOperations.select(select().from(CF_PENDING_APPROVAL_MAPPING), PendingApprovalMapping.class);
        assertEquals(1, mappings.size());
        PendingApprovalMapping mapping = mappings.get(0);
        assertEquals(healthId, mapping.getHealthId());
        assertEquals(data.getAddress().getDivisionId(), mapping.getDivisionId());
        assertEquals(data.getAddress().getDistrictId(), mapping.getDistrictId());
        assertEquals(data.getAddress().getUpazilaId(), mapping.getUpazilaId());
        UUID latestUuid = patientRepository.findLatestUuid(pendingApprovalsMap);
        assertEquals(latestUuid, mapping.getCreatedAt());
    }

    @After
    public void teardown() {
        cassandraOperations.execute("truncate " + CF_PATIENT);
        cassandraOperations.execute("truncate " + CF_NID_MAPPING);
        cassandraOperations.execute("truncate " + CF_BRN_MAPPING);
        cassandraOperations.execute("truncate " + CF_UID_MAPPING);
        cassandraOperations.execute("truncate " + CF_PHONE_NUMBER_MAPPING);
        cassandraOperations.execute("truncate " + CF_NAME_MAPPING);
        cassandraOperations.execute("truncate " + CF_PENDING_APPROVAL_MAPPING);
    }
}