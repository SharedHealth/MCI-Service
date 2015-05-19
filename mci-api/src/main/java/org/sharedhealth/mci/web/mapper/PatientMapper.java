package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.utils.DateUtil;
import org.sharedhealth.mci.web.exception.InvalidRequesterException;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.utils.PatientDataConstants;
import org.springframework.stereotype.Component;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_INVALID;
import static org.sharedhealth.mci.web.utils.JsonConstants.RELATIONS;
import static org.sharedhealth.mci.web.utils.PatientDataConstants.COUNTRY_CODE_BANGLADESH;
import static org.sharedhealth.mci.web.utils.PatientDataConstants.PATIENT_STATUS_ALIVE;
import static org.sharedhealth.mci.web.utils.PatientDataConstants.STRING_YES;

@Component
public class PatientMapper {

    private ObjectMapper objectMapper = new ObjectMapper();

    public List<PatientData> map(List<Patient> patients) {
        List<PatientData> dataList = new ArrayList<>();
        if (isNotEmpty(patients)) {
            for (Patient patient : patients) {
                PatientData data = this.map(patient);
                dataList.add(data);
            }
        }
        return dataList;
    }

    public List<PatientSummaryData> mapSummary(List<PatientData> patients) {
        List<PatientSummaryData> dataList = new ArrayList<>();
        if (isNotEmpty(patients)) {
            for (PatientData patient : patients) {
                PatientSummaryData data = this.mapSummary(patient);
                dataList.add(data);
            }
        }
        return dataList;
    }

