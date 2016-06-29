package org.sharedhealth.mci.domain.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.util.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.CF_PATIENT_UPDATE_LOG;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.EVENT_TYPE_CREATED;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;
import static org.sharedhealth.mci.domain.util.JsonMapper.writeValueAsString;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

@RunWith(SpringJUnit4ClassRunner.class)
public class PatientFeedRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PatientFeedRepository feedRepository;

    private PatientData data;

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        data = createPatient();
    }

    private PatientData createPatient() {
        PatientData data = new PatientData();
        data.setHealthId(String.valueOf(new Date().getTime()));
        data.setNationalId("1234567890123");
        data.setBirthRegistrationNumber("12345678901234567");
        data.setUid("12345678901");
        data.setGivenName("Scott");
        data.setSurName("Tiger");
        data.setDateOfBirth(parseDate("2014-12-01"));
        data.setGender("M");
        data.setOccupation("03");
        data.setEducationLevel("BA");
        data.setHouseholdCode("12345");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("999900000");
        data.setPhoneNumber(phoneNumber);

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazilaId("09");
        address.setCityCorporationId("20");
        address.setUnionOrUrbanWardId("01");
        address.setCountryCode("050");
        data.setAddress(address);

        data.setRequester("Bahmni", "Dr. Monika");
        return data;
    }

    @Test
    public void shouldCreateUpdateLogsWhenPatientIsUpdated() {
        String healthId = patientRepository.create(data).getId();

        Date since = new Date();


        assertUpdateLogEntry(healthId, since, false);

        PatientData updateRequest = new PatientData();
        updateRequest.setHealthId(healthId);
        updateRequest.setGivenName("Harry");
        updateRequest.setAddress(new Address("99", "88", "77"));

        String facilityId = "Bahmni";
        String providerId = "Dr. Monika";
        Requester requester = new Requester(facilityId, providerId);
        updateRequest.setRequester(facilityId, providerId);
        patientRepository.update(updateRequest, patientRepository.findByHealthId(healthId), requester);

        assertUpdateLogEntry(healthId, since, true);
    }

    @Test
    public void shouldCreateCreateLogsWhenPatientIsCreated() {
        Date since = new Date();
        String healthId = patientRepository.create(data).getId();
        assertUpdateLogEntry(healthId, since, true);
    }

    @Test
    public void shouldFindUpdateLogsUsingUpdatedSinceAndLastMarker() {
        String healthId = patientRepository.create(data).getId();
        Date since = new Date();
        final int limit = 20;

        List<PatientUpdateLog> patientUpdateLogs = feedRepository.findPatientsUpdatedSince(since, limit, null);
        assertEquals(0, patientUpdateLogs.size());

        PatientData updateRequest = new PatientData();
        updateRequest.setHealthId(healthId);
        updateRequest.setGivenName("Update1");
        String facilityId = "Bahmni";
        String providerId = "Dr. Monika";
        Requester requester = new Requester(facilityId, providerId);
        updateRequest.setRequester(facilityId, providerId);
        patientRepository.update(updateRequest, patientRepository.findByHealthId(healthId), requester);

        patientUpdateLogs = feedRepository.findPatientsUpdatedSince(since, limit, null);
        assertEquals(1, patientUpdateLogs.size());
        assertTrue(healthId.equals(patientUpdateLogs.get(0).getHealthId()));

        final UUID marker = patientUpdateLogs.get(0).getEventId();

        updateRequest.setGivenName("Update2");
        patientRepository.update(updateRequest, patientRepository.findByHealthId(healthId), requester);

        updateRequest.setGivenName("Update3");
        patientRepository.update(updateRequest, patientRepository.findByHealthId(healthId), requester);

        patientUpdateLogs = feedRepository.findPatientsUpdatedSince(since, limit, null);
        assertEquals(3, patientUpdateLogs.size());

        patientUpdateLogs = feedRepository.findPatientsUpdatedSince(since, limit, marker);
        assertEquals(2, patientUpdateLogs.size());
    }

    @Test
    public void shouldFindUpdateLogForGivenEventId() throws Exception {
        UUID eventId = timeBased();
        String healthId = "h100";
        String changeSet = "{}";
        String requestedBy = "requestedBy";
        String approvedBy = "approvedBy";

        PatientUpdateLog patientUpdateLog = new PatientUpdateLog();
        patientUpdateLog.setEventId(eventId);
        patientUpdateLog.setHealthId(healthId);
        patientUpdateLog.setChangeSet(changeSet);
        patientUpdateLog.setRequestedBy(requestedBy);
        patientUpdateLog.setApprovedBy(approvedBy);
        patientUpdateLog.setEventType(EVENT_TYPE_CREATED);
        cassandraOps.execute(createInsertQuery(CF_PATIENT_UPDATE_LOG, patientUpdateLog, null, cassandraOps.getConverter()));

        PatientUpdateLog logByEventId = feedRepository.findPatientUpdateLogByEventId(eventId);

        assertNotNull(logByEventId);
        assertEquals(healthId, logByEventId.getHealthId());
        assertEquals(changeSet, logByEventId.getChangeSet());
        assertEquals(approvedBy, logByEventId.getApprovedBy());
    }

    private void assertUpdateLogEntry(String healthId, Date since, boolean shouldFind) {
        List<PatientUpdateLog> patientUpdateLogs = feedRepository.findPatientsUpdatedSince(since, 1, null);

        if (shouldFind) {
            assertEquals(healthId, patientUpdateLogs.get(0).getHealthId());
            assertEquals(buildRequestedBy(), patientUpdateLogs.get(0).getRequestedBy());
        } else {
            assertEquals(0, patientUpdateLogs.size());
        }
    }

    private String buildRequestedBy() {
        Map<String, Set<Requester>> requestedBy = new HashMap<>();
        Set<Requester> requester = new HashSet<>();
        requester.add(new Requester("Bahmni", "Dr. Monika"));
        requestedBy.put("ALL_FIELDS", requester);
        return writeValueAsString(requestedBy);
    }
}