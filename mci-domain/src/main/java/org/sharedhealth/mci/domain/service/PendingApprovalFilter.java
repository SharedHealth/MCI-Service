package org.sharedhealth.mci.domain.service;

import org.sharedhealth.mci.domain.exception.NonUpdatableFieldUpdateException;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;
import static org.sharedhealth.mci.domain.constant.MCIConstants.COUNTRY_CODE_BANGLADESH;

@Component
public class PendingApprovalFilter {

    private static final String NEEDS_APPROVAL = "NA";
    private static final String NON_UPDATABLE = "NU";

    private ApprovalFieldService properties;

    @Autowired
    public PendingApprovalFilter(ApprovalFieldService approvalFieldService) {
        this.properties = approvalFieldService;
    }

    public PatientData filter(PatientData existingPatient, PatientData updateRequest) {
        PatientData newPatient = new PatientData();
        Requester requestedBy = updateRequest.getRequester();
        newPatient.setPendingApprovals(existingPatient.getPendingApprovals());

        newPatient.setHealthId(processString(HID, existingPatient.getHealthId(), updateRequest.getHealthId(), requestedBy, newPatient));
        newPatient.setNationalId(processString(NID, existingPatient.getNationalId(), updateRequest.getNationalId(), requestedBy,
                newPatient));
        newPatient.setNameBangla(processString(NAME_BANGLA, existingPatient.getNameBangla(), updateRequest.getNameBangla(), requestedBy,
                newPatient));
        newPatient.setBirthRegistrationNumber(processString(BIN_BRN, existingPatient.getBirthRegistrationNumber(), updateRequest
                .getBirthRegistrationNumber(), requestedBy, newPatient));
        newPatient.setGivenName(processString(GIVEN_NAME, existingPatient.getGivenName(), updateRequest.getGivenName(), requestedBy,
                newPatient));
        newPatient.setSurName(processString(SUR_NAME, existingPatient.getSurName(), updateRequest.getSurName(), requestedBy, newPatient));
        newPatient.setDateOfBirth(processDate(DATE_OF_BIRTH, existingPatient.getDateOfBirth(), updateRequest.getDateOfBirth(),
                requestedBy, newPatient));
        newPatient.setDobType(processString(DOB_TYPE, existingPatient.getDobType(), updateRequest.getDobType(), requestedBy, newPatient));
        newPatient.setGender(processString(GENDER, existingPatient.getGender(), updateRequest.getGender(), requestedBy, newPatient));
        newPatient.setOccupation(processString(OCCUPATION, existingPatient.getOccupation(), updateRequest.getOccupation(), requestedBy,
                newPatient));
        newPatient.setEducationLevel(processString(EDU_LEVEL, existingPatient.getEducationLevel(), updateRequest.getEducationLevel(),
                requestedBy, newPatient));
        newPatient.setUid(processString(UID, existingPatient.getUid(), updateRequest.getUid(), requestedBy, newPatient));
        newPatient.setPlaceOfBirth(processString(PLACE_OF_BIRTH, existingPatient.getPlaceOfBirth(), updateRequest.getPlaceOfBirth(),
                requestedBy, newPatient));
        newPatient.setReligion(processString(RELIGION, existingPatient.getReligion(), updateRequest.getReligion(), requestedBy,
                newPatient));
        newPatient.setBloodGroup(processString(BLOOD_GROUP, existingPatient.getBloodGroup(), updateRequest.getBloodGroup(), requestedBy,
                newPatient));
        newPatient.setNationality(processString(NATIONALITY, existingPatient.getNationality(), updateRequest.getNationality(),
                requestedBy, newPatient));
        newPatient.setDisability(processString(DISABILITY, existingPatient.getDisability(), updateRequest.getDisability(), requestedBy,
                newPatient));
        newPatient.setEthnicity(processString(ETHNICITY, existingPatient.getEthnicity(), updateRequest.getEthnicity(), requestedBy,
                newPatient));
        newPatient.setPrimaryContact(processString(PRIMARY_CONTACT, existingPatient.getPrimaryContact(), updateRequest.getPrimaryContact
                (), requestedBy, newPatient));
        newPatient.setMaritalStatus(processString(MARITAL_STATUS, existingPatient.getMaritalStatus(), updateRequest.getMaritalStatus(),
                requestedBy, newPatient));
        newPatient.setConfidential(processString(CONFIDENTIAL, existingPatient.getConfidential(), updateRequest.getConfidential(),
                requestedBy, newPatient));
        newPatient.setCreatedAt(processUuid(CREATED, existingPatient.getCreatedAt(), updateRequest.getCreatedAt(), requestedBy,
                newPatient));
        newPatient.setUpdatedAt(processUuid(MODIFIED, existingPatient.getUpdatedAt(), updateRequest.getUpdatedAt(), requestedBy,
                newPatient));
        newPatient.setPhoneNumber(processPhoneNumber(PHONE_NUMBER, existingPatient.getPhoneNumber(), updateRequest.getPhoneNumber(),
                requestedBy, newPatient));
        newPatient.setRelations(processRelations(RELATIONS, existingPatient.getRelations(), updateRequest.getRelations(), requestedBy,
                newPatient));
        newPatient.setPatientStatus(processPatientStatus(STATUS, existingPatient.getPatientStatus(), updateRequest.getPatientStatus(),
                requestedBy, newPatient));
        newPatient.setPrimaryContactNumber(processPhoneNumber(PRIMARY_CONTACT_NUMBER, existingPatient.getPrimaryContactNumber(),
                updateRequest.getPrimaryContactNumber(), requestedBy, newPatient));
        newPatient.setAddress(processAddress(PRESENT_ADDRESS, existingPatient.getAddress(), updateRequest.getAddress(), requestedBy,
                newPatient));
        newPatient.setPermanentAddress(processPermanentAddress(PERMANENT_ADDRESS, existingPatient.getPermanentAddress(), updateRequest
                .getPermanentAddress(), requestedBy, newPatient));
        newPatient.setHouseholdCode(processString(HOUSEHOLD_CODE, existingPatient.getHouseholdCode(), updateRequest.getHouseholdCode(),
                requestedBy, newPatient));
        newPatient.setActive((Boolean) process(ACTIVE, existingPatient.isActive(), updateRequest.isActive(), requestedBy, newPatient));
        newPatient.setMergedWith(processString(MERGED_WITH, existingPatient.getMergedWith(), updateRequest.getMergedWith(), requestedBy,
                newPatient));

        return newPatient;
    }