    public PatientData map(Patient patient) {
        PatientData data = new PatientData();
        if (patient.getRelations() != null) {
            try {
                List<Relation> relations = objectMapper.readValue(patient.getRelations(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Relation.class));
                data.setRelations(relations);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        data.setHealthId(patient.getHealthId());
        data.setNationalId(patient.getNationalId());
        data.setBirthRegistrationNumber(patient.getBirthRegistrationNumber());
        data.setUid(patient.getUid());
        data.setPlaceOfBirth(patient.getPlaceOfBirth());

        data.setReligion(patient.getReligion());
        data.setBloodGroup(patient.getBloodGroup());
        data.setNameBangla(patient.getFullNameBangla());

        data.setGivenName(patient.getGivenName());
        data.setSurName(patient.getSurName());

        if (patient.getDateOfBirth() != null) {
            data.setDateOfBirth(toIsoFormat(patient.getDateOfBirth()));
        }
        data.setGender(patient.getGender());
        data.setOccupation(patient.getOccupation());
        data.setEducationLevel(patient.getEducationLevel());
        data.setNationality(patient.getNationality());
        data.setDisability(patient.getDisability());
        data.setEthnicity(patient.getEthnicity());

        PatientStatus patientStatus = new PatientStatus();
        patientStatus.setType(patient.getStatus());
        if (isPatientDeadAndHasDateOfDeath(patient)) {
            patientStatus.setDateOfDeath(toIsoFormat(patient.getDateOfDeath()));
        }

        if(!patientStatus.isEmpty()) {
            data.setPatientStatus(patientStatus);
        }

        data.setActive(patient.getActive());
        data.setMergedWith(patient.getMergedWith());

        if (patient.getConfidential() != null) {
            mapConfidentiality(patient, data);
        }

        data.setMaritalStatus(patient.getMaritalStatus());
        data.setPrimaryContact(patient.getPrimaryContact());

        Address address = new Address();
        address.setAddressLine(patient.getAddressLine());
        address.setDivisionId(patient.getDivisionId());
        address.setDistrictId(patient.getDistrictId());
        address.setUpazilaId(patient.getUpazilaId());
        address.setCityCorporationId(patient.getCityCorporationId());
        address.setUnionOrUrbanWardId(patient.getUnionOrUrbanWardId());
        address.setRuralWardId(patient.getRuralWardId());
        address.setHoldingNumber(patient.getHoldingNumber());
        address.setStreet(patient.getStreet());
        address.setAreaMouja(patient.getAreaMouja());
        address.setVillage(patient.getVillage());
        address.setPostOffice(patient.getPostOffice());
        address.setPostCode(patient.getPostCode());
        address.setCountryCode(patient.getCountryCode());
        data.setAddress(address);

        Address permanentAddress = new Address();
        permanentAddress.setAddressLine(patient.getPermanentAddressLine());
        permanentAddress.setDivisionId(patient.getPermanentDivisionId());
        permanentAddress.setDistrictId(patient.getPermanentDistrictId());
        permanentAddress.setUpazilaId(patient.getPermanentUpazilaId());
        permanentAddress.setCityCorporationId(patient.getPermanentCityCorporationId());
        permanentAddress.setUnionOrUrbanWardId(patient.getPermanentUnionOrUrbanWardId());
        permanentAddress.setRuralWardId(patient.getPermanentRuralWardId());
        permanentAddress.setHoldingNumber(patient.getPermanentHoldingNumber());
        permanentAddress.setStreet(patient.getPermanentStreet());
        permanentAddress.setAreaMouja(patient.getPermanentAreaMouja());
        permanentAddress.setVillage(patient.getPermanentVillage());
        permanentAddress.setPostOffice(patient.getPermanentPostOffice());
        permanentAddress.setPostCode(patient.getPermanentPostCode());

        if (!permanentAddress.isEmpty()) {
            permanentAddress.setCountryCode(patient.getPermanentCountryCode());
            data.setPermanentAddress(permanentAddress);
        }

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(patient.getCellNo());
        phoneNumber.setAreaCode(patient.getPhoneNumberAreaCode());
        phoneNumber.setCountryCode(patient.getPhoneNumberCountryCode());
        phoneNumber.setExtension(patient.getPhoneNumberExtension());
        if (phoneNumber.getNumber() != null) {
            data.setPhoneNumber(phoneNumber);
        }

        PhoneNumber primaryContactNumber = new PhoneNumber();
        primaryContactNumber.setNumber(patient.getPrimaryCellNo());
        primaryContactNumber.setAreaCode(patient.getPrimaryContactNumberAreaCode());
        primaryContactNumber.setExtension(patient.getPrimaryContactNumberExtension());
        primaryContactNumber.setCountryCode(patient.getPrimaryContactNumberCountryCode());
        if (primaryContactNumber.getNumber() != null) {
            data.setPrimaryContactNumber(primaryContactNumber);
        }
        data.setPendingApprovals(patient.getPendingApprovals());
        data.setCreatedAt(patient.getCreatedAt());
        data.setUpdatedAt(patient.getUpdatedAt());
        data.setCreatedBy(patient.getCreatedBy());
        data.setUpdatedBy(patient.getUpdatedBy());
        data.setHouseholdCode(patient.getHouseholdCode());
        return data;
    }

    private boolean isPatientDeadAndHasDateOfDeath(Patient patient) {
        return patient.getDateOfDeath() != null && PatientDataConstants.PATIENT_STATUS_DEAD.equals(patient.getStatus()) && DateUtil.getYearOf(patient.getDateOfDeath()) > 1;
    }

    private void mapConfidentiality(Patient patient, PatientData data) {
        if (patient.getConfidential()) {
            data.setConfidential(PatientDataConstants.STRING_YES);
        } else {
            data.setConfidential(PatientDataConstants.STRING_NO);
        }
    }

    public Patient map(PatientData data, PatientData existing) {
        Patient patient = new Patient();
        prepareRelationBlock(data, existing, patient);

        Address address = data.getAddress();
        Address permanentAddress = data.getPermanentAddress();
        PatientStatus patientStatus = data.getPatientStatus();
        PhoneNumber phoneNumber = data.getPhoneNumber();
        PhoneNumber primaryContactNumber = data.getPrimaryContactNumber();

        patient.setHealthId(data.getHealthId());
        patient.setNationalId(data.getNationalId());
        patient.setBirthRegistrationNumber(data.getBirthRegistrationNumber());
        patient.setFullNameBangla(StringUtils.trim(data.getNameBangla()));
        patient.setGivenName(StringUtils.trim(data.getGivenName()));
        patient.setSurName(StringUtils.trim(data.getSurName()));
        patient.setDateOfBirth(data.getDateOfBirth());
        patient.setGender(data.getGender());
        patient.setOccupation(data.getOccupation());
        patient.setEducationLevel(data.getEducationLevel());

        patient.setUid(data.getUid());
        patient.setPlaceOfBirth(StringUtils.trim(data.getPlaceOfBirth()));
        patient.setReligion(data.getReligion());
        patient.setBloodGroup(data.getBloodGroup());
        patient.setNationality(StringUtils.trim(data.getNationality()));
        patient.setDisability(data.getDisability());
        patient.setEthnicity(data.getEthnicity());

        patient.setMaritalStatus(data.getMaritalStatus());

        if (data.getConfidential() != null) {
            patient.setConfidential(STRING_YES.equalsIgnoreCase(data.getConfidential()));
        }

        if (address != null) {
            mapAddress(patient, address);
        }

        if (permanentAddress != null) {
            mapPermanentAddress(patient, permanentAddress);
        }

        if (phoneNumber != null) {
            mapPhoneNumber(patient, phoneNumber);
        }

        if(patientStatus != null) {
            mapPatientStatus(patient, patientStatus);
        }

        if (primaryContactNumber != null) {
            mapPrimaryContactNumber(patient, primaryContactNumber);
        }

        mapPatientActivation(patient, data);

        patient.setPrimaryContact(StringUtils.trim(data.getPrimaryContact()));
        patient.setPendingApprovals(data.getPendingApprovals());
        patient.setHouseholdCode(data.getHouseholdCode());

        return patient;
    }

    private void mapPrimaryContactNumber(Patient patient, PhoneNumber primaryContactNumber) {
        patient.setPrimaryCellNo(defaultString(primaryContactNumber.getNumber()));
        patient.setPrimaryContactNumberAreaCode(defaultString(primaryContactNumber.getAreaCode()));
        patient.setPrimaryContactNumberCountryCode(defaultString(primaryContactNumber.getCountryCode()));
        patient.setPrimaryContactNumberExtension(defaultString(primaryContactNumber.getExtension()));
    }

    private void mapPhoneNumber(Patient patient, PhoneNumber phoneNumber) {
        patient.setCellNo(defaultString(phoneNumber.getNumber()));
        patient.setPhoneNumberAreaCode(defaultString(phoneNumber.getAreaCode()));
        patient.setPhoneNumberCountryCode(defaultString(phoneNumber.getCountryCode()));
        patient.setPhoneNumberExtension(defaultString(phoneNumber.getExtension()));
    }

    private void mapPatientStatus(Patient patient, PatientStatus patientStatus) {
        patient.setStatus(defaultString(patientStatus.getType(), PATIENT_STATUS_ALIVE));
        patient.setDateOfDeath(defaultString(patientStatus.getDateOfDeath()));
    }

    private void mapPatientActivation(Patient patient, PatientData patientData) {
        if (null != patientData.getActive() && patientData.getActive() && StringUtils.isNotBlank(patientData.getMergedWith())) {
            throw new InvalidRequesterException("Active Patient cannot be merged with other patient");
        }
        patient.setActive(patientData.getActive());
        patient.setMergedWith(patientData.getMergedWith());
    }

    private void mapPermanentAddress(Patient patient, Address permanentAddress) {
        patient.setPermanentAddressLine(defaultString(permanentAddress.getAddressLine()));
        patient.setPermanentDivisionId(defaultString(permanentAddress.getDivisionId()));
        patient.setPermanentDistrictId(defaultString(permanentAddress.getDistrictId()));
        patient.setPermanentUpazilaId(defaultString(permanentAddress.getUpazilaId()));
        patient.setPermanentUnionOrUrbanWardId(defaultString(permanentAddress.getUnionOrUrbanWardId()));
        patient.setPermanentHoldingNumber(defaultString(StringUtils.trim(permanentAddress.getHoldingNumber())));
        patient.setPermanentStreet(defaultString(StringUtils.trim(permanentAddress.getStreet())));
        patient.setPermanentAreaMouja(defaultString(StringUtils.trim(permanentAddress.getAreaMouja())));
        patient.setPermanentVillage(defaultString(StringUtils.trim(permanentAddress.getVillage())));
        patient.setPermanentPostOffice(defaultString(StringUtils.trim(permanentAddress.getPostOffice())));
        patient.setPermanentPostCode(defaultString(permanentAddress.getPostCode()));
        patient.setPermanentRuralWardId(defaultString(permanentAddress.getRuralWardId()));
        patient.setPermanentCityCorporationId(defaultString(permanentAddress.getCityCorporationId()));

        if (permanentAddress.isEmpty()) {
            patient.setPermanentCountryCode("");
        } else {
            patient.setPermanentCountryCode(permanentAddress.getCountryCode());
        }
    }

    private void mapAddress(Patient patient, Address address) {
        patient.setAddressLine(address.getAddressLine());
        patient.setDivisionId(address.getDivisionId());
        patient.setDistrictId(address.getDistrictId());
        patient.setUpazilaId(address.getUpazilaId());
        patient.setUnionOrUrbanWardId(defaultString(address.getUnionOrUrbanWardId()));
        patient.setHoldingNumber(defaultString(StringUtils.trim(address.getHoldingNumber())));
        patient.setStreet(defaultString(StringUtils.trim(address.getStreet())));
        patient.setAreaMouja(defaultString(StringUtils.trim(address.getAreaMouja())));
        patient.setVillage(defaultString(StringUtils.trim(address.getVillage())));
        patient.setPostOffice(defaultString(StringUtils.trim(address.getPostOffice())));
        patient.setPostCode(defaultString(address.getPostCode()));
        patient.setRuralWardId(defaultString(address.getRuralWardId()));
        patient.setCityCorporationId(defaultString(address.getCityCorporationId()));
        patient.setCountryCode(defaultString(address.getCountryCode(), COUNTRY_CODE_BANGLADESH));
    }

    private PatientSummaryData mapSummary(PatientData patient) {

        if (patient == null) {
            return null;
        }

        PatientSummaryData data = new PatientSummaryData();

        data.setHealthId(patient.getHealthId());
        data.setNationalId(patient.getNationalId());
        data.setUid(patient.getUid());
        data.setBirthRegistrationNumber(patient.getBirthRegistrationNumber());
        data.setGivenName(StringUtils.trim(patient.getGivenName()));
        data.setSurName(StringUtils.trim(patient.getSurName()));
        data.setDateOfBirth(patient.getDateOfBirth());
        data.setGender(patient.getGender());
        data.setAddress(patient.getAddress());
        data.setPhoneNumber(patient.getPhoneNumber());
        data.setActive(patient.getActive());
        data.setMergedWith(patient.getMergedWith());

        return data;
    }

    private void prepareRelationBlock(PatientData data, PatientData existingData, Patient patient) {

        List<Relation> relations = data.getRelations();

        if (relations == null) {
            return;
        }

        removeDuplicateRelationBlock(relations);
        appendExistingRelationBlock(data, existingData);
        handleRelationRemovalRequest(relations);
        populateRelationId(relations);
        prepareFathersInfo(data, patient);
        prepareMothersInfo(data, patient);

        try {
            patient.setRelations(objectMapper.writeValueAsString(data.getRelations()));
        } catch (JsonProcessingException e) {
            System.out.println("Error on preparing relation : " + e.getMessage());
        }
    }

    private void removeDuplicateRelationBlock(List<Relation> r) {
        try {
            Set<Relation> uniqueRelations = new LinkedHashSet<>(r);
            r.clear();
            r.addAll(uniqueRelations);
        } catch (Exception e) {
            System.out.println("Error on removing duplicate : " + e.getMessage());
        }
    }

    private void appendExistingRelationBlock(PatientData patient, PatientData existingPatient) {

        List<Relation> r = patient.getRelations();

        if (r == null) {
            r = new ArrayList<>();
        }

        List<Relation> relations = existingPatient.getRelations();

        if (!isValidRelationBlock(r, relations)) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patient, "patient");
            bindingResult.addError(new FieldError("patient", RELATIONS, ERROR_CODE_INVALID));
            throw new ValidationException(bindingResult);
        }

        if (relations == null) {
            return;
        }

        for (Relation relation : relations) {
            if (r.contains(relation)) {
                r.get(r.indexOf(relation)).setId(relation.getId());
            } else if (patient.getRelationById(relation.getId()) == null) {
                r.add(relation);
            }
        }
    }

