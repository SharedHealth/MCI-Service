package org.sharedhealth.mci.web.infrastructure.persistence;

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
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;
import static org.springframework.http.HttpStatus.ACCEPTED;

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

    private PatientMapper patientDto;
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
        patientDto = createPatient();
    }

    @Test
    public void shouldCreatePatientAndMappings() {
        String id = patientRepository.create(patientDto).getId();
        assertNotNull(id);

        String healthId = cassandraOperations.queryForObject(buildFindByNidQuery(nationalId), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOperations.queryForObject(buildFindByBrnQuery(birthRegistrationNumber), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOperations.queryForObject(buildFindByUidQuery(uid), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOperations.queryForObject(buildFindByPhoneNumberQuery(phoneNumber), String.class);
        assertEquals(healthId, id);

        healthId = cassandraOperations.queryForObject(buildFindByNameQuery(divisionId, districtId, upazilaId, givenName, surname), String.class);
        assertEquals(healthId, id);
    }

    @Test
    public void shouldFindPatientWithMatchingGeneratedHealthId() throws ExecutionException, InterruptedException {
        MCIResponse mciResponse = patientRepository.create(patientDto);
        PatientMapper p = patientRepository.findByHealthId(mciResponse.id);
        assertNotNull(p);
        patientDto.setHealthId(mciResponse.id);
        patientDto.setCreatedAt(p.getCreatedAt());
        patientDto.setUpdatedAt(p.getUpdatedAt());
        assertEquals(patientDto, p);
    }

    @Test(expected = PatientNotFoundException.class)
    public void shouldThrowException_IfPatientDoesNotExistForGivenHealthId() {
        patientRepository.findByHealthId(UUID.randomUUID().toString());
    }

    @Test(expected = HealthIDExistException.class)
    public void shouldThrowException_IfHealthIdProvidedForCreate() throws ExecutionException, InterruptedException {
        patientDto.setHealthId("12");
        patientRepository.create(patientDto);
    }

    @Test
    public void shouldReturnAccepted_IfPatientExistWithProvidedTwoIdFieldsOnCreate() throws ExecutionException, InterruptedException {
        patientRepository.create(patientDto);
        patientDto.setHealthId(null);
        MCIResponse mciResponse = patientRepository.create(patientDto);
        assertEquals(mciResponse.getHttpStatus(), ACCEPTED.value());
    }

    @Test
    public void shouldFindPatientWithMatchingNationalId() throws ExecutionException, InterruptedException {

        MCIResponse mciResponse = patientRepository.create(patientDto);
        final PatientMapper p = patientRepository.findByNationalId(nationalId).get();
        assertNotNull(p);
        patientDto.setHealthId(mciResponse.id);
        patientDto.setCreatedAt(p.getCreatedAt());
        patientDto.setUpdatedAt(p.getUpdatedAt());
        assertEquals(patientDto, p);
    }


    @Test
    public void shouldFindPatientWithMatchingBirthRegistrationNumber() throws ExecutionException, InterruptedException {

        MCIResponse mciResponse = patientRepository.create(patientDto);
        final PatientMapper p = patientRepository.findByBirthRegistrationNumber(birthRegistrationNumber).get();
        assertNotNull(p);
        patientDto.setHealthId(mciResponse.id);
        patientDto.setCreatedAt(p.getCreatedAt());
        patientDto.setUpdatedAt(p.getUpdatedAt());
        assertEquals(patientDto, p);
    }

    @Test
    public void shouldFindPatientWithMatchingUid() throws ExecutionException, InterruptedException {
        MCIResponse mciResponse = patientRepository.create(patientDto);

        final PatientMapper p = patientRepository.findByUid(uid).get();
        assertNotNull(p);
        patientDto.setHealthId(mciResponse.id);
        patientDto.setCreatedAt(p.getCreatedAt());
        patientDto.setUpdatedAt(p.getUpdatedAt());
        assertEquals(patientDto, p);
    }

    @Test(expected = PatientNotFoundException.class)
    public void shouldThrowErrorIfPatientNotFound() throws Exception {
        patientRepository.update(new PatientMapper(), "1");
    }

    @Test
    public void shouldUpdatePatient() throws Exception {
        PatientMapper patientDto = createPatient();
        MCIResponse mciResponseForCreate = patientRepository.create(patientDto);
        assertEquals(201, mciResponseForCreate.getHttpStatus());
        String healthId = mciResponseForCreate.getId();
        patientDto.setHealthId(healthId);
        patientDto.setGivenName("Danny");
        MCIResponse mciResponseForUpdate = patientRepository.update(patientDto, patientDto.getHealthId());
        assertEquals(202, mciResponseForUpdate.getHttpStatus());
        PatientMapper savedPatient = patientRepository.findByHealthId(healthId);
        assertPatient(savedPatient, patientDto);
    }

    @Test
    public void shouldNotUpdateFieldsThatNeedApprovalPatient() throws Exception {
        PatientMapper patientDto = createPatient();
        MCIResponse mciResponseForCreate = patientRepository.create(patientDto);
        assertEquals(201, mciResponseForCreate.getHttpStatus());
        String healthId = mciResponseForCreate.getId();
        patientDto.setHealthId(healthId);

        patientDto.setGender("F");
        MCIResponse mciResponseForUpdate = patientRepository.update(patientDto, patientDto.getHealthId());
        assertEquals(202, mciResponseForUpdate.getHttpStatus());
        PatientMapper savedPatient = patientRepository.findByHealthId(healthId);
        assertEquals("M", savedPatient.getGender());
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
        List<PatientMapper> patients;
        patients = patientRepository.findAllByLocation(location, null, 0, null).get();
        assertEquals(expectedRecordCount, patients.size());
    }

    private void generatePatientSet() throws Exception {
        String json = asString("jsons/patient/required_only_payload.json");
        PatientMapper patientMapper = new ObjectMapper().readValue(json, PatientMapper.class);
        createMultiplePatients(patientMapper, 10);
        patientMapper.setAddress(createAddress("10", "04", "09", "20", "01"));
        createMultiplePatients(patientMapper, 5);
    }

    private void createMultiplePatients(PatientMapper patientDto, int n) throws Exception {
        for (int x = 0; x < n; x++) {
            patientRepository.create(patientDto);
            patientDto.setHealthId(null);
        }
    }

    private void assertPatient(PatientMapper savedPatient, PatientMapper patientDto) {
        assertEquals(patientDto.getHealthId(), savedPatient.getHealthId());
        assertEquals(patientDto.getDateOfBirth(), savedPatient.getDateOfBirth());
        assertEquals(patientDto.getGender(), savedPatient.getGender());
        assertEquals(patientDto.getNationalId(), savedPatient.getNationalId());
        assertEquals(patientDto.getBirthRegistrationNumber(), savedPatient.getBirthRegistrationNumber());
        assertTrue(patientDto.getAddress().equals(savedPatient.getAddress()));
        assertEquals(patientDto.getPermanentAddress(), savedPatient.getPermanentAddress());
        assertEquals(patientDto.getUid(), savedPatient.getUid());
        assertEquals(patientDto.getGivenName(), savedPatient.getGivenName());
        assertEquals(patientDto.getSurName(), savedPatient.getSurName());
        assertEquals(patientDto.getOccupation(), savedPatient.getOccupation());
        assertEquals(patientDto.getEducationLevel(), savedPatient.getEducationLevel());
        assertNotNull(savedPatient.getUpdatedAt());
        assertNotNull(savedPatient.getCreatedAt());
    }

    private PatientMapper createPatient() {
        PatientMapper patientDto1 = new PatientMapper();
        patientDto1.setNationalId(nationalId);
        patientDto1.setBirthRegistrationNumber(birthRegistrationNumber);
        patientDto1.setUid(uid);
        patientDto1.setGivenName(givenName);
        patientDto1.setSurName(surname);
        patientDto1.setDateOfBirth("2014-12-01");
        patientDto1.setGender("M");
        patientDto1.setOccupation("salaried");
        patientDto1.setEducationLevel("BA");
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber(phoneNumber);
        patientDto1.setPhoneNumber(phone);

        Address address = createAddress(divisionId, districtId, upazilaId, "20", "01");
        patientDto1.setAddress(address);

        return patientDto1;
    }

    private Address createAddress(String division, String district, String upazilla, String cityCorp, String ward) {
        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId(division);
        address.setDistrictId(district);
        address.setUpazillaId(upazilla);
        address.setCityCorporationId(cityCorp);
        address.setWardId(ward);

        return address;
    }

    @After
    public void teardown() {
        cassandraOperations.execute("truncate " + CF_PATIENT);
        cassandraOperations.execute("truncate " + CF_NID_MAPPING);
        cassandraOperations.execute("truncate " + CF_BRN_MAPPING);
        cassandraOperations.execute("truncate " + CF_UID_MAPPING);
        cassandraOperations.execute("truncate " + CF_PHONE_NUMBER_MAPPING);
        cassandraOperations.execute("truncate " + CF_NAME_MAPPING);
        cassandraOperations.execute("truncate approval");
    }
}