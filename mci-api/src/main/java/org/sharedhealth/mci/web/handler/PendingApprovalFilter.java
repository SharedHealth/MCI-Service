package org.sharedhealth.mci.web.handler;

import org.sharedhealth.mci.web.exception.NonUpdatableFieldUpdateException;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.service.ApprovalFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.TreeMap;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.sharedhealth.mci.web.utils.PatientDataConstants.COUNTRY_CODE_BANGLADESH;

@Component
public class PendingApprovalFilter {

    private static final String NEEDS_APPROVAL = "NA";
    private static final String NON_UPDATABLE = "NU";

    private PatientData newPatient;
    private Requester requestedBy;
    private ApprovalFieldService properties;

    @Autowired
    public PendingApprovalFilter(ApprovalFieldService approvalFieldService) {
        this.properties = approvalFieldService;
    }

    public PatientData filter(PatientData existingPatient, PatientData updateRequest) {
        this.newPatient = new PatientData();
        this.requestedBy = updateRequest.getRequester();
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
        newPatient.setConfidential(processString(CONFIDENTIAL, existingPatient.getConfidential(), updateRequest.getConfidential()));
        newPatient.setCreatedAt(processUuid(CREATED, existingPatient.getCreatedAt(), updateRequest.getCreatedAt()));
        newPatient.setUpdatedAt(processUuid(MODIFIED, existingPatient.getUpdatedAt(), updateRequest.getUpdatedAt()));
        newPatient.setPhoneNumber(processPhoneNumber(PHONE_NUMBER, existingPatient.getPhoneNumber(), updateRequest.getPhoneNumber()));
        newPatient.setPatientStatus(processPatientStatus(STATUS, existingPatient.getPatientStatus(), updateRequest.getPatientStatus()));
        newPatient.setPrimaryContactNumber(processPhoneNumber(PRIMARY_CONTACT_NUMBER, existingPatient.getPrimaryContactNumber(), updateRequest.getPrimaryContactNumber()));
        newPatient.setAddress(processAddress(PRESENT_ADDRESS, existingPatient.getAddress(), updateRequest.getAddress()));
        newPatient.setPermanentAddress(processAddress(PERMANENT_ADDRESS, existingPatient.getPermanentAddress(), updateRequest.getPermanentAddress()));
        newPatient.setHouseholdCode(processString(HOUSEHOLD_CODE, existingPatient.getHouseholdCode(), updateRequest.getHouseholdCode()));

        return newPatient;
    }

    private PhoneNumber processPhoneNumber(String key, PhoneNumber oldValue, PhoneNumber newValue) {
        Object phoneNumber = process(key, oldValue, newValue);
        return phoneNumber == null ? null : (PhoneNumber) phoneNumber;
    }

    private Address processAddress(String key, Address oldValue, Address newValue) {

        if (newValue != null && !newValue.isEmpty()) {
            newValue.setCountryCode(COUNTRY_CODE_BANGLADESH);
        }

        Object address = process(key, oldValue, newValue);
        return address == null ? null : (Address) address;
    }

    private PatientStatus processPatientStatus(String key, PatientStatus oldValue, PatientStatus newValue) {
        Object patientStatus = process(key, oldValue, newValue);
        return patientStatus == null ? null : (PatientStatus) patientStatus;
    }

    private UUID processUuid(String key, UUID oldValue, UUID newValue) {
        Object value = process(key, oldValue, newValue);
        return value == null ? null : (UUID) value;
    }

    private String processString(String key, String oldValue, String newValue) {
        if ("".equals(trim(newValue))) {
            oldValue = defaultString(oldValue);
            newValue = defaultString(newValue);
        }
        Object value = process(key, oldValue, newValue);
        return value == null ? null : valueOf(value);
    }

    private Object process(String key, Object oldValue, Object newValue) {
        if (newValue == null) {
            return oldValue;
        }
        String property = properties.getProperty(key);
        if (property != null) {
            if (property.equals(NON_UPDATABLE)) {
                if (!newValue.equals(oldValue)) {
                    throw new NonUpdatableFieldUpdateException("Cannot update non-updatable field: " + key);
                }
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
        fieldDetails.setRequestedBy(this.requestedBy);
        UUID uuid = timeBased();
        fieldDetails.setCreatedAt(unixTimestamp(uuid));
        fieldDetailsMap.put(uuid, fieldDetails);
        pendingApproval.setFieldDetails(fieldDetailsMap);

        return pendingApproval;
    }
}