    private void handleRelationRemovalRequest(List<Relation> relations) {
        if (relations == null) {
            return;
        }

        List<Relation> relationsToRemoved = new ArrayList<>();

        for (Relation relation : relations) {
            if (relation.isEmpty()) {
                relationsToRemoved.add(relation);
            }
        }

        for (Relation relation : relationsToRemoved) {
            relations.remove(relation);
        }
    }

    private void populateRelationId(List<Relation> r) {
        for (Relation aR : r) {
            if (StringUtils.isBlank(aR.getId())) {
                aR.setId(UUID.randomUUID().toString());
            }
        }
    }

    private void prepareFathersInfo(PatientData data, Patient patient) {
        Relation father = data.getRelationOfType("FTH");
        if (father != null) {
            patient.setFathersNameBangla(StringUtils.trim(father.getNameBangla()));
            patient.setFathersGivenName(StringUtils.trim(father.getGivenName()));
            patient.setFathersSurName(StringUtils.trim(father.getSurName()));
            patient.setFathersBrn(father.getBirthRegistrationNumber());
            patient.setFathersNid(father.getNationalId());
            patient.setFathersUid(father.getUid());
        } else {
            patient.setFathersNameBangla("");
            patient.setFathersGivenName("");
            patient.setFathersSurName("");
            patient.setFathersBrn("");
            patient.setFathersNid("");
            patient.setFathersUid("");
        }
    }

