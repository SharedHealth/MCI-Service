package org.sharedhealth.mci.web.handler;

import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.sharedhealth.mci.web.model.PendingApproval;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.sharedhealth.mci.web.utils.PatientFieldProperties.*;

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

    public PendingApproval filter() {
        Map<String, String> filteredFeildMap = filterFeilds(patient);
        if (filteredFeildMap.isEmpty()) {
            return null;
        }
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setFacilityId("10000059");
        pendingApproval.setFields(filteredFeildMap);
        return pendingApproval;
    }

    private Map<String, String> filterFeilds(PatientData patient) {
        HashMap<String, String> map = new HashMap<>();
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
        patient.setIsAlive(toBeApproved(map, IS_ALIVE, existingPatient.getIsAlive(), patientToBeUpdated.getIsAlive()));
        patient.setCreatedAt(toBeApproved(map, CREATED, existingPatient.getCreatedAt(), patientToBeUpdated.getCreatedAt()));
        patient.setUpdatedAt(toBeApproved(map, MODIFIED, existingPatient.getUpdatedAt(), patientToBeUpdated.getUpdatedAt()));
        patient.setPhoneNumber(toBeapprovedPhoneNumber(map, PHONE_NUMBER, existingPatient.getPhoneNumber(), patientToBeUpdated.getPhoneNumber()));
        if (patientToBeUpdated.getAddress() != null) {
            patient.setAddress(toBeApprovedAddress(map, PRESENT_ADDRESS, existingPatient.getAddress(), patientToBeUpdated.getAddress()));
        }
        if (patientToBeUpdated.getPermanentAddress() != null) {
            patient.setPermanentAddress(toBeApprovedAddress(map, PERMANENT_ADDRESS, existingPatient.getPermanentAddress(), patientToBeUpdated.getPermanentAddress()));
        }
        return map;
    }

    private PhoneNumber toBeapprovedPhoneNumber(HashMap<String, String> map, String phoneNumber, PhoneNumber phoneNumberExisting, PhoneNumber phoneNumberUpdated) {
        String value = properties.getProperty(phoneNumber);
        if (value != null && phoneNumberUpdated != null) {
            if (value.equals(NON_UPDATEABLE)) {
                return phoneNumberExisting;
            } else if (value.equals(NEEDS_APPROVAL) && !phoneNumberExisting.equals(phoneNumberUpdated)) {
                map.put(phoneNumber, phoneNumberUpdated.toString());
                return phoneNumberExisting;
            }
        }
        return phoneNumberUpdated;
    }

    private Address toBeApprovedAddress(Map<String, String> map, String addressType, Address existingAddress, Address updatedAddress) {
        String value = properties.getProperty(addressType);
        if (value != null && updatedAddress != null) {
            if (value.equals(NON_UPDATEABLE)) {
                return existingAddress;
            } else if (value.equals(NEEDS_APPROVAL) && !existingAddress.equals(updatedAddress)) {
                map.put(addressType, updatedAddress.toString());
                return existingAddress;
            }
        }
        return updatedAddress;
    }

    private String toBeApproved(Map<String, String> map, String key, String existingPatientKeyValue, String patientToBeUpdatedKeyValue) {
        String value = properties.getProperty(key);
        if (value != null && patientToBeUpdatedKeyValue != null) {
            if (value.equals(NON_UPDATEABLE)) {
                return existingPatientKeyValue;
            } else if (value.equals(NEEDS_APPROVAL) && !existingPatientKeyValue.equals(patientToBeUpdatedKeyValue))
                map.put(key, patientToBeUpdatedKeyValue);
            return existingPatientKeyValue;
        }
        return patientToBeUpdatedKeyValue;
    }
}