    private Date processDate(String key, Date oldDate, Date newDate, Requester requestedBy, PatientData newPatient) {
        Object date = process(key, oldDate, newDate, requestedBy, newPatient);
        return date == null ? null : (Date) date;
    }

    private List<Relation> processRelations(String key, List<Relation> oldRelations, List<Relation> newRelations, Requester requester,
                                            PatientData newPatient) {

        Object relations = process(key, oldRelations, newRelations, requester, newPatient);
        return relations == null ? null : (List<Relation>) relations;
    }

    private PhoneNumber processPhoneNumber(String key, PhoneNumber oldValue, PhoneNumber newValue, Requester requester, PatientData
            newPatient) {
        Object phoneNumber = process(key, oldValue, newValue, requester, newPatient);
        return phoneNumber == null ? null : (PhoneNumber) phoneNumber;
    }

    private Address processAddress(String key, Address oldValue, Address newValue, Requester requester, PatientData newPatient) {

        if (newValue != null && !newValue.isEmpty()) {
            newValue.setCountryCode(COUNTRY_CODE_BANGLADESH);
        }

        Object address = process(key, oldValue, newValue, requester, newPatient);
        return address == null ? null : (Address) address;
    }

    private Address processPermanentAddress(String key, Address oldValue, Address newValue, Requester requester, PatientData newPatient) {

        Object address = process(key, oldValue, newValue, requester, newPatient);
        return address == null ? null : (Address) address;
    }

    private PatientStatus processPatientStatus(String key, PatientStatus oldValue, PatientStatus newValue, Requester requester,
                                               PatientData newPatient) {
        Object patientStatus = process(key, oldValue, newValue, requester, newPatient);
        return patientStatus == null ? null : (PatientStatus) patientStatus;
    }

    private UUID processUuid(String key, UUID oldValue, UUID newValue, Requester requester, PatientData newPatient) {
        Object value = process(key, oldValue, newValue, requester, newPatient);
        return value == null ? null : (UUID) value;
    }

    private String processString(String key, String oldValue, String newValue, Requester requester, PatientData newPatient) {
        if ("".equals(trim(newValue))) {
            oldValue = defaultString(oldValue);
            newValue = defaultString(newValue);
        }
        Object value = process(key, oldValue, newValue, requester, newPatient);
        return value == null ? null : valueOf(value);
    }

    private Object process(String key, Object oldValue, Object newValue, Requester requester, PatientData newPatient) {
        if (newValue == null) {
            return oldValue;
        }
        String property = properties.getProperty(key);
        if (property != null) {
            if (isNonUpdateable(key, oldValue, newValue, property)) return oldValue;

            if (isUpdatedByAdmin(requester)) return newValue;

            if (needsApproval(oldValue, newValue, property)) {
                newPatient.addPendingApproval(buildPendingApproval(key, newValue, requester));
                return oldValue;
            }
        }
        return newValue;
    }

    private boolean needsApproval(Object oldValue, Object newValue, String property) {
        if (property.equals(NEEDS_APPROVAL) && !isEqual(oldValue, newValue)) {
            return true;
        }
        return false;
    }

    private boolean isEqual(Object oldValue, Object newValue) {
        if (oldValue instanceof Date) {
            return DateUtil.isEqualTo((Date) oldValue, (Date) newValue);
        }
        return newValue.equals(oldValue);
    }

    private boolean isUpdatedByAdmin(Requester requester) {
        if (requester != null && requester.getAdmin() != null) {
            return true;
        }
        return false;
    }

    private boolean isNonUpdateable(String key, Object oldValue, Object newValue, String property) {
        if (property.equals(NON_UPDATABLE)) {
            if (!isEqual(oldValue, newValue)) {
                throw new NonUpdatableFieldUpdateException("Cannot update non-updatable field: " + key);
            }
            return true;
        }
        return false;
    }

    private PendingApproval buildPendingApproval(String key, Object newValue, Requester requester) {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(key);

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
        fieldDetails.setValue(newValue);
        fieldDetails.setRequestedBy(requester);
        UUID uuid = timeBased();
        fieldDetails.setCreatedAt(unixTimestamp(uuid));
        fieldDetailsMap.put(uuid, fieldDetails);
        pendingApproval.setFieldDetails(fieldDetailsMap);

        return pendingApproval;
    }
}