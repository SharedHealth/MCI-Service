package org.sharedhealth.mci.web.model;


import com.fasterxml.jackson.core.type.TypeReference;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PendingApproval;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;
import java.util.TreeSet;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.sharedhealth.mci.utils.DateUtil.ISO_DATE_FORMAT;
import static org.sharedhealth.mci.utils.DateUtil.parseDate;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.ADDRESS_LINE;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.AREA_MOUJA;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.BIN_BRN;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.BLOOD_GROUP;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.CREATED_AT;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.DATE_OF_BIRTH;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.DATE_OF_DEATH;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.DISABILITY;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.DISTRICT_ID;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.DIVISION_ID;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.EDU_LEVEL;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.ETHNICITY;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.GENDER;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.GIVEN_NAME;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.HOLDING_NUMBER;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.MARITAL_STATUS;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.NATIONALITY;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.OCCUPATION;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.PHONE_NO;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.PLACE_OF_BIRTH;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.POST_CODE;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.POST_OFFICE;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.PRIMARY_CONTACT;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.RELATIONS;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.RELIGION;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.RURAL_WARD_ID;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.STREET;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.SUR_NAME;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.UID;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.UNION_OR_URBAN_WARD_ID;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.UPAZILA_ID;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.VILLAGE;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.STATUS;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.sharedhealth.mci.web.utils.JsonMapper.readValue;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;
import static org.sharedhealth.mci.web.utils.PatientDataConstants.COUNTRY_CODE_BANGLADESH;

@Table(value = "patient")
public class Patient {

    public static final String EMPTY_DATE_VALUE = "0001-01-01";
    @PrimaryKey(HEALTH_ID)
    private String healthId;

    @Column(NATIONAL_ID)
    private String nationalId;

    @Column(BIN_BRN)
    private String birthRegistrationNumber;

    @Column(FULL_NAME_BANGLA)
    private String fullNameBangla;

    @Column(GIVEN_NAME)
    private String givenName;

    @Column(SUR_NAME)
    private String surName;

    @Column(DATE_OF_BIRTH)
    private Date dateOfBirth;

    @Column(GENDER)
    private String gender;

    @Column(OCCUPATION)
    private String occupation;

    @Column(EDU_LEVEL)
    private String educationLevel;

    @Column(FATHERS_NAME_BANGLA)
    private String fathersNameBangla;

    @Column(FATHERS_GIVEN_NAME)
    private String fathersGivenName;

    @Column(FATHERS_SUR_NAME)
    private String fathersSurName;

    @Column(FATHERS_UID)
    private String fathersUid;

    @Column(FATHERS_NID)
    private String fathersNid;

    @Column(FATHERS_BRN)
    private String fathersBrn;

    @Column(MOTHERS_NAME_BANGLA)
    private String mothersNameBangla;

    @Column(MOTHERS_GIVEN_NAME)
    private String mothersGivenName;

    @Column(MOTHERS_SUR_NAME)
    private String mothersSurName;

    @Column(MOTHERS_UID)
    private String mothersUid;

    @Column(MOTHERS_NID)
    private String mothersNid;

    @Column(MOTHERS_BRN)
    private String mothersBrn;

    @Column(UID)
    private String uid;

    @Column(PLACE_OF_BIRTH)
    private String placeOfBirth;

    @Column(MARITAL_STATUS)
    private String maritalStatus;

    @Column(RELIGION)
    private String religion;

    @Column(BLOOD_GROUP)
    private String bloodGroup;

    @Column(NATIONALITY)
    private String nationality;

    @Column(DISABILITY)
    private String disability;

    @Column(ETHNICITY)
    private String ethnicity;

    @Column(ADDRESS_LINE)
    private String addressLine;

    @Column(DIVISION_ID)
    private String divisionId;

    @Column(DISTRICT_ID)
    private String districtId;

    @Column(UPAZILA_ID)
    private String upazilaId;

    @Column(UNION_OR_URBAN_WARD_ID)
    private String unionOrUrbanWardId;

    @Column(HOLDING_NUMBER)
    private String holdingNumber;

    @Column(STREET)
    private String street;

    @Column(AREA_MOUJA)
    private String areaMouja;

    @Column(VILLAGE)
    private String village;

    @Column(POST_OFFICE)
    private String postOffice;

    @Column(POST_CODE)
    private String postCode;

    @Column(RURAL_WARD_ID)
    private String ruralWardId;

    @Column(CITY_CORPORATION)
    private String cityCorporationId;

    @Column(COUNTRY)
    private String countryCode;

    @Column(LOCATION_LEVEL1)
    private String locationLevel1;

    @Column(LOCATION_LEVEL2)
    private String locationLevel2;

    @Column(LOCATION_LEVEL3)
    private String locationLevel3;

