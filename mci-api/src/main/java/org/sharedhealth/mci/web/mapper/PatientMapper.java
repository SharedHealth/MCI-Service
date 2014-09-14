package org.sharedhealth.mci.web.mapper;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.sharedhealth.mci.validation.constraints.Date;
import org.sharedhealth.mci.validation.constraints.Length;
import org.sharedhealth.mci.validation.constraints.Location;
import org.sharedhealth.mci.validation.constraints.Occupation;
import org.sharedhealth.mci.validation.constraints.MaritalRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@MaritalRelation.List({
        @MaritalRelation(maritalStatus = "maritalStatus", relationalStatus = "relations", message = "2004")
})
public class PatientMapper {

    private static final Logger logger = LoggerFactory.getLogger(PatientMapper.class);
    @JsonProperty("hid")
    private String healthId;

    @JsonProperty("nid")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{13}|[\\d]{17}", message = "1002")
    private String nationalId;

    @JsonProperty("bin_brn")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{17}", message = "1002")
    private String birthRegistrationNumber;

    @JsonProperty("name_bangla")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 125, message = "1002")
    private String nameBangla;

    @JsonProperty("given_name")
    @NotBlank(message = "1001")
    @Length(lengthSize= 100, message = "1002")
    private String givenName;

    @JsonProperty("sur_name")
    @NotBlank(message = "1001")
    @Length(lengthSize= 25, message = "1002")
    private String surName;

    @JsonProperty("date_of_birth")
    @NotBlank(message = "1001")
    @Date(format = "yyyy-MM-dd", message = "1002")
    private String dateOfBirth;

    @JsonProperty("gender")
    @NotBlank(message = "1001")
    @Pattern(regexp = "[M|F|O]{1}", message = "1004")
    private String gender;

    @JsonProperty("occupation")
    @JsonInclude(NON_EMPTY)
    @Occupation(message = "1004")
    private String occupation;

    @JsonProperty("edu_level")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-1]{1}[0-9]{1}", message = "1004")
    private String educationLevel;

    @JsonProperty("relations")
    @JsonInclude(NON_EMPTY)
    @Valid
    private List<Relation> relations;

    @JsonProperty("uid")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{11}", message = "1002")
    private String uid;

    @JsonInclude(NON_EMPTY)
    @JsonProperty("place_of_birth")
    @Pattern(regexp = "^[a-zA-Z0-9]{0,20}$", message = "1002")
    private String placeOfBirth;

    @JsonProperty("religion")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[1|2|3|4|8|9|0]{1}", message = "1004")
    private String religion;

    @JsonProperty("blood_group")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[1-8]{1}", message = "1004")
    private String bloodGroup;

    @JsonProperty("nationality")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 50, message = "1002")
    private String nationality;

    @JsonProperty("disability")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-5]{1}", message = "1004")
    private String disability;

    @JsonProperty("ethnicity")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-9]{2}", message = "1004")
    private String ethnicity;

    @JsonProperty("present_address")
    @Valid
    @Location(message = "1002")
    private Address address;

    @JsonProperty("primary_contact")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 100, message = "1002")
    private String primaryContact;

    @JsonProperty("cell_no")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 20, message = "1002")
    @Pattern(regexp = "^[-)(+0-9 ]*$", message = "1002")
    private String cellNo;

    @JsonProperty("primary_cell_no")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 20, message = "1002")
    @Pattern(regexp = "^[-)(+0-9 ]*$", message = "1002")
    private String primaryCellNo;

    @JsonProperty("permanent_address")
    @JsonInclude(NON_EMPTY)
    @Location(message = "1004")
    private Address permanentAddress;

    @JsonProperty("marital_status")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[1|2]{1}", message = "1004")
    private String maritalStatus;

    @JsonProperty("full_name")
    @JsonInclude(NON_EMPTY)
    private String fullName;

    @JsonProperty("is_alive")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0|1]{1}", message = "1004")
    private String isAlive;

    @JsonProperty("created_at")
    @JsonInclude(NON_EMPTY)
    private java.util.Date createdAt;

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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
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

    public String getNameBangla() {
        return nameBangla;
    }

    public void setNameBangla(String nameBangla) {
        this.nameBangla = nameBangla;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
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

    public Address getPermanentAddress() {
        return permanentAddress;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPermanentAddress(Address permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getIsAlive() {
        return isAlive;
    }

    public void setIsAlive(String isAlive) {
        this.isAlive = isAlive;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public Relation getRelation(String relationType) {

        if(this.relations == null) {
            return null;
        }

        for (Relation relation : this.relations) {

            if (relation.getType() != null && relation.getType().equals(relationType)) {
                return relation;
            }
        }

        return null;
    }

    public boolean isSimilarTo(PatientMapper patientMapper) {
        int matches = 0;

        if(this.getNationalId() != null && this.getNationalId().equals(patientMapper.getNationalId())) {
            matches++;
        }

        if(this.getBirthRegistrationNumber() != null && this.getBirthRegistrationNumber().equals(patientMapper.getBirthRegistrationNumber())) {
            matches++;
        }

        if(this.getUid() != null && this.getUid().equals(patientMapper.getUid())) {
            matches++;
        }

        return matches > 1;
    }

    public void setRelations(List<Relation> relations) {
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

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public java.util.Date getCreatedAt() {
        return this.createdAt;
    }

    @JsonInclude(NON_EMPTY)
    public String getCreated() {
        if(this.createdAt == null){
            return null;
        }
        return DateFormatUtils.ISO_DATETIME_FORMAT.format(this.createdAt);
    }

    public void setCreatedAt(java.util.Date createdAt) {
        this.createdAt = createdAt;
    }
}
