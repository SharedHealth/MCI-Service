package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.exception.HealthIDExistException;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.model.PendingApprovalMapping;
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
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;
import static org.sharedhealth.mci.web.utils.JsonConstants.PHONE_NUMBER;
import static org.sharedhealth.mci.web.utils.PatientDataConstants.PATIENT_STATUS_ALIVE;

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
    public Catchment catchment = new Catchment(divisionId, districtId, upazilaId);

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        data = createPatient();
    }

    @Test
    public void shouldCreatePatientAndMappings() {
        String id = patientRepository.create(data).getId();
        assertNotNull(id);

        String healthId = cassandraOps.queryForObject(buildFindByNidStmt(nationalId), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOps.queryForObject(buildFindByBrnStmt(birthRegistrationNumber), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOps.queryForObject(buildFindByUidStmt(uid), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOps.queryForObject(buildFindByPhoneNumberStmt(phoneNumber), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOps.queryForObject(buildFindByNameStmt(divisionId, districtId, upazilaId,
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
        data.setStatus(PATIENT_STATUS_ALIVE);

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
        data.setOccupation("03");
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
        mapping.setHealthId(healthId);
        mapping.setDivisionId("10");
        mapping.setDistrictId("20");
        mapping.setUpazilaId(upazilaId);
        mapping.setLastUpdated(UUIDs.timeBased());
        return mapping;
    }

    @Test
    public void shouldCreatePendingApprovalInPatientAndMappingTables_IfPatientDoesNotHaveAnyPendingApproval() throws Exception {
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

        List<PendingApprovalMapping> mappings = cassandraOps.select(select().from(CF_PENDING_APPROVAL_MAPPING), PendingApprovalMapping.class);
        assertEquals(1, mappings.size());
        PendingApprovalMapping mapping = mappings.get(0);
        assertEquals(healthId, mapping.getHealthId());
        assertEquals(data.getAddress().getDivisionId(), mapping.getDivisionId());
        assertEquals(data.getAddress().getDistrictId(), mapping.getDistrictId());
        assertEquals(data.getAddress().getUpazilaId(), mapping.getUpazilaId());
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

        List<PendingApprovalMapping> mappings = cassandraOps.select(select().from(CF_PENDING_APPROVAL_MAPPING), PendingApprovalMapping.class);
        assertEquals(1, mappings.size());
        PendingApprovalMapping mapping = mappings.get(0);
        assertEquals(healthId, mapping.getHealthId());
        assertEquals(data.getAddress().getDivisionId(), mapping.getDivisionId());
        assertEquals(data.getAddress().getDistrictId(), mapping.getDistrictId());
        assertEquals(data.getAddress().getUpazilaId(), mapping.getUpazilaId());
        assertEquals(fieldDetailsMap.keySet().iterator().next(), mapping.getLastUpdated());
    }

    @Test
    public void shouldBeAbleToAddPendingApprovalsForMultipleFields() {
        PatientData patientData = new PatientData();
        patientData.setGivenName("John Doe");
        patientData.setGender("O");
        patientData.setOccupation("07");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("40000000777");
        patientData.setPhoneNumber(phoneNumber);

        String healthId = patientRepository.create(data).getId();
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

        assertTrue(isEmpty(patientRepository.findPendingApprovalMapping(catchment, null, null, 100)));
    }

    @Test
    public void shouldBeAbleToRejectPendingApprovalsWhenPatientHasOnePendingApproval() throws Exception {
        String healthId = processPendingApprovalsWhenPatientHasOnePendingApproval(false);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertEquals(data.getGender(), patient.getGender());
        assertTrue(isEmpty(patient.getPendingApprovals()));

        assertTrue(isEmpty(patientRepository.findPendingApprovalMapping(catchment, null, null, 100)));
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

        List<PendingApprovalMapping> pendingApprovalMappings = patientRepository.findPendingApprovalMapping(catchment, null, null, 100);
        assertEquals(1, pendingApprovalMappings.size());
        PendingApprovalMapping pendingApprovalMapping = pendingApprovalMappings.iterator().next();
        assertEquals(healthId, pendingApprovalMapping.getHealthId());
        assertEquals(patientRepository.findLatestUuid(pendingApprovals), pendingApprovalMapping.getLastUpdated());

        patientData = new PatientData();
        patientData.setHealthId(healthId);
        patientData.setGender("F");
        PatientData existingPatientData = patientRepository.findByHealthId(healthId);
        return patientRepository.processPendingApprovals(patientData, existingPatientData, catchment, shouldAccept);
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

        List<PendingApprovalMapping> pendingApprovalMappings = patientRepository.findPendingApprovalMapping(catchment, null, null, 100);
        assertEquals(1, pendingApprovalMappings.size());
        PendingApprovalMapping pendingApprovalMapping = pendingApprovalMappings.iterator().next();
        assertEquals(healthId, pendingApprovalMapping.getHealthId());
        assertEquals(patientRepository.findLatestUuid(pendingApprovals), pendingApprovalMapping.getLastUpdated());
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

        List<PendingApprovalMapping> pendingApprovalMappings = patientRepository.findPendingApprovalMapping(catchment, null, null, 100);
        assertEquals(1, pendingApprovalMappings.size());
        PendingApprovalMapping pendingApprovalMapping = pendingApprovalMappings.iterator().next();
        assertEquals(healthId, pendingApprovalMapping.getHealthId());
        assertEquals(patientRepository.findLatestUuid(pendingApprovals), pendingApprovalMapping.getLastUpdated());
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

        Catchment catchment = new Catchment(divisionId, districtId, upazilaId);
        List<PendingApprovalMapping> pendingApprovalMappings = patientRepository.findPendingApprovalMapping(catchment, null, null, 100);
        assertEquals(1, pendingApprovalMappings.size());
        PendingApprovalMapping pendingApprovalMapping = pendingApprovalMappings.iterator().next();
        assertEquals(healthId, pendingApprovalMapping.getHealthId());
        assertEquals(patientRepository.findLatestUuid(pendingApprovals), pendingApprovalMapping.getLastUpdated());

        patientData = new PatientData();
        patientData.setHealthId(healthId);
        patientData.setGender("F");
        patientData.setPhoneNumber(phoneNumber);
        PatientData existingPatientData = patientRepository.findByHealthId(healthId);
        return patientRepository.processPendingApprovals(patientData, existingPatientData, catchment, shouldAccept);
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

        List<PendingApprovalMapping> pendingApprovalMappings = patientRepository.findPendingApprovalMapping(catchment, null, null, 100);
        assertEquals(1, pendingApprovalMappings.size());
        PendingApprovalMapping pendingApprovalMapping = pendingApprovalMappings.iterator().next();
        assertEquals(healthId, pendingApprovalMapping.getHealthId());
        assertEquals(patientRepository.findLatestUuid(pendingApprovals), pendingApprovalMapping.getLastUpdated());
    }

    @Test
    public void shouldBeAbleToRejectPendingApprovalsWhenPatientHasMultiplePendingApprovalsForMultipleFields() throws Exception {
        String healthId = processPendingApprovalsWhenPatientHasMultiplePendingApprovalsForMultipleFields(false);

        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        assertEquals(data.getGender(), patient.getGender());
        assertEquals(data.getOccupation(), patient.getOccupation());

        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        PendingApproval pendingApproval = pendingApprovals.iterator().next();
        assertNotNull(pendingApproval);
        assertEquals(OCCUPATION, pendingApproval.getName());

        List<PendingApprovalMapping> pendingApprovalMappings = patientRepository.findPendingApprovalMapping(catchment, null, null, 100);
        assertEquals(1, pendingApprovalMappings.size());
        PendingApprovalMapping pendingApprovalMapping = pendingApprovalMappings.iterator().next();
        assertEquals(healthId, pendingApprovalMapping.getHealthId());
        assertEquals(patientRepository.findLatestUuid(pendingApprovals), pendingApprovalMapping.getLastUpdated());
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

        Catchment catchment = new Catchment(divisionId, districtId, upazilaId);
        List<PendingApprovalMapping> pendingApprovalMappings = patientRepository.findPendingApprovalMapping(catchment, null, null, 100);
        assertEquals(1, pendingApprovalMappings.size());
        PendingApprovalMapping pendingApprovalMapping = pendingApprovalMappings.iterator().next();
        assertEquals(healthId, pendingApprovalMapping.getHealthId());
        assertEquals(patientRepository.findLatestUuid(pendingApprovals), pendingApprovalMapping.getLastUpdated());

        patientData = new PatientData();
        patientData.setHealthId(healthId);
        patientData.setGender("F");
        PatientData existingPatientData = patientRepository.findByHealthId(healthId);
        return patientRepository.processPendingApprovals(patientData, existingPatientData, catchment, shouldAccept);
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

        List<PendingApprovalMapping> pendingApprovalMappings = patientRepository.findPendingApprovalMapping(catchment, null, null, 100);
        assertEquals(0, pendingApprovalMappings.size());
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

        List<PendingApprovalMapping> pendingApprovalMappings = patientRepository.findPendingApprovalMapping(catchment, null, null, 100);
        assertEquals(0, pendingApprovalMappings.size());
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

        Catchment catchment = new Catchment(divisionId, districtId, upazilaId);
        List<PendingApprovalMapping> pendingApprovalMappings = patientRepository.findPendingApprovalMapping(catchment, null, null, 100);
        assertEquals(1, pendingApprovalMappings.size());
        PendingApprovalMapping pendingApprovalMapping = pendingApprovalMappings.iterator().next();
        assertEquals(healthId, pendingApprovalMapping.getHealthId());
        assertEquals(patientRepository.findLatestUuid(pendingApprovals), pendingApprovalMapping.getLastUpdated());

        patientData = new PatientData();
        patientData.setHealthId(healthId);
        patientData.setPhoneNumber(phoneNumber);
        PatientData existingPatientData = patientRepository.findByHealthId(healthId);
        return patientRepository.processPendingApprovals(patientData, existingPatientData, catchment, shouldAccept);
    }

    @After
    public void teardown() {
        cassandraOps.execute("truncate " + CF_PATIENT);
        cassandraOps.execute("truncate " + CF_NID_MAPPING);
        cassandraOps.execute("truncate " + CF_BRN_MAPPING);
        cassandraOps.execute("truncate " + CF_UID_MAPPING);
        cassandraOps.execute("truncate " + CF_PHONE_NUMBER_MAPPING);
        cassandraOps.execute("truncate " + CF_NAME_MAPPING);
        cassandraOps.execute("truncate " + CF_PENDING_APPROVAL_MAPPING);
    }
}