    @Column(LOCATION_LEVEL4)
    private String locationLevel4;

    @Column(LOCATION_LEVEL5)
    private String locationLevel5;

    @Column(LOCATION_LEVEL6)
    private String locationLevel6;

    @Column(PERMANENT_ADDRESS_LINE)
    private String permanentAddressLine;

    @Column(PERMANENT_DIVISION_ID)
    private String permanentDivisionId;

    @Column(PERMANENT_DISTRICT_ID)
    private String permanentDistrictId;

    @Column(PERMANENT_UPAZILA_ID)
    private String permanentUpazilaId;

    @Column(PERMANENT_UNION_OR_URBAN_WARD_ID)
    private String permanentUnionOrUrbanWardId;

    @Column(PERMANENT_HOLDING_NUMBER)
    private String permanentHoldingNumber;

    @Column(PERMANENT_STREET)
    private String permanentStreet;

    @Column(PERMANENT_AREA_MOUJA)
    private String permanentAreaMouja;

    @Column(PERMANENT_VILLAGE)
    private String permanentVillage;

    @Column(PERMANENT_POST_OFFICE)
    private String permanentPostOffice;

    @Column(PERMANENT_POST_CODE)
    private String permanentPostCode;

    @Column(PERMANENT_RURAL_WARD_ID)
    private String permanentRuralWardId;

    @Column(PERMANENT_CITY_CORPORATION)
    private String permanentCityCorporationId;

    @Column(PERMANENT_COUNTRY)
    private String permanentCountryCode;

    @Column(FULL_NAME)
    private String fullName;

    @Column(STATUS)
    private String status;

    @Column(DATE_OF_DEATH)
    private Date dateOfDeath;

    @Column(PRIMARY_CONTACT)
    private String primaryContact;

    @Column(PRIMARY_CONTACT_NO)
    private String primaryCellNo;

    @Column(PHONE_NO)
    private String cellNo;

    @Column(PHONE_NUMBER_COUNTRY_CODE)
    private String phoneNumberCountryCode;

    @Column(PHONE_NUMBER_AREA_CODE)
    private String phoneNumberAreaCode;

    @Column(PHONE_NUMBER_EXTENSION)
    private String phoneNumberExtension;

    @Column(PRIMARY_CONTACT_NUMBER_COUNTRY_CODE)
    private String primaryContactNumberCountryCode;

    @Column(PRIMARY_CONTACT_NUMBER_AREA_CODE)
    private String primaryContactNumberAreaCode;

    @Column(PRIMARY_CONTACT_NUMBER_EXTENSION)
    private String primaryContactNumberExtension;

    @Column(CREATED_AT)
    private UUID createdAt;

    @Column(UPDATED_AT)
    private UUID updatedAt;

    @Column(PatientRepositoryConstants.CREATED_BY)
    private String createdBy;

    @Column(UPDATED_BY)
    private String updatedBy;

    @Column(RELATIONS)
    private String relations;

    @Column(LOWER_SUR_NAME)
    private String lowerSurName;

    @Column(LOWER_GIVEN_NAME)
    private String lowerGivenName;

    @Column(PENDING_APPROVALS)
    private String pendingApprovals;

    @Column(HOUSEHOLD_CODE)
    private String householdCode;

    @Column(CONFIDENTIAL)
    private Boolean confidential;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;

        Patient patient = (Patient) o;

