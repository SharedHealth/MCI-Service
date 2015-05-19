package org.sharedhealth.mci.web.infrastructure.persistence;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.model.PatientAuditLog;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.sharedhealth.mci.web.service.PatientAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.setupApprovalsConfig;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.truncateAllColumnFamilies;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientAuditRepositoryIT {


    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cassandraOps;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientAuditService auditService;

    @Autowired
    private PatientAuditRepository auditRepository;

    @Autowired
    private PatientFeedRepository feedRepository;

    @Before
    public void setUp() throws Exception {
        setupApprovalsConfig(cassandraOps);
    }

    @Test
    public void shouldFindByHealthId() {
        String facility = "CHW";
        String provider = "Dr. Monika";
        PatientData patientCreateData = buildPatient();
        String healthId = patientRepository.create(patientCreateData).getId();

        PatientData updateRequest = new PatientData();
        updateRequest.setGivenName("John");
        updateRequest.setRequester(facility, provider);
        patientRepository.update(updateRequest, healthId);

        updateRequest = new PatientData();
        updateRequest.setEducationLevel("02");
        updateRequest.setRequester(facility, provider);
        patientRepository.update(updateRequest, healthId);

        updateRequest = new PatientData();
        Address address = new Address("10", "20", "31");
        updateRequest.setPermanentAddress(address);
        updateRequest.setRequester(facility, provider);
        patientRepository.update(updateRequest, healthId);

        auditService.sync();

        List<PatientAuditLogData> logs = auditRepository.findByHealthId(healthId);
        assertNotNull(logs);
        assertEquals(3, logs.size());

        assertChangeSet(GIVEN_NAME, patientCreateData.getGivenName(), "John", logs.get(0).getChangeSet());
        assertChangeSet(EDU_LEVEL, patientCreateData.getOccupation(), "02", logs.get(1).getChangeSet());
        assertChangeSet(PERMANENT_ADDRESS, patientCreateData.getAddress(), address, logs.get(2).getChangeSet());
    }

    private void assertChangeSet(String fieldName, Object oldValue, Object newValue, Map<String, Map<String, Object>> changeSet) {
        assertNotNull(changeSet);
        assertEquals(1, changeSet.size());
        assertNotNull(changeSet.get(fieldName));
        assertEquals(writeValueAsString(oldValue), writeValueAsString(changeSet.get(fieldName).get(OLD_VALUE)));
        assertEquals(writeValueAsString(newValue), writeValueAsString(changeSet.get(fieldName).get(NEW_VALUE)));
    }

    private PatientData buildPatient() {
        PatientData patient = new PatientData();
        patient.setNationalId("1234567890123");
        patient.setBirthRegistrationNumber("12345678901234567");
        patient.setUid("12345678901");
        patient.setGivenName("Happy");
        patient.setSurName("Rotter");
        patient.setDateOfBirth(toIsoFormat("2014-12-01"));
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

    @Test
    public void shouldFindLatestMarker() {
        PatientData patientCreateData = buildPatient();
        String healthId = patientRepository.create(patientCreateData).getId();

        PatientData updateRequest = new PatientData();
        updateRequest.setGivenName("John");
        updateRequest.setSurName("Doe");
        updateRequest.setRequester("Bahmni", "Dr. Monika");
        patientRepository.update(updateRequest, healthId);

        auditService.sync();

        List<PatientUpdateLog> feeds = feedRepository.findPatientsUpdatedSince(null, 10);
        assertNotNull(feeds);
        assertEquals(2, feeds.size());

        UUID marker = auditRepository.findLatestMarker();
        assertNotNull(marker);
        assertEquals(feeds.get(1).getEventId(), marker);
    }

    @Test
    public void shouldUpdateAuditLogsIfPrimaryKeyExists() {
        PatientAuditLog log1 = new PatientAuditLog();
        log1.setHealthId("h100");
        log1.setEventId(timeBased());
        log1.setRequestedBy(buildRequestedBy());
        auditRepository.saveOrUpdate(asList(log1));

        List<PatientAuditLogData> logs = auditRepository.findByHealthId(log1.getHealthId());
        assertNotNull(logs);
        assertEquals(1, logs.size());

        PatientAuditLog log2 = new PatientAuditLog();
        log2.setHealthId(log1.getHealthId());
        log2.setEventId(log1.getEventId());
        log2.setRequestedBy(buildRequestedBy());
        auditRepository.saveOrUpdate(asList(log2));

        logs = auditRepository.findByHealthId(log1.getHealthId());
        assertNotNull(logs);
        assertEquals(1, logs.size());
    }

    private String buildRequestedBy() {
        Map<String, Set<Requester>> requestedBy = new HashMap<>();
        Set<Requester> requester = new HashSet<>();
        requester.add(new Requester("CHW"));
        requestedBy.put("ALL_FIELDS", requester);
        return writeValueAsString(requestedBy);
    }

    @After
    public void tearDown() {
        truncateAllColumnFamilies(cassandraOps);
    }
}