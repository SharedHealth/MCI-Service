package org.sharedhealth.mci.web.handler;

import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.sharedhealth.mci.web.model.PendingApprovalRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
        patient.setHealthId(toBeApproved(map, HID, existingPatient.getHealthId(), patientToBeUpdated.getHealthId()));
        patient.setNationalId(toBeApproved(map, NID, existingPatient.getNationalId(), patientToBeUpdated.getNationalId()));
        patient.setNameBangla(toBeApproved(map, NAME_BANGLA, existingPatient.getNameBangla(), patientToBeUpdated.getNameBangla()));
        patient.setBirthRegistrationNumber(toBeApproved(map, BIN_BRN, existingPatient.getBirthRegistrationNumber(), patientToBeUpdated.getBirthRegistrationNumber()));
        patient.setGivenName(toBeApproved(map, GIVEN_NAME, existingPatient.getGivenName(), patientToBeUpdated.getGivenName()));
        patient.setSurName(toBeApproved(map, SUR_NAME, existingPatient.getSurName(), patientToBeUpdated.getSurName()));
        patient.setDateOfBirth(toBeApproved(map, DATE_OF_BIRTH, existingPatient.getDateOfBirth(), patientToBeUpdated.getDateOfBirth()));
        patient.setGender(toBeApproved(map, GENDER, existingPatient.getGender(), patientToBeUpdated.getGender()));
        patient.setOccupation(toBeApproved(map, OCCUPATION, existingPatient.getOccupation(), patientToBeUpdated.getOccupation()));
        patient.setEducationLevel(toBeApproved(map, EDU_LEVEL, existingPatient.getEducationLevel(), patientToBeUpdated.getEducationLevel()));
        //TODO : rewrite after relations bug is fixed.
        patient.setRelations(patientToBeUpdated.getRelations());
        patient.setUid(toBeApproved(map, UID, existingPatient.getUid(), patientToBeUpdated.getUid()));
        patient.setPlaceOfBirth(toBeApproved(map, PLACE_OF_BIRTH, existingPatient.getPlaceOfBirth(), patientToBeUpdated.getPlaceOfBirth()));
        patient.setReligion(toBeApproved(map, RELIGION, existingPatient.getReligion(), patientToBeUpdated.getReligion()));
        patient.setBloodGroup(toBeApproved(map, BLOOD_GROUP, existingPatient.getBloodGroup(), patientToBeUpdated.getBloodGroup()));
        patient.setNationality(toBeApproved(map, NATIONALITY, existingPatient.getNationality(), patientToBeUpdated.getNationality()));
        patient.setDisability(toBeApproved(map, DISABILITY, existingPatient.getDisability(), patientToBeUpdated.getDisability()));
        patient.setEthnicity(toBeApproved(map, ETHNICITY, existingPatient.getEthnicity(), patientToBeUpdated.getEthnicity()));
        patient.setPrimaryContact(toBeApproved(map, PRIMARY_CONTACT, existingPatient.getPrimaryContact(), patientToBeUpdated.getPrimaryContact()));
        patient.setMaritalStatus(toBeApproved(map, MARITAL_STATUS, existingPatient.getMaritalStatus(), patientToBeUpdated.getMaritalStatus()));
        patient.setFullName(toBeApproved(map, FULL_NAME, existingPatient.getFullName(), patientToBeUpdated.getFullName()));
        patient.setStatus(toBeApproved(map, PATIENT_STATUS, existingPatient.getStatus(), patientToBeUpdated.getStatus()));
        patient.setDateOfDeath(toBeApproved(map, DATE_OF_DEATH, existingPatient.getDateOfDeath(), patientToBeUpdated.getDateOfDeath()));
        patient.setCreatedAt(toBeApproved(map, CREATED, existingPatient.getCreatedAt(), patientToBeUpdated.getCreatedAt()));
        patient.setUpdatedAt(toBeApproved(map, MODIFIED, existingPatient.getUpdatedAt(), patientToBeUpdated.getUpdatedAt()));
        patient.setPhoneNumber(toBeapprovedPhoneNumber(map, PHONE_NUMBER, existingPatient.getPhoneNumber(), patientToBeUpdated.getPhoneNumber()));
        patient.setPrimaryContactNumber(toBeapprovedPhoneNumber(map, PRIMARY_CONTACT_NUMBER, existingPatient.getPrimaryContactNumber(), patientToBeUpdated.getPrimaryContactNumber()));
        if (patientToBeUpdated.getAddress() != null) {
            patient.setAddress(toBeApprovedAddress(map, PRESENT_ADDRESS, existingPatient.getAddress(), patientToBeUpdated.getAddress()));
        }
        if (patientToBeUpdated.getPermanentAddress() != null) {
            patient.setPermanentAddress(toBeApprovedAddress(map, PERMANENT_ADDRESS, existingPatient.getPermanentAddress(), patientToBeUpdated.getPermanentAddress()));
        }
        return map;
    }

    private PhoneNumber toBeapprovedPhoneNumber(HashMap<String, Object> map, String phoneNumber, PhoneNumber phoneNumberExisting, PhoneNumber phoneNumberUpdated) {
        String value = properties.getProperty(phoneNumber);
        if (value != null && phoneNumberUpdated != null) {
            if (value.equals(NON_UPDATEABLE)) {
                return phoneNumberExisting;
            } else if (value.equals(NEEDS_APPROVAL) && !phoneNumberExisting.equals(phoneNumberUpdated)) {
                map.put(phoneNumber, phoneNumberUpdated);
                return phoneNumberExisting;
            }
        }
        return phoneNumberUpdated;
    }

    private Address toBeApprovedAddress(Map<String, Object> map, String addressType, Address existingAddress, Address updatedAddress) {
        String value = properties.getProperty(addressType);
        if (value != null && updatedAddress != null) {
            if (value.equals(NON_UPDATEABLE)) {
                return existingAddress;
            } else if (value.equals(NEEDS_APPROVAL) && !existingAddress.equals(updatedAddress)) {
                map.put(addressType, updatedAddress);
                return existingAddress;
            }
        }
        return updatedAddress;
    }

    private String toBeApproved(Map<String, Object> map, String key, String oldValue, String newValue) {
        String property = properties.getProperty(key);
        if (property != null && newValue != null) {
            if (property.equals(NON_UPDATEABLE)) {
                return oldValue;
            } else if (property.equals(NEEDS_APPROVAL) && !newValue.equals(oldValue))
                map.put(key, newValue);
            return oldValue;
        }
        return newValue;
    }
}