        if (addressLine != null ? !addressLine.equals(patient.addressLine) : patient.addressLine != null) return false;
        if (areaMouja != null ? !areaMouja.equals(patient.areaMouja) : patient.areaMouja != null) return false;
        if (birthRegistrationNumber != null ? !birthRegistrationNumber.equals(patient.birthRegistrationNumber) : patient.birthRegistrationNumber != null)
            return false;
        if (bloodGroup != null ? !bloodGroup.equals(patient.bloodGroup) : patient.bloodGroup != null) return false;
        if (cellNo != null ? !cellNo.equals(patient.cellNo) : patient.cellNo != null) return false;
        if (cityCorporationId != null ? !cityCorporationId.equals(patient.cityCorporationId) : patient.cityCorporationId != null)
            return false;
        if (confidential != null ? !confidential.equals(patient.confidential) : patient.confidential != null)
            return false;
        if (countryCode != null ? !countryCode.equals(patient.countryCode) : patient.countryCode != null) return false;
        if (createdAt != null ? !createdAt.equals(patient.createdAt) : patient.createdAt != null) return false;
        if (createdBy != null ? !createdBy.equals(patient.createdBy) : patient.createdBy != null) return false;
        if (dateOfBirth != null ? !dateOfBirth.equals(patient.dateOfBirth) : patient.dateOfBirth != null) return false;
        if (dateOfDeath != null ? !dateOfDeath.equals(patient.dateOfDeath) : patient.dateOfDeath != null) return false;
        if (disability != null ? !disability.equals(patient.disability) : patient.disability != null) return false;
        if (districtId != null ? !districtId.equals(patient.districtId) : patient.districtId != null) return false;
        if (divisionId != null ? !divisionId.equals(patient.divisionId) : patient.divisionId != null) return false;
        if (educationLevel != null ? !educationLevel.equals(patient.educationLevel) : patient.educationLevel != null)
            return false;
        if (ethnicity != null ? !ethnicity.equals(patient.ethnicity) : patient.ethnicity != null) return false;
        if (fathersBrn != null ? !fathersBrn.equals(patient.fathersBrn) : patient.fathersBrn != null) return false;
        if (fathersGivenName != null ? !fathersGivenName.equals(patient.fathersGivenName) : patient.fathersGivenName != null)
            return false;
        if (fathersNameBangla != null ? !fathersNameBangla.equals(patient.fathersNameBangla) : patient.fathersNameBangla != null)
            return false;
        if (fathersNid != null ? !fathersNid.equals(patient.fathersNid) : patient.fathersNid != null) return false;
        if (fathersSurName != null ? !fathersSurName.equals(patient.fathersSurName) : patient.fathersSurName != null)
            return false;
        if (fathersUid != null ? !fathersUid.equals(patient.fathersUid) : patient.fathersUid != null) return false;
        if (fullName != null ? !fullName.equals(patient.fullName) : patient.fullName != null) return false;
        if (fullNameBangla != null ? !fullNameBangla.equals(patient.fullNameBangla) : patient.fullNameBangla != null)
            return false;
        if (gender != null ? !gender.equals(patient.gender) : patient.gender != null) return false;
        if (givenName != null ? !givenName.equals(patient.givenName) : patient.givenName != null) return false;
        if (healthId != null ? !healthId.equals(patient.healthId) : patient.healthId != null) return false;
        if (holdingNumber != null ? !holdingNumber.equals(patient.holdingNumber) : patient.holdingNumber != null)
            return false;
        if (locationLevel1 != null ? !locationLevel1.equals(patient.locationLevel1) : patient.locationLevel1 != null)
            return false;
        if (locationLevel2 != null ? !locationLevel2.equals(patient.locationLevel2) : patient.locationLevel2 != null)
            return false;
        if (locationLevel3 != null ? !locationLevel3.equals(patient.locationLevel3) : patient.locationLevel3 != null)
            return false;
        if (locationLevel4 != null ? !locationLevel4.equals(patient.locationLevel4) : patient.locationLevel4 != null)
            return false;
        if (locationLevel5 != null ? !locationLevel5.equals(patient.locationLevel5) : patient.locationLevel5 != null)
            return false;
        if (locationLevel6 != null ? !locationLevel6.equals(patient.locationLevel6) : patient.locationLevel6 != null)
            return false;
        if (lowerGivenName != null ? !lowerGivenName.equals(patient.lowerGivenName) : patient.lowerGivenName != null)
            return false;
        if (lowerSurName != null ? !lowerSurName.equals(patient.lowerSurName) : patient.lowerSurName != null)
            return false;
        if (maritalStatus != null ? !maritalStatus.equals(patient.maritalStatus) : patient.maritalStatus != null)
            return false;
        if (mothersBrn != null ? !mothersBrn.equals(patient.mothersBrn) : patient.mothersBrn != null) return false;
        if (mothersGivenName != null ? !mothersGivenName.equals(patient.mothersGivenName) : patient.mothersGivenName != null)
            return false;
        if (mothersNameBangla != null ? !mothersNameBangla.equals(patient.mothersNameBangla) : patient.mothersNameBangla != null)
            return false;
        if (mothersNid != null ? !mothersNid.equals(patient.mothersNid) : patient.mothersNid != null) return false;
        if (mothersSurName != null ? !mothersSurName.equals(patient.mothersSurName) : patient.mothersSurName != null)
            return false;
        if (mothersUid != null ? !mothersUid.equals(patient.mothersUid) : patient.mothersUid != null) return false;
        if (nationalId != null ? !nationalId.equals(patient.nationalId) : patient.nationalId != null) return false;
        if (nationality != null ? !nationality.equals(patient.nationality) : patient.nationality != null) return false;
        if (occupation != null ? !occupation.equals(patient.occupation) : patient.occupation != null) return false;
        if (pendingApprovals != null ? !pendingApprovals.equals(patient.pendingApprovals) : patient.pendingApprovals != null)
            return false;
        if (permanentAddressLine != null ? !permanentAddressLine.equals(patient.permanentAddressLine) : patient.permanentAddressLine != null)
            return false;
        if (permanentAreaMouja != null ? !permanentAreaMouja.equals(patient.permanentAreaMouja) : patient.permanentAreaMouja != null)
            return false;
        if (permanentCityCorporationId != null ? !permanentCityCorporationId.equals(patient.permanentCityCorporationId) : patient.permanentCityCorporationId != null)
            return false;
        if (permanentCountryCode != null ? !permanentCountryCode.equals(patient.permanentCountryCode) : patient.permanentCountryCode != null)
            return false;
        if (permanentDistrictId != null ? !permanentDistrictId.equals(patient.permanentDistrictId) : patient.permanentDistrictId != null)
            return false;
        if (permanentDivisionId != null ? !permanentDivisionId.equals(patient.permanentDivisionId) : patient.permanentDivisionId != null)
            return false;
        if (permanentHoldingNumber != null ? !permanentHoldingNumber.equals(patient.permanentHoldingNumber) : patient.permanentHoldingNumber != null)
            return false;
        if (permanentPostCode != null ? !permanentPostCode.equals(patient.permanentPostCode) : patient.permanentPostCode != null)
            return false;
        if (permanentPostOffice != null ? !permanentPostOffice.equals(patient.permanentPostOffice) : patient.permanentPostOffice != null)
            return false;
        if (permanentRuralWardId != null ? !permanentRuralWardId.equals(patient.permanentRuralWardId) : patient.permanentRuralWardId != null)
            return false;
        if (permanentStreet != null ? !permanentStreet.equals(patient.permanentStreet) : patient.permanentStreet != null)
            return false;
        if (permanentUnionOrUrbanWardId != null ? !permanentUnionOrUrbanWardId.equals(patient.permanentUnionOrUrbanWardId) : patient.permanentUnionOrUrbanWardId != null)
            return false;
        if (permanentUpazilaId != null ? !permanentUpazilaId.equals(patient.permanentUpazilaId) : patient.permanentUpazilaId != null)
            return false;
        if (permanentVillage != null ? !permanentVillage.equals(patient.permanentVillage) : patient.permanentVillage != null)
            return false;
        if (phoneNumberAreaCode != null ? !phoneNumberAreaCode.equals(patient.phoneNumberAreaCode) : patient.phoneNumberAreaCode != null)
            return false;
        if (phoneNumberCountryCode != null ? !phoneNumberCountryCode.equals(patient.phoneNumberCountryCode) : patient.phoneNumberCountryCode != null)
            return false;
        if (phoneNumberExtension != null ? !phoneNumberExtension.equals(patient.phoneNumberExtension) : patient.phoneNumberExtension != null)
            return false;
        if (placeOfBirth != null ? !placeOfBirth.equals(patient.placeOfBirth) : patient.placeOfBirth != null)
            return false;
        if (postCode != null ? !postCode.equals(patient.postCode) : patient.postCode != null) return false;
        if (postOffice != null ? !postOffice.equals(patient.postOffice) : patient.postOffice != null) return false;
        if (primaryCellNo != null ? !primaryCellNo.equals(patient.primaryCellNo) : patient.primaryCellNo != null)
            return false;
        if (primaryContact != null ? !primaryContact.equals(patient.primaryContact) : patient.primaryContact != null)
            return false;
        if (primaryContactNumberAreaCode != null ? !primaryContactNumberAreaCode.equals(patient.primaryContactNumberAreaCode) : patient.primaryContactNumberAreaCode != null)
            return false;
        if (primaryContactNumberCountryCode != null ? !primaryContactNumberCountryCode.equals(patient.primaryContactNumberCountryCode) : patient.primaryContactNumberCountryCode != null)
            return false;
        if (primaryContactNumberExtension != null ? !primaryContactNumberExtension.equals(patient.primaryContactNumberExtension) : patient.primaryContactNumberExtension != null)
            return false;
        if (relations != null ? !relations.equals(patient.relations) : patient.relations != null) return false;
        if (religion != null ? !religion.equals(patient.religion) : patient.religion != null) return false;
        if (ruralWardId != null ? !ruralWardId.equals(patient.ruralWardId) : patient.ruralWardId != null) return false;
        if (status != null ? !status.equals(patient.status) : patient.status != null) return false;
        if (street != null ? !street.equals(patient.street) : patient.street != null) return false;
        if (surName != null ? !surName.equals(patient.surName) : patient.surName != null) return false;
        if (uid != null ? !uid.equals(patient.uid) : patient.uid != null) return false;
        if (unionOrUrbanWardId != null ? !unionOrUrbanWardId.equals(patient.unionOrUrbanWardId) : patient.unionOrUrbanWardId != null)
            return false;
        if (upazilaId != null ? !upazilaId.equals(patient.upazilaId) : patient.upazilaId != null) return false;
        if (updatedAt != null ? !updatedAt.equals(patient.updatedAt) : patient.updatedAt != null) return false;
        if (updatedBy != null ? !updatedBy.equals(patient.updatedBy) : patient.updatedBy != null) return false;
        if (village != null ? !village.equals(patient.village) : patient.village != null) return false;
        if (householdCode != null ? !householdCode.equals(patient.householdCode) : patient.householdCode != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = healthId != null ? healthId.hashCode() : 0;
        result = 31 * result + (nationalId != null ? nationalId.hashCode() : 0);
        result = 31 * result + (birthRegistrationNumber != null ? birthRegistrationNumber.hashCode() : 0);
        result = 31 * result + (fullNameBangla != null ? fullNameBangla.hashCode() : 0);
        result = 31 * result + (givenName != null ? givenName.hashCode() : 0);
        result = 31 * result + (surName != null ? surName.hashCode() : 0);
        result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (occupation != null ? occupation.hashCode() : 0);
        result = 31 * result + (educationLevel != null ? educationLevel.hashCode() : 0);
        result = 31 * result + (fathersNameBangla != null ? fathersNameBangla.hashCode() : 0);
        result = 31 * result + (fathersGivenName != null ? fathersGivenName.hashCode() : 0);
        result = 31 * result + (fathersSurName != null ? fathersSurName.hashCode() : 0);
        result = 31 * result + (fathersUid != null ? fathersUid.hashCode() : 0);
        result = 31 * result + (fathersNid != null ? fathersNid.hashCode() : 0);
        result = 31 * result + (fathersBrn != null ? fathersBrn.hashCode() : 0);
        result = 31 * result + (mothersNameBangla != null ? mothersNameBangla.hashCode() : 0);
        result = 31 * result + (mothersGivenName != null ? mothersGivenName.hashCode() : 0);
        result = 31 * result + (mothersSurName != null ? mothersSurName.hashCode() : 0);
        result = 31 * result + (mothersUid != null ? mothersUid.hashCode() : 0);
        result = 31 * result + (mothersNid != null ? mothersNid.hashCode() : 0);
        result = 31 * result + (mothersBrn != null ? mothersBrn.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (placeOfBirth != null ? placeOfBirth.hashCode() : 0);
        result = 31 * result + (maritalStatus != null ? maritalStatus.hashCode() : 0);
        result = 31 * result + (religion != null ? religion.hashCode() : 0);
        result = 31 * result + (bloodGroup != null ? bloodGroup.hashCode() : 0);
        result = 31 * result + (nationality != null ? nationality.hashCode() : 0);
        result = 31 * result + (disability != null ? disability.hashCode() : 0);
        result = 31 * result + (ethnicity != null ? ethnicity.hashCode() : 0);
        result = 31 * result + (addressLine != null ? addressLine.hashCode() : 0);
        result = 31 * result + (divisionId != null ? divisionId.hashCode() : 0);
        result = 31 * result + (districtId != null ? districtId.hashCode() : 0);
        result = 31 * result + (upazilaId != null ? upazilaId.hashCode() : 0);
        result = 31 * result + (unionOrUrbanWardId != null ? unionOrUrbanWardId.hashCode() : 0);
        result = 31 * result + (holdingNumber != null ? holdingNumber.hashCode() : 0);
        result = 31 * result + (street != null ? street.hashCode() : 0);
        result = 31 * result + (areaMouja != null ? areaMouja.hashCode() : 0);
        result = 31 * result + (village != null ? village.hashCode() : 0);
        result = 31 * result + (postOffice != null ? postOffice.hashCode() : 0);
        result = 31 * result + (postCode != null ? postCode.hashCode() : 0);
        result = 31 * result + (ruralWardId != null ? ruralWardId.hashCode() : 0);
        result = 31 * result + (cityCorporationId != null ? cityCorporationId.hashCode() : 0);
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
        result = 31 * result + (locationLevel1 != null ? locationLevel1.hashCode() : 0);
        result = 31 * result + (locationLevel2 != null ? locationLevel2.hashCode() : 0);
        result = 31 * result + (locationLevel3 != null ? locationLevel3.hashCode() : 0);
        result = 31 * result + (locationLevel4 != null ? locationLevel4.hashCode() : 0);
        result = 31 * result + (locationLevel5 != null ? locationLevel5.hashCode() : 0);
        result = 31 * result + (locationLevel6 != null ? locationLevel6.hashCode() : 0);
        result = 31 * result + (permanentAddressLine != null ? permanentAddressLine.hashCode() : 0);
        result = 31 * result + (permanentDivisionId != null ? permanentDivisionId.hashCode() : 0);
        result = 31 * result + (permanentDistrictId != null ? permanentDistrictId.hashCode() : 0);
        result = 31 * result + (permanentUpazilaId != null ? permanentUpazilaId.hashCode() : 0);
        result = 31 * result + (permanentUnionOrUrbanWardId != null ? permanentUnionOrUrbanWardId.hashCode() : 0);
        result = 31 * result + (permanentHoldingNumber != null ? permanentHoldingNumber.hashCode() : 0);
        result = 31 * result + (permanentStreet != null ? permanentStreet.hashCode() : 0);
        result = 31 * result + (permanentAreaMouja != null ? permanentAreaMouja.hashCode() : 0);
        result = 31 * result + (permanentVillage != null ? permanentVillage.hashCode() : 0);
        result = 31 * result + (permanentPostOffice != null ? permanentPostOffice.hashCode() : 0);
        result = 31 * result + (permanentPostCode != null ? permanentPostCode.hashCode() : 0);
        result = 31 * result + (permanentRuralWardId != null ? permanentRuralWardId.hashCode() : 0);
        result = 31 * result + (permanentCityCorporationId != null ? permanentCityCorporationId.hashCode() : 0);
        result = 31 * result + (permanentCountryCode != null ? permanentCountryCode.hashCode() : 0);
        result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (dateOfDeath != null ? dateOfDeath.hashCode() : 0);
        result = 31 * result + (primaryContact != null ? primaryContact.hashCode() : 0);
        result = 31 * result + (primaryCellNo != null ? primaryCellNo.hashCode() : 0);
        result = 31 * result + (cellNo != null ? cellNo.hashCode() : 0);
        result = 31 * result + (phoneNumberCountryCode != null ? phoneNumberCountryCode.hashCode() : 0);
        result = 31 * result + (phoneNumberAreaCode != null ? phoneNumberAreaCode.hashCode() : 0);
        result = 31 * result + (phoneNumberExtension != null ? phoneNumberExtension.hashCode() : 0);
        result = 31 * result + (primaryContactNumberCountryCode != null ? primaryContactNumberCountryCode.hashCode() : 0);
        result = 31 * result + (primaryContactNumberAreaCode != null ? primaryContactNumberAreaCode.hashCode() : 0);
        result = 31 * result + (primaryContactNumberExtension != null ? primaryContactNumberExtension.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (updatedBy != null ? updatedBy.hashCode() : 0);
        result = 31 * result + (relations != null ? relations.hashCode() : 0);
        result = 31 * result + (lowerSurName != null ? lowerSurName.hashCode() : 0);
        result = 31 * result + (lowerGivenName != null ? lowerGivenName.hashCode() : 0);
        result = 31 * result + (pendingApprovals != null ? pendingApprovals.hashCode() : 0);
        result = 31 * result + (confidential != null ? confidential.hashCode() : 0);
        result = 31 * result + (householdCode != null ? householdCode.hashCode() : 0);
        return result;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = parseDate(dateOfBirth, ISO_DATE_FORMAT);
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getBirthRegistrationNumber() {
        return birthRegistrationNumber;
    }

    public void setBirthRegistrationNumber(String birthRegistrationNumber) {
        this.birthRegistrationNumber = birthRegistrationNumber;
    }

    public String getFullNameBangla() {
        return fullNameBangla;
    }

    public void setFullNameBangla(String fullNameBangla) {
        this.fullNameBangla = fullNameBangla;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFathersNameBangla() {
        return fathersNameBangla;
    }

    public void setFathersNameBangla(String fathersNameBangla) {
        this.fathersNameBangla = fathersNameBangla;
    }

    public String getFathersSurName() {
        return fathersSurName;
    }

    public void setFathersSurName(String fathersSurName) {
        this.fathersSurName = fathersSurName;
    }

    public String getFathersUid() {
        return fathersUid;
    }

    public void setFathersUid(String fathersUid) {
        this.fathersUid = fathersUid;
    }

    public String getFathersNid() {
        return fathersNid;
    }

    public void setFathersNid(String fathersNid) {
        this.fathersNid = fathersNid;
    }

    public String getFathersBrn() {
        return fathersBrn;
    }

    public void setFathersBrn(String fathersBrn) {
        this.fathersBrn = fathersBrn;
    }

    public String getMothersNameBangla() {
        return mothersNameBangla;
    }

    public void setMothersNameBangla(String mothersNameBangla) {
        this.mothersNameBangla = mothersNameBangla;
    }

    public String getmothersGivenName() {
        return mothersGivenName;
    }

    public void setMothersGivenName(String mothersGivenName) {
        this.mothersGivenName = mothersGivenName;
    }

    public String getMothersSurName() {
        return mothersSurName;
    }

    public void setMothersSurName(String mothersSurName) {
        this.mothersSurName = mothersSurName;
    }


    public String getMothersUid() {
        return mothersUid;
    }

    public void setMothersUid(String mothersUid) {
        this.mothersUid = mothersUid;
    }

    public String getMothersNid() {
        return mothersNid;
    }

    public void setMothersNid(String mothersNid) {
        this.mothersNid = mothersNid;
    }

    public String getMothersBrn() {
        return mothersBrn;
    }

    public void setMothersBrn(String mothersBrn) {
        this.mothersBrn = mothersBrn;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getDisability() {
        return disability;
    }

    public void setDisability(String disability) {
        this.disability = disability;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getFathersGivenName() {
        return fathersGivenName;
    }

    public void setFathersGivenName(String fathersGivenName) {
        this.fathersGivenName = fathersGivenName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(String divisionId) {
        this.divisionId = divisionId;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getUpazilaId() {
        return upazilaId;
    }

    public void setUpazilaId(String upazilaId) {
        this.upazilaId = upazilaId;
    }

    public String getUnionOrUrbanWardId() {
        return unionOrUrbanWardId;
    }

    public void setUnionOrUrbanWardId(String unionOrUrbanWardId) {
        this.unionOrUrbanWardId = unionOrUrbanWardId;
    }

    public String getHoldingNumber() {
        return holdingNumber;
    }

    public void setHoldingNumber(String holdingNumber) {
        this.holdingNumber = holdingNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getAreaMouja() {
        return areaMouja;
    }

    public void setAreaMouja(String areaMouja) {
        this.areaMouja = areaMouja;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getPostOffice() {
        return postOffice;
    }

    public void setPostOffice(String postOffice) {
        this.postOffice = postOffice;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getRuralWardId() {
        return ruralWardId;
    }

    public void setRuralWardId(String ruralWardId) {
        this.ruralWardId = ruralWardId;
    }

    public String getCityCorporationId() {
        return cityCorporationId;
    }

    public void setCityCorporationId(String cityCorporationId) {
        this.cityCorporationId = cityCorporationId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPermanentAddressLine() {
        return permanentAddressLine;
    }

    public void setPermanentAddressLine(String permanentAddressLine) {
        this.permanentAddressLine = permanentAddressLine;
    }

    public String getPermanentDivisionId() {
        return permanentDivisionId;
    }

    public void setPermanentDivisionId(String permanentDivisionId) {
        this.permanentDivisionId = permanentDivisionId;
    }

    public String getPermanentDistrictId() {
        return permanentDistrictId;
    }

    public void setPermanentDistrictId(String permanentDistrictId) {
        this.permanentDistrictId = permanentDistrictId;
    }

    public String getPermanentUpazilaId() {
        return permanentUpazilaId;
    }

    public void setPermanentUpazilaId(String permanentUpazilaId) {
        this.permanentUpazilaId = permanentUpazilaId;
    }

    public String getPermanentUnionOrUrbanWardId() {
        return permanentUnionOrUrbanWardId;
    }

    public void setPermanentUnionOrUrbanWardId(String permanentUnionOrUrbanWardId) {
        this.permanentUnionOrUrbanWardId = permanentUnionOrUrbanWardId;
    }

    public String getPermanentHoldingNumber() {
        return permanentHoldingNumber;
    }

    public void setPermanentHoldingNumber(String permanentHoldingNumber) {
        this.permanentHoldingNumber = permanentHoldingNumber;
    }

    public String getPermanentStreet() {
        return permanentStreet;
    }

    public void setPermanentStreet(String permanentStreet) {
        this.permanentStreet = permanentStreet;
    }

    public String getPermanentAreaMouja() {
        return permanentAreaMouja;
    }

    public void setPermanentAreaMouja(String permanentAreaMouja) {
        this.permanentAreaMouja = permanentAreaMouja;
    }

    public String getPermanentVillage() {
        return permanentVillage;
    }

    public void setPermanentVillage(String permanentVillage) {
        this.permanentVillage = permanentVillage;
    }

    public String getPermanentPostOffice() {
        return permanentPostOffice;
    }

    public void setPermanentPostOffice(String permanentPostOffice) {
        this.permanentPostOffice = permanentPostOffice;
    }

    public String getPermanentPostCode() {
        return permanentPostCode;
    }

    public void setPermanentPostCode(String permanentPostCode) {
        this.permanentPostCode = permanentPostCode;
    }

    public String getPermanentRuralWardId() {
        return permanentRuralWardId;
    }

    public void setPermanentRuralWardId(String permanentRuralWardId) {
        this.permanentRuralWardId = permanentRuralWardId;
    }

    public String getPermanentCityCorporationId() {
        return permanentCityCorporationId;
    }

    public void setPermanentCityCorporationId(String permanentCityCorporationId) {
        this.permanentCityCorporationId = permanentCityCorporationId;
    }

    public String getPermanentCountryCode() {
        return permanentCountryCode;
    }

    public void setPermanentCountryCode(String permanentCountryCode) {
        this.permanentCountryCode = defaultString(permanentCountryCode, COUNTRY_CODE_BANGLADESH);
    }

    public UUID getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(UUID createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(UUID updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRelations() {
        return relations;
    }

    public void setRelations(String relations) {
        this.relations = relations;
    }

    public String getPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(String primaryContact) {
        this.primaryContact = primaryContact;
    }

    public String getCellNo() {
        return cellNo;
    }

    public void setCellNo(String cellNo) {
        this.cellNo = cellNo;
    }

    public String getPrimaryCellNo() {
        return primaryCellNo;
    }

    public void setPrimaryCellNo(String primaryCellNo) {
        this.primaryCellNo = primaryCellNo;
    }

    public String getPhoneNumberCountryCode() {
        return phoneNumberCountryCode;
    }

    public void setPhoneNumberCountryCode(String phoneNumberCountryCode) {
        this.phoneNumberCountryCode = phoneNumberCountryCode;
    }

    public String getPhoneNumberAreaCode() {
        return phoneNumberAreaCode;
    }

    public void setPhoneNumberAreaCode(String phoneNumberAreaCode) {
        this.phoneNumberAreaCode = phoneNumberAreaCode;
    }

    public String getPhoneNumberExtension() {
        return phoneNumberExtension;
    }

    public void setPhoneNumberExtension(String phoneNumberExtension) {
        this.phoneNumberExtension = phoneNumberExtension;
    }

    public String getPrimaryContactNumberCountryCode() {
        return primaryContactNumberCountryCode;
    }

    public void setPrimaryContactNumberCountryCode(String primaryContactNumberCountryCode) {
        this.primaryContactNumberCountryCode = primaryContactNumberCountryCode;
    }

    public String getPrimaryContactNumberAreaCode() {
        return primaryContactNumberAreaCode;
    }

    public void setPrimaryContactNumberAreaCode(String primaryContactNumberAreaCode) {
        this.primaryContactNumberAreaCode = primaryContactNumberAreaCode;
    }

    public String getPrimaryContactNumberExtension() {
        return primaryContactNumberExtension;
    }

    public void setPrimaryContactNumberExtension(String primaryContactNumberExtension) {
        this.primaryContactNumberExtension = primaryContactNumberExtension;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public TreeSet<PendingApproval> getPendingApprovals() {
        if (isBlank(this.pendingApprovals)) {
            return null;
        }
        return readValue(this.pendingApprovals, new TypeReference<TreeSet<PendingApproval>>() {
        });
    }

    public void setPendingApprovals(TreeSet<PendingApproval> pendingApprovals) {
        if (pendingApprovals != null) {
            this.pendingApprovals = writeValueAsString(pendingApprovals);
        }
    }

    public void addPendingApprovals(TreeSet<PendingApproval> pendingApprovalsToAdd) {
        TreeSet<PendingApproval> pendingApprovals = this.getPendingApprovals();
        if (pendingApprovals == null) {
            pendingApprovals = new TreeSet<>();
        }

        for (PendingApproval pendingApproval : pendingApprovalsToAdd) {
            if (!pendingApprovals.contains(pendingApproval)) {
                pendingApprovals.add(pendingApproval);

            } else {
                for (PendingApproval p : pendingApprovals) {
                    if (p.equals(pendingApproval)) {
                        p.setFieldDetails(pendingApproval.getFieldDetails());
                    }
                }
            }
        }

        this.setPendingApprovals(pendingApprovals);
    }

    public Date getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(Date dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public void setDateOfDeath(String dateOfDeath) {
        if (dateOfDeath == null) {
            this.dateOfDeath = null;
            return;
        }

        if ("".equals(dateOfDeath)) {
            dateOfDeath = EMPTY_DATE_VALUE;
        }
        this.dateOfDeath = parseDate(dateOfDeath, ISO_DATE_FORMAT);
    }

    public Boolean getConfidential() {
        return confidential;
    }

    public void setConfidential(Boolean confidential) {
        this.confidential = confidential;
    }

    @Transient
    public Catchment getCatchment() {
        String divisionId = this.getDivisionId();
        String districtId = this.getDistrictId();
        if (isBlank(divisionId) || isBlank(districtId)) {
            return null;
        }
        return new Catchment(divisionId, districtId, this.getUpazilaId(),
                this.getCityCorporationId(), this.getUnionOrUrbanWardId(), this.getRuralWardId());
    }

    public String getHouseholdCode() {
        return householdCode;
    }

    public void setHouseholdCode(String householdCode) {
        this.householdCode = householdCode;
    }
}
