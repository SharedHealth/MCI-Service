package org.sharedhealth.mci.web.model;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.sharedhealth.mci.validation.constraints.Date;
import org.sharedhealth.mci.validation.constraints.Length;
import org.sharedhealth.mci.validation.constraints.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public class Patient {

    private static final Logger logger = LoggerFactory.getLogger(Patient.class);
    @JsonProperty("hid")
    private String healthId;

    @JsonProperty("nid")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{13}|[\\d]{17}", message = "1001")
    private String nationalId;

    @JsonProperty("bin_brn")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{17}", message = "1002")
    private String birthRegistrationNumber;

    @JsonProperty("full_name_bangla")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 120, message = "1003")
    private String fullNameBangla;

    @JsonProperty("first_name")
    @NotBlank(message = "1004")
    @Length(lengthSize= 25, message = "1036")
    private String firstName;

    @JsonProperty("middle_name")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 25, message = "1005")
    private String middleName;

    @JsonProperty("last_name")
    @NotBlank(message = "1006")
    @Length(lengthSize= 25, message = "1037")
    private String lastName;

    @JsonProperty("date_of_birth")
    @NotBlank(message = "1007")
    @Date(format = "yyyy-MM-dd", message = "1008")
    private String dateOfBirth;

    @JsonProperty("gender")
    @NotBlank(message = "1009")
    @Pattern(regexp = "[1-3]{1}", message = "1010")
    private String gender;

    @JsonProperty("occupation")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[1-8]{1}[\\d]{1}|0[1-9]{1}|9[0-2]{1}", message = "1011")
    private String occupation;

    @JsonProperty("edu_level")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-1]{1}[0-9]{1}", message = "1012")
    private String educationLevel;

    @JsonProperty("relations")
    @JsonInclude(NON_EMPTY)
    private List<Relation> relations;

    @JsonProperty("uid")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{11}", message = "1027")
    private String uid;

    @JsonInclude(NON_EMPTY)
    @JsonProperty("place_of_birth")
    @Pattern(regexp = "^[a-zA-Z]{0,7}$", message = "1038")
    private String placeOfBirth;

    @JsonProperty("religion")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[1-7]{1}", message = "1031")
    private String religion;

    @JsonProperty("blood_group")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[1-8]{1}", message = "1032")
    private String bloodGroup;

    @JsonProperty("nationality")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 50, message = "1033")
    private String nationality;

    @JsonProperty("disability")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-5]{1}", message = "1034")
    private String disability;

    @JsonProperty("ethnicity")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-9]{2}", message = "1035")
    private String ethnicity;

    @JsonProperty("present_address")
    @Valid
    @Location(message = "1036")
    private Address address;


    @JsonProperty("permanent_address")
    @JsonInclude(NON_EMPTY)
    @Location(message = "1037")
    private Address permanentAddress;

    @JsonProperty("full_name")
    @JsonInclude(NON_EMPTY)
    private String fullName;

    @JsonProperty("is_alive")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[1-2]{1}", message = "1041")
    private String isAlive;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public Relation getRelation(String relationType){

            for (Relation relation : this.relations) {

                if (relation.getType() != null && relation.getType().equals(relationType)) {
                    return relation;
                }
            }

        return null;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }
}
