package org.sharedhealth.mci.web.model;


import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.sharedhealth.mci.utils.DateUtil.string2Date;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;

@Table(value = "patient")
public class Patient {

    private static final String BD_COUNTRY_CODE = "050";

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

    @Column(UPAZILLA_ID)
    private String upazillaId;

    @Column(UNION_ID)
    private String unionId;

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

    @Column(WARD)
    private String wardId;

    @Column(THANA)
    private String thanaId;

    @Column(CITY_CORPORATION)
    private String cityCorporationId;

    @Column(COUNTRY)
    private String countryCode;

    @Column("location_level1")
    private String locationLevel1;

    @Column("location_level2")
    private String locationLevel2;

    @Column("location_level3")
    private String locationLevel3;

    @Column("location_level4")
    private String locationLevel4;

    @Column("location_level5")
    private String locationLevel5;

    @Column(PERMANENT_ADDRESS_LINE)
    private String permanentAddressLine;

    @Column(PERMANENT_DIVISION_ID)
    private String permanentDivisionId;

    @Column(PERMANENT_DISTRICT_ID)
    private String permanentDistrictId;

    @Column(PERMANENT_UPAZILLA_ID)
    private String permanentUpazillaId;

    @Column(PERMANENT_UNION_ID)
    private String permanentUnionId;

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

    @Column(PERMANENT_WARD)
    private String permanentWardId;

    @Column(PERMANENT_THANA)
    private String permanentThanaId;

    @Column(PERMANENT_CITY_CORPORATION)
    private String permanentCityCorporationId;

    @Column(PERMANENT_COUNTRY)
    private String permanentCountryCode;

    @Column(FULL_NAME)
    private String fullName;

    @Column(IS_ALIVE)
    private String isAlive;

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

    @Column("created_at")
    private Date createdAt;

    @Column("updated_at")
    private Date updatedAt;

    @Column("created_by")
    private String createdBy;

    @Column("updated_by")
    private String updatedBy;

    @Column(RELATIONS)
    private String relations;

    @Column("lower_sur_name")
    private String lowerSurName;

    @Column("lower_given_name")
    private String lowerGivenName;

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
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
        this.dateOfBirth = string2Date(dateOfBirth, "yyyy-MM-dd");
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

    public String getIsAlive() {
        return isAlive;
    }

    public void setIsAlive(String isAlive) {
        this.isAlive = isAlive;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = defaultString(addressLine);
    }

    public String getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(String divisionId) {
        this.divisionId = defaultString(divisionId);
        this.updateLocationLevels();
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = defaultString(districtId);
        this.updateLocationLevels();
    }

    public String getUpazillaId() {
        return upazillaId;
    }

    public void setUpazillaId(String upazillaId) {
        this.upazillaId = defaultString(upazillaId);
        this.updateLocationLevels();
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = defaultString(unionId);
        this.updateLocationLevels();
    }

    public String getHoldingNumber() {
        return holdingNumber;
    }

    public void setHoldingNumber(String holdingNumber) {
        this.holdingNumber = defaultString(holdingNumber);
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = defaultString(street);
    }

    public String getAreaMouja() {
        return areaMouja;
    }

    public void setAreaMouja(String areaMouja) {
        this.areaMouja = defaultString(areaMouja);
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = defaultString(village);
    }

    public String getPostOffice() {
        return postOffice;
    }

    public void setPostOffice(String postOffice) {
        this.postOffice = defaultString(postOffice);
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = defaultString(postCode);
    }

    public String getWardId() {
        return wardId;
    }

    public void setWardId(String wardId) {
        this.wardId = defaultString(wardId);
        this.updateLocationLevels();
    }

    public String getThanaId() {
        return thanaId;
    }

    public void setThanaId(String thanaId) {
        this.thanaId = defaultString(thanaId);
        this.updateLocationLevels();
    }

    public String getCityCorporationId() {
        return cityCorporationId;
    }

    public void setCityCorporationId(String cityCorporationId) {
        this.cityCorporationId = defaultString(cityCorporationId);
        this.updateLocationLevels();
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = defaultString(countryCode);
    }

    public String getPermanentAddressLine() {
        return permanentAddressLine;
    }

    public void setPermanentAddressLine(String permanentAddressLine) {
        this.permanentAddressLine = defaultString(permanentAddressLine);
    }

    public String getPermanentDivisionId() {
        return permanentDivisionId;
    }

    public void setPermanentDivisionId(String permanentDivisionId) {
        this.permanentDivisionId = defaultString(permanentDivisionId);
    }

    public String getPermanentDistrictId() {
        return permanentDistrictId;
    }

    public void setPermanentDistrictId(String permanentDistrictId) {
        this.permanentDistrictId = defaultString(permanentDistrictId);
    }

    public String getPermanentUpazillaId() {
        return permanentUpazillaId;
    }

    public void setPermanentUpazillaId(String permanentUpazillaId) {
        this.permanentUpazillaId = defaultString(permanentUpazillaId);
    }

    public String getPermanentUnionId() {
        return permanentUnionId;
    }

    public void setPermanentUnionId(String permanentUnionId) {
        this.permanentUnionId = defaultString(permanentUnionId);
    }

    public String getPermanentHoldingNumber() {
        return permanentHoldingNumber;
    }

    public void setPermanentHoldingNumber(String permanentHoldingNumber) {
        this.permanentHoldingNumber = defaultString(permanentHoldingNumber);
    }

