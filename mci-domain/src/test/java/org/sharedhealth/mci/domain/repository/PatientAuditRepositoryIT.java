package org.sharedhealth.mci.domain.repository;


import junit.framework.Assert;
import org.junit.After;
import org.junit.Test;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.util.BaseRepositoryIT;
import org.sharedhealth.mci.domain.util.JsonMapper;
import org.sharedhealth.mci.domain.util.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;

public class PatientAuditRepositoryIT extends BaseRepositoryIT {


    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientAuditRepository auditRepository;

    @After
    public void tearDown() {
        TestUtil.truncateAllColumnFamilies(cassandraOps);
    }

    @Test
    public void shouldFindByHealthId() {
        String facilityId = "CHW";
        String providerId = "Dr. Monika";
        PatientData patientCreateData = buildPatient();
        String healthId = patientRepository.create(patientCreateData).getId();

        PatientData updateRequest = new PatientData();
        updateRequest.setGivenName("John");
        updateRequest.setRequester(facilityId, providerId);
        Requester requester = new Requester(facilityId, providerId);
        patientRepository.update(updateRequest, patientRepository.findByHealthId(healthId), requester);

        updateRequest = new PatientData();
        updateRequest.setEducationLevel("02");
        updateRequest.setRequester(facilityId, providerId);
        patientRepository.update(updateRequest, patientRepository.findByHealthId(healthId), requester);

        updateRequest = new PatientData();
        Address address = new Address("10", "20", "31");
        updateRequest.setPermanentAddress(address);
        updateRequest.setRequester(facilityId, providerId);
        patientRepository.update(updateRequest, patientRepository.findByHealthId(healthId), requester);

        List<PatientAuditLogData> logs = auditRepository.findByHealthId(healthId);
        assertNotNull(logs);
        assertEquals(4, logs.size());

        assertChangeSet(GIVEN_NAME, patientCreateData.getGivenName(), "John", logs.get(1).getChangeSet());
        assertChangeSet(EDU_LEVEL, patientCreateData.getOccupation(), "02", logs.get(2).getChangeSet());
        assertChangeSet(PERMANENT_ADDRESS, patientCreateData.getAddress(), address, logs.get(3).getChangeSet());
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
        return JsonMapper.writeValueAsString(requestedBy);
    }

    private void assertChangeSet(String fieldName, Object oldValue, Object newValue, Map<String, Map<String, Object>> changeSet) {
        assertNotNull(changeSet);
        assertEquals(1, changeSet.size());
        assertNotNull(changeSet.get(fieldName));
        Assert.assertEquals(JsonMapper.writeValueAsString(oldValue), JsonMapper.writeValueAsString(changeSet.get(fieldName).get
                (OLD_VALUE)));
        Assert.assertEquals(JsonMapper.writeValueAsString(newValue), JsonMapper.writeValueAsString(changeSet.get(fieldName).get
                (NEW_VALUE)));
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
}