package org.sharedhealth.mci.web.handler;

import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.sharedhealth.mci.web.model.PendingApprovalRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.valueOf;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PatientFilter {

    private static final String NEEDS_APPROVAL = "NA";
    private static final String NON_UPDATEABLE = "NU";
    private Properties properties;
    private PatientData existingPatient;
    private PatientData patientToBeUpdated;
    private PatientData patient;

    public PatientFilter(Properties properties, PatientData existingPatient, PatientData patientToBeUpdated, PatientData patient) {
        this.properties = properties;
        this.existingPatient = existingPatient;
        this.patientToBeUpdated = patientToBeUpdated;
        this.patient = patient;
    }

    public PendingApprovalRequest filter() {
        Map<String, Object> filteredFeildMap = filterFeilds(patient);
        if (filteredFeildMap.isEmpty()) {
            return null;
        }
        PendingApprovalRequest pendingApprovalRequest = new PendingApprovalRequest();
        pendingApprovalRequest.setFacilityId("10000059");
        pendingApprovalRequest.setFields(filteredFeildMap);
        return pendingApprovalRequest;
    }

    private Map<String, Object> filterFeilds(PatientData patient) {
        HashMap<String, Object> map = new HashMap<>();
        patient.setHealthId(processString(map, HID, existingPatient.getHealthId(), patientToBeUpdated.getHealthId()));
        patient.setNationalId(processString(map, NID, existingPatient.getNationalId(), patientToBeUpdated.getNationalId()));
        patient.setNameBangla(processString(map, NAME_BANGLA, existingPatient.getNameBangla(), patientToBeUpdated.getNameBangla()));
        patient.setBirthRegistrationNumber(processString(map, BIN_BRN, existingPatient.getBirthRegistrationNumber(), patientToBeUpdated.getBirthRegistrationNumber()));
        patient.setGivenName(processString(map, GIVEN_NAME, existingPatient.getGivenName(), patientToBeUpdated.getGivenName()));
        patient.setSurName(processString(map, SUR_NAME, existingPatient.getSurName(), patientToBeUpdated.getSurName()));
        patient.setDateOfBirth(processString(map, DATE_OF_BIRTH, existingPatient.getDateOfBirth(), patientToBeUpdated.getDateOfBirth()));
        patient.setGender(processString(map, GENDER, existingPatient.getGender(), patientToBeUpdated.getGender()));
        patient.setOccupation(processString(map, OCCUPATION, existingPatient.getOccupation(), patientToBeUpdated.getOccupation()));
        patient.setEducationLevel(processString(map, EDU_LEVEL, existingPatient.getEducationLevel(), patientToBeUpdated.getEducationLevel()));
        //TODO : rewrite after relations bug is fixed.
        patient.setRelations(patientToBeUpdated.getRelations());
        patient.setUid(processString(map, UID, existingPatient.getUid(), patientToBeUpdated.getUid()));
        patient.setPlaceOfBirth(processString(map, PLACE_OF_BIRTH, existingPatient.getPlaceOfBirth(), patientToBeUpdated.getPlaceOfBirth()));
        patient.setReligion(processString(map, RELIGION, existingPatient.getReligion(), patientToBeUpdated.getReligion()));
        patient.setBloodGroup(processString(map, BLOOD_GROUP, existingPatient.getBloodGroup(), patientToBeUpdated.getBloodGroup()));
        patient.setNationality(processString(map, NATIONALITY, existingPatient.getNationality(), patientToBeUpdated.getNationality()));
        patient.setDisability(processString(map, DISABILITY, existingPatient.getDisability(), patientToBeUpdated.getDisability()));
        patient.setEthnicity(processString(map, ETHNICITY, existingPatient.getEthnicity(), patientToBeUpdated.getEthnicity()));
        patient.setPrimaryContact(processString(map, PRIMARY_CONTACT, existingPatient.getPrimaryContact(), patientToBeUpdated.getPrimaryContact()));
        patient.setMaritalStatus(processString(map, MARITAL_STATUS, existingPatient.getMaritalStatus(), patientToBeUpdated.getMaritalStatus()));
        patient.setFullName(processString(map, FULL_NAME, existingPatient.getFullName(), patientToBeUpdated.getFullName()));
        patient.setStatus(processString(map, PATIENT_STATUS, existingPatient.getStatus(), patientToBeUpdated.getStatus()));
        patient.setDateOfDeath(processString(map, DATE_OF_DEATH, existingPatient.getDateOfDeath(), patientToBeUpdated.getDateOfDeath()));
        patient.setCreatedAt(processString(map, CREATED, existingPatient.getCreatedAt(), patientToBeUpdated.getCreatedAt()));
        patient.setUpdatedAt(processString(map, MODIFIED, existingPatient.getUpdatedAt(), patientToBeUpdated.getUpdatedAt()));
        patient.setPhoneNumber(processPhoneNumber(map, PHONE_NUMBER, existingPatient.getPhoneNumber(), patientToBeUpdated.getPhoneNumber()));
        patient.setPrimaryContactNumber(processPhoneNumber(map, PRIMARY_CONTACT_NUMBER, existingPatient.getPrimaryContactNumber(), patientToBeUpdated.getPrimaryContactNumber()));
        if (patientToBeUpdated.getAddress() != null) {
            patient.setAddress(processAddress(map, PRESENT_ADDRESS, existingPatient.getAddress(), patientToBeUpdated.getAddress()));
        }
        if (patientToBeUpdated.getPermanentAddress() != null) {
            patient.setPermanentAddress(processAddress(map, PERMANENT_ADDRESS, existingPatient.getPermanentAddress(), patientToBeUpdated.getPermanentAddress()));
        }
        return map;
    }

    private PhoneNumber processPhoneNumber(HashMap<String, Object> map, String key, PhoneNumber oldValue, PhoneNumber newValue) {
        Object phoneNumber = process(map, key, oldValue, newValue);
        return phoneNumber == null ? null : (PhoneNumber) phoneNumber;
    }

    private Address processAddress(Map<String, Object> map, String key, Address oldValue, Address newValue) {
        Object address = process(map, key, oldValue, newValue);
        return address == null ? null : (Address) address;
    }

    private String processString(Map<String, Object> map, String key, String oldValue, String newValue) {
        Object value = process(map, key, oldValue, newValue);
        return value == null ? null : valueOf(value);
    }

    private Object process(Map<String, Object> map, String key, Object oldValue, Object newValue) {
        String property = properties.getProperty(key);
        if (property != null && newValue != null) {
            if (property.equals(NON_UPDATEABLE)) {
                return oldValue;
            }
            if (property.equals(NEEDS_APPROVAL) && !newValue.equals(oldValue)) {
                map.put(key, newValue);
                return oldValue;
            }
        }
        return newValue;
    }
}