    private void prepareMothersInfo(PatientData data, Patient patient) {
        Relation mother = data.getRelationOfType("MTH");

        if (mother != null) {
            patient.setMothersNameBangla(StringUtils.trim(mother.getNameBangla()));
            patient.setMothersGivenName(StringUtils.trim(mother.getGivenName()));
            patient.setMothersSurName(StringUtils.trim(mother.getSurName()));
            patient.setMothersBrn(mother.getBirthRegistrationNumber());
            patient.setMothersNid(mother.getNationalId());
            patient.setMothersUid(mother.getUid());
        } else {
            patient.setMothersNameBangla("");
            patient.setMothersGivenName("");
            patient.setMothersSurName("");
            patient.setMothersBrn("");
            patient.setMothersNid("");
            patient.setMothersUid("");
        }
    }

    private Boolean isValidRelationBlock(List<Relation> relations, List<Relation> existing) {
        for (Relation relation : relations) {
            if (isNotBlank(relation.getId()) && !relationExistWithId(existing, relation.getId())) {
                return false;
            }
        }
        return true;
    }

    public Boolean relationExistWithId(List<Relation> relations, String id) {
        if (relations == null) {
            return false;
        }
        for (Relation relation : relations) {

            if (relation.getId() != null && relation.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public PatientSummaryData mapSummary(Patient patient) {
        if (patient == null) {
            return null;
        }

        PatientSummaryData data = new PatientSummaryData();

        data.setHealthId(patient.getHealthId());

        return data;
    }
}
