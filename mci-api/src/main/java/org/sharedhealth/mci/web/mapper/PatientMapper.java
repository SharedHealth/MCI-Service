package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.model.Patient;
import org.springframework.stereotype.Component;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
        data.setDateOfBirth(ISO_DATE_FORMAT.format(patient.getDateOfBirth()));
        data.setGender(patient.getGender());
        data.setOccupation(patient.getOccupation());
        data.setEducationLevel(patient.getEducationLevel());
        data.setNationality(patient.getNationality());
        data.setDisability(patient.getDisability());
        data.setEthnicity(patient.getEthnicity());
        data.setIsAlive(patient.getIsAlive());
        data.setMaritalStatus(patient.getMaritalStatus());
        data.setPrimaryContact(patient.getPrimaryContact());

        Address address = new Address();
        address.setAddressLine(patient.getAddressLine());
        address.setDivisionId(patient.getDivisionId());
        address.setDistrictId(patient.getDistrictId());
        address.setUpazillaId(patient.getUpazillaId());
        address.setCityCorporationId(patient.getCityCorporationId());
        address.setUnionId(patient.getUnionId());
        address.setWardId(patient.getWardId());
        address.setHoldingNumber(patient.getHoldingNumber());
        address.setStreet(patient.getStreet());
        address.setAreaMouja(patient.getAreaMouja());
        address.setVillage(patient.getVillage());
        address.setPostOffice(patient.getPostOffice());
        address.setPostCode(patient.getPostCode());
        address.setThanaId(patient.getThanaId());
        address.setCountryCode(patient.getCountryCode());
        data.setAddress(address);

        Address permanentAddress = new Address();
        permanentAddress.setAddressLine(patient.getPermanentAddressLine());
        permanentAddress.setDivisionId(patient.getPermanentDivisionId());
        permanentAddress.setDistrictId(patient.getPermanentDistrictId());
        permanentAddress.setUpazillaId(patient.getPermanentUpazillaId());
        permanentAddress.setCityCorporationId(patient.getPermanentCityCorporationId());
        permanentAddress.setUnionId(patient.getPermanentUnionId());
        permanentAddress.setWardId(patient.getPermanentWardId());
        permanentAddress.setHoldingNumber(patient.getPermanentHoldingNumber());
        permanentAddress.setStreet(patient.getPermanentStreet());
        permanentAddress.setAreaMouja(patient.getPermanentAreaMouja());
        permanentAddress.setVillage(patient.getPermanentVillage());
        permanentAddress.setPostOffice(patient.getPermanentPostOffice());
        permanentAddress.setPostCode(patient.getPermanentPostCode());
        permanentAddress.setThanaId(patient.getPermanentThanaId());
        permanentAddress.setCountryCode(patient.getPermanentCountryCode());
        if (permanentAddress.getCountryCode() != null) {
            if ("050".equals(permanentAddress.getCountryCode()) && permanentAddress.getDistrictId() != null) {
                data.setPermanentAddress(permanentAddress);
            }
            if (!"050".equals(permanentAddress.getCountryCode())) {
                data.setPermanentAddress(permanentAddress);
            }
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
        return data;
    }

    public Patient map(PatientData data, PatientData existing) {
        Patient patient = new Patient();
        prepareRelationBlock(data, existing, patient);

        Address address = data.getAddress();
        Address permanentAddress = data.getPermanentAddress();

        PhoneNumber phoneNumber = data.getPhoneNumber();
        PhoneNumber primaryContactNumber = data.getPrimaryContactNumber();

        patient.setHealthId(data.getHealthId());
        patient.setNationalId(data.getNationalId());
        patient.setBirthRegistrationNumber(data.getBirthRegistrationNumber());
        patient.setFullNameBangla(StringUtils.trim(data.getNameBangla()));
        patient.setGivenName(StringUtils.trim(data.getGivenName()));
        if (data.getGivenName() != null) {
            patient.setLowerGivenName(StringUtils.trim(data.getGivenName()).toLowerCase());
        }
        patient.setSurName(StringUtils.trim(data.getSurName()));
        if (data.getSurName() != null) {
            patient.setLowerSurName(StringUtils.trim(data.getSurName()).toLowerCase());
        }
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
        patient.setIsAlive(data.getIsAlive());
        patient.setMaritalStatus(data.getMaritalStatus());

        if (address != null) {
            patient.setAddressLine(address.getAddressLine());
            patient.setDivisionId(address.getDivisionId());
            patient.setDistrictId(address.getDistrictId());
            patient.setUpazillaId(address.getUpazillaId());
            patient.setUnionId(address.getUnionId());
            patient.setHoldingNumber(StringUtils.trim(address.getHoldingNumber()));
            patient.setStreet(StringUtils.trim(address.getStreet()));
            patient.setAreaMouja(StringUtils.trim(address.getAreaMouja()));
            patient.setVillage(StringUtils.trim(address.getVillage()));
            patient.setPostOffice(StringUtils.trim(address.getPostOffice()));
            patient.setPostCode(address.getPostCode());
            patient.setWardId(address.getWardId());
            patient.setThanaId(address.getThanaId());
            patient.setCityCorporationId(address.getCityCorporationId());
            patient.setCountryCode(address.getCountryCode());
        }

        if (permanentAddress != null) {
            patient.setPermanentAddressLine(permanentAddress.getAddressLine());
            patient.setPermanentDivisionId(permanentAddress.getDivisionId());
            patient.setPermanentDistrictId(permanentAddress.getDistrictId());
            patient.setPermanentUpazillaId(permanentAddress.getUpazillaId());
            patient.setPermanentUnionId(permanentAddress.getUnionId());
            patient.setPermanentHoldingNumber(StringUtils.trim(permanentAddress.getHoldingNumber()));
            patient.setPermanentStreet(StringUtils.trim(permanentAddress.getStreet()));
            patient.setPermanentAreaMouja(StringUtils.trim(permanentAddress.getAreaMouja()));
            patient.setPermanentVillage(StringUtils.trim(permanentAddress.getVillage()));
            patient.setPermanentPostOffice(StringUtils.trim(permanentAddress.getPostOffice()));
            patient.setPermanentPostCode(permanentAddress.getPostCode());
            patient.setPermanentWardId(permanentAddress.getWardId());
            patient.setPermanentThanaId(permanentAddress.getThanaId());
            patient.setPermanentCityCorporationId(permanentAddress.getCityCorporationId());
            patient.setPermanentCountryCode(permanentAddress.getCountryCode());
        }

        if (phoneNumber != null) {
            patient.setCellNo(phoneNumber.getNumber());
            patient.setPhoneNumberAreaCode(phoneNumber.getAreaCode());
            patient.setPhoneNumberCountryCode(phoneNumber.getCountryCode());
            patient.setPhoneNumberExtension(phoneNumber.getExtension());
        }

        if (primaryContactNumber != null) {
            patient.setPrimaryCellNo(primaryContactNumber.getNumber());
            patient.setPrimaryContactNumberAreaCode(primaryContactNumber.getAreaCode());
            patient.setPrimaryContactNumberCountryCode(primaryContactNumber.getCountryCode());
            patient.setPrimaryContactNumberExtension(primaryContactNumber.getExtension());
        }

        patient.setPrimaryContact(StringUtils.trim(data.getPrimaryContact()));
        patient.setPendingApprovals(existing.getPendingApprovals());

        return patient;
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
            bindingResult.addError(new FieldError("patient", "relations", "1004"));
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
        int y = r.size();
        for (int x = 0; x < y; x = x + 1) {
            if (StringUtils.isBlank(r.get(x).getId())) {
                r.get(x).setId(UUID.randomUUID().toString());
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
}
