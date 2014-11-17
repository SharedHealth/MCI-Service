package org.sharedhealth.mci.web.handler;

import org.sharedhealth.mci.web.infrastructure.persistence.Approval;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.PhoneNumber;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.sharedhealth.mci.web.utils.PatientFieldProperties.*;

public class PatientFilter {

    private static final String NEEDS_APPROVAL = "NA";
    private static final String NON_UPDATEABLE = "NU";
    private Properties properties;
    private PatientMapper existingPatient;
    private PatientMapper patientToBeUpdated;
    private PatientMapper patientDto;

    public PatientFilter(Properties properties, PatientMapper existingPatient, PatientMapper patientToBeUpdated, PatientMapper patientDto) {
        this.properties = properties;
        this.existingPatient = existingPatient;
        this.patientToBeUpdated = patientToBeUpdated;
        this.patientDto = patientDto;
    }

    public Approval filter() {
        Map<String, String> filteredFeildMap = filterFeilds(patientDto);
        if (filteredFeildMap.isEmpty())
            return null;
        Approval approval = new Approval();
        approval.setHealth_id(patientDto.getHealthId());
        approval.setFacility_id("10000059");
        approval.setFieldsToApprovedMaps(filteredFeildMap);
        approval.setDatetime(new Date());
        return approval;
    }

    private Map<String, String> filterFeilds(PatientMapper patientDto) {
        HashMap<String, String> map = new HashMap<>();
        patientDto.setHealthId(toBeApproved(map, HID, existingPatient.getHealthId(), patientToBeUpdated.getHealthId()));
        patientDto.setNationalId(toBeApproved(map, NID, existingPatient.getNationalId(), patientToBeUpdated.getNationalId()));
        patientDto.setNameBangla(toBeApproved(map, NAME_BANGLA, existingPatient.getNameBangla(), patientToBeUpdated.getNameBangla()));
        patientDto.setBirthRegistrationNumber(toBeApproved(map, BIN_BRN, existingPatient.getBirthRegistrationNumber(), patientToBeUpdated.getBirthRegistrationNumber()));
        patientDto.setGivenName(toBeApproved(map, GIVEN_NAME, existingPatient.getGivenName(), patientToBeUpdated.getGivenName()));
        patientDto.setSurName(toBeApproved(map, SUR_NAME, existingPatient.getSurName(), patientToBeUpdated.getSurName()));
        patientDto.setDateOfBirth(toBeApproved(map, DATE_OF_BIRTH, existingPatient.getDateOfBirth(), patientToBeUpdated.getDateOfBirth()));
        patientDto.setGender(toBeApproved(map, GENDER, existingPatient.getGender(), patientToBeUpdated.getGender()));
        patientDto.setOccupation(toBeApproved(map, OCCUPATION, existingPatient.getOccupation(), patientToBeUpdated.getOccupation()));
        patientDto.setEducationLevel(toBeApproved(map, EDU_LEVEL, existingPatient.getEducationLevel(), patientToBeUpdated.getEducationLevel()));
        //TODO : rewrite after relations bug is fixed.
        patientDto.setRelations(patientToBeUpdated.getRelations());
        patientDto.setUid(toBeApproved(map, UID, existingPatient.getUid(), patientToBeUpdated.getUid()));
        patientDto.setPlaceOfBirth(toBeApproved(map, PLACE_OF_BIRTH, existingPatient.getPlaceOfBirth(), patientToBeUpdated.getPlaceOfBirth()));
        patientDto.setReligion(toBeApproved(map, RELIGION, existingPatient.getReligion(), patientToBeUpdated.getReligion()));
        patientDto.setBloodGroup(toBeApproved(map, BLOOD_GROUP, existingPatient.getBloodGroup(), patientToBeUpdated.getBloodGroup()));
        patientDto.setNationality(toBeApproved(map, NATIONALITY, existingPatient.getNationality(), patientToBeUpdated.getNationality()));
        patientDto.setDisability(toBeApproved(map, DISABILITY, existingPatient.getDisability(), patientToBeUpdated.getDisability()));
        patientDto.setEthnicity(toBeApproved(map, ETHNICITY, existingPatient.getEthnicity(), patientToBeUpdated.getEthnicity()));
        patientDto.setPrimaryContact(toBeApproved(map, PRIMARY_CONTACT, existingPatient.getPrimaryContact(), patientToBeUpdated.getPrimaryContact()));
        patientDto.setMaritalStatus(toBeApproved(map, MARITAL_STATUS, existingPatient.getMaritalStatus(), patientToBeUpdated.getMaritalStatus()));
        patientDto.setFullName(toBeApproved(map, FULL_NAME, existingPatient.getFullName(), patientToBeUpdated.getFullName()));
        patientDto.setIsAlive(toBeApproved(map, IS_ALIVE, existingPatient.getIsAlive(), patientToBeUpdated.getIsAlive()));
        patientDto.setCreatedAt(toBeApproved(map, CREATED, existingPatient.getCreatedAt(), patientToBeUpdated.getCreatedAt()));
        patientDto.setUpdatedAt(toBeApproved(map, MODIFIED, existingPatient.getUpdatedAt(), patientToBeUpdated.getUpdatedAt()));
        patientDto.setPhoneNumber(toBeapprovedPhoneNumber(map, PHONE_NUMBER, existingPatient.getPhoneNumber(), patientToBeUpdated.getPhoneNumber()));
        if (patientToBeUpdated.getAddress() != null) {
            patientDto.setAddress(toBeApprovedAddress(map, PRESENT_ADDRESS, existingPatient.getAddress(), patientToBeUpdated.getAddress()));
        }
        if (patientToBeUpdated.getPermanentAddress() != null) {
            patientDto.setPermanentAddress(toBeApprovedAddress(map, PERMANENT_ADDRESS, existingPatient.getPermanentAddress(), patientToBeUpdated.getPermanentAddress()));
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

    private Date toBeApproved(Map<String, String> map, String key, Date existingPatientKeyValue, Date patientToBeUpdatedKeyValue) {
        String value = properties.getProperty(key);
        if (value != null && patientToBeUpdatedKeyValue != null) {
            if (value.equals(NON_UPDATEABLE)) {
                return existingPatientKeyValue;
            } else if (value.equals(NEEDS_APPROVAL) && !existingPatientKeyValue.equals(patientToBeUpdatedKeyValue))
                map.put(key, patientToBeUpdatedKeyValue.toString());
            return existingPatientKeyValue;
        }
        return patientToBeUpdatedKeyValue;
    }
}
