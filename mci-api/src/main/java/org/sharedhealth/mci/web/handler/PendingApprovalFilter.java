package org.sharedhealth.mci.web.handler;

import org.sharedhealth.mci.web.mapper.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.lang.String.valueOf;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

@Component
public class PendingApprovalFilter {

    private static final String NEEDS_APPROVAL = "NA";
    private static final String NON_UPDATEABLE = "NU";
    private static final String DUMMY_FACILITY = "10000059";

    private Properties properties;
    private PatientData newPatient;

    public PendingApprovalFilter() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("approvalFeilds.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            this.properties = properties;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read approval property file.", e);
        }
    }

    public PatientData filter(PatientData existingPatient, PatientData updateRequest) {
        this.newPatient = new PatientData();
        newPatient.setPendingApprovals(existingPatient.getPendingApprovals());

        newPatient.setHealthId(processString(HID, existingPatient.getHealthId(), updateRequest.getHealthId()));
        newPatient.setNationalId(processString(NID, existingPatient.getNationalId(), updateRequest.getNationalId()));
        newPatient.setNameBangla(processString(NAME_BANGLA, existingPatient.getNameBangla(), updateRequest.getNameBangla()));
        newPatient.setBirthRegistrationNumber(processString(BIN_BRN, existingPatient.getBirthRegistrationNumber(), updateRequest.getBirthRegistrationNumber()));
        newPatient.setGivenName(processString(GIVEN_NAME, existingPatient.getGivenName(), updateRequest.getGivenName()));
        newPatient.setSurName(processString(SUR_NAME, existingPatient.getSurName(), updateRequest.getSurName()));
        newPatient.setDateOfBirth(processString(DATE_OF_BIRTH, existingPatient.getDateOfBirth(), updateRequest.getDateOfBirth()));
        newPatient.setGender(processString(GENDER, existingPatient.getGender(), updateRequest.getGender()));
        newPatient.setOccupation(processString(OCCUPATION, existingPatient.getOccupation(), updateRequest.getOccupation()));
        newPatient.setEducationLevel(processString(EDU_LEVEL, existingPatient.getEducationLevel(), updateRequest.getEducationLevel()));
        newPatient.setRelations(updateRequest.getRelations()); //TODO : rewrite after relations bug is fixed.
        newPatient.setUid(processString(UID, existingPatient.getUid(), updateRequest.getUid()));
        newPatient.setPlaceOfBirth(processString(PLACE_OF_BIRTH, existingPatient.getPlaceOfBirth(), updateRequest.getPlaceOfBirth()));
        newPatient.setReligion(processString(RELIGION, existingPatient.getReligion(), updateRequest.getReligion()));
        newPatient.setBloodGroup(processString(BLOOD_GROUP, existingPatient.getBloodGroup(), updateRequest.getBloodGroup()));
        newPatient.setNationality(processString(NATIONALITY, existingPatient.getNationality(), updateRequest.getNationality()));
        newPatient.setDisability(processString(DISABILITY, existingPatient.getDisability(), updateRequest.getDisability()));
        newPatient.setEthnicity(processString(ETHNICITY, existingPatient.getEthnicity(), updateRequest.getEthnicity()));
        newPatient.setPrimaryContact(processString(PRIMARY_CONTACT, existingPatient.getPrimaryContact(), updateRequest.getPrimaryContact()));
        newPatient.setMaritalStatus(processString(MARITAL_STATUS, existingPatient.getMaritalStatus(), updateRequest.getMaritalStatus()));
        newPatient.setFullName(processString(FULL_NAME, existingPatient.getFullName(), updateRequest.getFullName()));
        newPatient.setStatus(processString(PATIENT_STATUS, existingPatient.getStatus(), updateRequest.getStatus()));
        newPatient.setDateOfDeath(processString(DATE_OF_DEATH, existingPatient.getDateOfDeath(), updateRequest.getDateOfDeath()));
        newPatient.setConfidential(processString(CONFIDENTIAL, existingPatient.getConfidential(), updateRequest.getConfidential()));
        newPatient.setCreatedAt(processUuid(CREATED, existingPatient.getCreatedAt(), updateRequest.getCreatedAt()));
        newPatient.setUpdatedAt(processUuid(MODIFIED, existingPatient.getUpdatedAt(), updateRequest.getUpdatedAt()));
        newPatient.setPhoneNumber(processPhoneNumber(PHONE_NUMBER, existingPatient.getPhoneNumber(), updateRequest.getPhoneNumber()));
        newPatient.setPrimaryContactNumber(processPhoneNumber(PRIMARY_CONTACT_NUMBER, existingPatient.getPrimaryContactNumber(), updateRequest.getPrimaryContactNumber()));
        newPatient.setAddress(processAddress(PRESENT_ADDRESS, existingPatient.getAddress(), updateRequest.getAddress()));
        newPatient.setPermanentAddress(processAddress(PERMANENT_ADDRESS, existingPatient.getPermanentAddress(), updateRequest.getPermanentAddress()));

        return newPatient;
    }

    private PhoneNumber processPhoneNumber(String key, PhoneNumber oldValue, PhoneNumber newValue) {
        Object phoneNumber = process(key, oldValue, newValue);
        return phoneNumber == null ? null : (PhoneNumber) phoneNumber;
    }

    private Address processAddress(String key, Address oldValue, Address newValue) {
        Object address = process(key, oldValue, newValue);
        return address == null ? null : (Address) address;
    }

    private UUID processUuid(String key, UUID oldValue, UUID newValue) {
        Object value = process(key, oldValue, newValue);
        return value == null ? null : (UUID) value;
    }

    private String processString(String key, String oldValue, String newValue) {
        Object value = process(key, oldValue, newValue);
        return value == null ? null : valueOf(value);
    }

    private Object process(String key, Object oldValue, Object newValue) {
        if (newValue == null) {
            return oldValue;
        }
        String property = properties.getProperty(key);
        if (property != null) {
            if (property.equals(NON_UPDATEABLE)) {
                return oldValue;
            }
            if (property.equals(NEEDS_APPROVAL) && !newValue.equals(oldValue)) {
                newPatient.addPendingApproval(buildPendingApproval(key, newValue));
                return oldValue;
            }
        }
        return newValue;
    }

    private PendingApproval buildPendingApproval(String key, Object newValue) {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(key);

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
        fieldDetails.setValue(newValue);
        fieldDetails.setFacilityId(DUMMY_FACILITY);
        UUID uuid = timeBased();
        fieldDetails.setCreatedAt(unixTimestamp(uuid));
        fieldDetailsMap.put(uuid, fieldDetails);
        pendingApproval.setFieldDetails(fieldDetailsMap);

        return pendingApproval;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