    public String getPermanentStreet() {
        return permanentStreet;
    }

    public void setPermanentStreet(String permanentStreet) {
        this.permanentStreet = defaultString(permanentStreet);
    }

    public String getPermanentAreaMouja() {
        return permanentAreaMouja;
    }

    public void setPermanentAreaMouja(String permanentAreaMouja) {
        this.permanentAreaMouja = defaultString(permanentAreaMouja);
    }

    public String getPermanentVillage() {
        return permanentVillage;
    }

    public void setPermanentVillage(String permanentVillage) {
        this.permanentVillage = defaultString(permanentVillage);
    }

    public String getPermanentPostOffice() {
        return permanentPostOffice;
    }

    public void setPermanentPostOffice(String permanentPostOffice) {
        this.permanentPostOffice = defaultString(permanentPostOffice);
    }

    public String getPermanentPostCode() {
        return permanentPostCode;
    }

    public void setPermanentPostCode(String permanentPostCode) {
        this.permanentPostCode = defaultString(permanentPostCode);
    }

    public String getPermanentWardId() {
        return permanentWardId;
    }

    public void setPermanentWardId(String permanentWardId) {
        this.permanentWardId = defaultString(permanentWardId);
    }

    public String getPermanentThanaId() {
        return permanentThanaId;
    }

    public void setPermanentThanaId(String permanentThanaId) {
        this.permanentThanaId = defaultString(permanentThanaId);
    }

    public String getPermanentCityCorporationId() {
        return permanentCityCorporationId;
    }

    public void setPermanentCityCorporationId(String permanentCityCorporationId) {
        this.permanentCityCorporationId = defaultString(permanentCityCorporationId);
    }

    public String getPermanentCountryCode() {
        return permanentCountryCode;
    }

    public void setPermanentCountryCode(String permanentCountryCode) {
        this.permanentCountryCode = defaultString(permanentCountryCode, BD_COUNTRY_CODE);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
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
        this.phoneNumberCountryCode = defaultString(phoneNumberCountryCode);
    }

    public String getPhoneNumberAreaCode() {
        return phoneNumberAreaCode;
    }

    public void setPhoneNumberAreaCode(String phoneNumberAreaCode) {
        this.phoneNumberAreaCode = defaultString(phoneNumberAreaCode);
    }

    public String getPhoneNumberExtension() {
        return phoneNumberExtension;
    }

    public void setPhoneNumberExtension(String phoneNumberExtension) {
        this.phoneNumberExtension = defaultString(phoneNumberExtension);
    }

    public String getPrimaryContactNumberCountryCode() {
        return primaryContactNumberCountryCode;
    }

    public void setPrimaryContactNumberCountryCode(String primaryContactNumberCountryCode) {
        this.primaryContactNumberCountryCode = defaultString(primaryContactNumberCountryCode);
    }

    public String getPrimaryContactNumberAreaCode() {
        return primaryContactNumberAreaCode;
    }

    public void setPrimaryContactNumberAreaCode(String primaryContactNumberAreaCode) {
        this.primaryContactNumberAreaCode = defaultString(primaryContactNumberAreaCode);
    }

    public String getPrimaryContactNumberExtension() {
        return primaryContactNumberExtension;
    }

    public void setPrimaryContactNumberExtension(String primaryContactNumberExtension) {
        this.primaryContactNumberExtension = defaultString(primaryContactNumberExtension);
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

    public void updateLocationLevels() {
        this.locationLevel1 = this.getLocationLevel1();
        this.locationLevel2 = this.getLocationLevel2();
        this.locationLevel3 = this.getLocationLevel3();
        this.locationLevel4 = this.getLocationLevel4();
        this.locationLevel5 = this.getLocationLevel5();
    }

    public String getLocationLevel1() {

        if (StringUtils.isBlank(this.getDivisionId())) {
            return "";
        }

        return this.getDivisionId();
    }

    public String getLocationLevel2() {

        if (StringUtils.isBlank(this.getDistrictId())) {
            return "";
        }

        return this.getLocationLevel1() + this.getDistrictId();
    }

    public String getLocationLevel3() {
        String ut = "";

        if(this.getUpazillaId() != null) {
            ut = this.getUpazillaId();
        }

        if(this.getThanaId() != null) {
            ut = ut + this.getThanaId();
        }

        if (StringUtils.isBlank(ut)) {
            return "";
        }

        return this.getLocationLevel2() + ut;
    }

    public String getLocationLevel4() {
        if (StringUtils.isBlank(this.getCityCorporationId())) {
            return "";
        }

        return this.getLocationLevel3() + this.getCityCorporationId();
    }

    public String getLocationLevel5() {
        String uw = "";

        if(this.getUnionId() != null) {
            uw = this.getUnionId();
        }

        if(this.getWardId() != null) {
            uw = uw + this.getWardId();
        }

        if (StringUtils.isBlank(uw)) {
            return "";
        }

        return this.getLocationLevel4() + uw;
    }

    public String getLowerSurName() {
        return lowerSurName;
    }

    public void setLowerSurName(String lowerSurName) {
        this.lowerSurName = lowerSurName;
    }

    public String getLowerGivenName() {
        return lowerGivenName;
    }

    public void setLowerGivenName(String lowerGivenName) {
        this.lowerGivenName = lowerGivenName;
    }
}
