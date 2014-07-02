package org.sharedhealth.mci.web.model;


import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;
import org.sharedhealth.mci.validation.constraints.Date;

public class Patient {

    @JsonProperty("hid")
    private String healthId;

    @JsonProperty("nid")
    @Pattern(regexp = "[\\d]{13}|[\\d]{17}")
    private String nationalId;

    @JsonProperty("bin_brn")
    @Pattern(regexp = "[\\d]{17}")
    private String birthRegistrationNumber;

    @JsonProperty("uid")
    @Pattern(regexp = "[a-zA-Z0-9]{11}")
    private String uid;

    @JsonProperty("full_name_bangla")
    private String fullNameBangla;

    @JsonProperty("first_name")
    @NotBlank
    private String firstName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("last_name")
    @NotBlank
    private String lastName;

    @JsonProperty("fathers_name_bangla")
    private String fathersNameBangla;

    @JsonProperty("fathers_first_name")
    private String fathersFirstName;

    @JsonProperty("fathers_middle_name")
    private String fathersMiddleName;

    @JsonProperty("fathers_last_name")
    private String fathersLastName;

    @JsonProperty("fathers_uid")
    private String fathersUid;

    @JsonProperty("fathers_nid")
    private String fathersNid;

    @JsonProperty("fathers_brn")
    private String fatherBrn;

    @JsonProperty("mothers_name_bangla")
    private String mothersNameBangla;

    @JsonProperty("mothers_first_name")
    private String mothersFirstName;

    @JsonProperty("mothers_middle_name")
    private String mothersMiddleName;

    @JsonProperty("mothers_last_name")
    private String mothersLastName;

    @JsonProperty("mothers_uid")
    private String mothersUid;

    @JsonProperty("mothers_nid")
    private String mothersNid;

    @JsonProperty("mothers_brn")
    private String mothersBrn;

    @JsonProperty("place_of_birth")
    private String placeOfBirth;

    @JsonProperty("marital_status")
    private String maritalStatus;

    @JsonProperty("marriage_id")
    private String marriageId;

    @JsonProperty("spouse_name_bangla")
    private String spouseNameBangla;

    @JsonProperty("spouse_name")
    private String spouseName;

    @JsonProperty("spouse_uid_nid")
    private String spouseUidNid;

    @JsonProperty("religion")
    private String religion;

    @JsonProperty("blood_group")
    private String bloodGroup;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("disability")
    private String disability;

    @JsonProperty("ethnicity")
    private String ethnicity;


    @JsonProperty("date_of_birth")
    @NotBlank
    @Date(format = "yyyy-MM-dd")
    private String dateOfBirth;

    @JsonProperty("gender")
    @NotBlank
    @Pattern(regexp = "[1-3]{1}")
    private String gender;

    @JsonProperty("occupation")
    private String occupation;

    @JsonProperty("edu_level")
    private String educationLevel;

    @JsonProperty("primary_contact")
    private String primaryContact;

    @JsonProperty("present_address")
    @Valid
    private Address address;

    @JsonProperty("permanent_address")
    private Address permanentAddress;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;

        Patient patient = (Patient) o;

        if (address != null ? !address.equals(patient.address) : patient.address != null) return false;
        if (dateOfBirth != null ? !dateOfBirth.equals(patient.dateOfBirth) : patient.dateOfBirth != null) return false;
        if (educationLevel != null ? !educationLevel.equals(patient.educationLevel) : patient.educationLevel != null)
            return false;
        if (firstName != null ? !firstName.equals(patient.firstName) : patient.firstName != null) return false;
        if (gender != null ? !gender.equals(patient.gender) : patient.gender != null) return false;
        if (healthId != null ? !healthId.equals(patient.healthId) : patient.healthId != null) return false;
        if (lastName != null ? !lastName.equals(patient.lastName) : patient.lastName != null) return false;
        if (middleName != null ? !middleName.equals(patient.middleName) : patient.middleName != null) return false;
        if (nationalId != null ? !nationalId.equals(patient.nationalId) : patient.nationalId != null) return false;
        if (occupation != null ? !occupation.equals(patient.occupation) : patient.occupation != null) return false;
        if (primaryContact != null ? !primaryContact.equals(patient.primaryContact) : patient.primaryContact != null)
            return false;

        return true;
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

    public String getPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(String primaryContact) {
        this.primaryContact = primaryContact;
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

    public String getFathersMiddleName() {
        return fathersMiddleName;
    }

    public void setFathersMiddleName(String fathersMiddleName) {
        this.fathersMiddleName = fathersMiddleName;
    }

    public String getFathersLastName() {
        return fathersLastName;
    }

    public void setFathersLastName(String fathersLastName) {
        this.fathersLastName = fathersLastName;
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

    public String getFatherBrn() {
        return fatherBrn;
    }

    public void setFatherBrn(String fatherBrn) {
        this.fatherBrn = fatherBrn;
    }

    public String getMothersNameBangla() {
        return mothersNameBangla;
    }

    public void setMothersNameBangla(String mothersNameBangla) {
        this.mothersNameBangla = mothersNameBangla;
    }

    public String getMothersFirstName() {
        return mothersFirstName;
    }

    public void setMothersFirstName(String mothersFirstName) {
        this.mothersFirstName = mothersFirstName;
    }

    public String getMothersMiddleName() {
        return mothersMiddleName;
    }

    public void setMothersMiddleName(String mothersMiddleName) {
        this.mothersMiddleName = mothersMiddleName;
    }

    public String getMothersLastName() {
        return mothersLastName;
    }

    public void setMothersLastName(String mothersLastName) {
        this.mothersLastName = mothersLastName;
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

    public String getMarriageId() {
        return marriageId;
    }

    public void setMarriageId(String marriageId) {
        this.marriageId = marriageId;
    }

    public String getSpouseNameBangla() {
        return spouseNameBangla;
    }

    public void setSpouseNameBangla(String spouseNameBangla) {
        this.spouseNameBangla = spouseNameBangla;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public String getSpouseUidNid() {
        return spouseUidNid;
    }

    public void setSpouseUidNid(String spouseUidNid) {
        this.spouseUidNid = spouseUidNid;
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
    public String getFathersFirstName() {
        return fathersFirstName;
    }

    public void setFathersFirstName(String fathersFirstName) {
        this.fathersFirstName = fathersFirstName;
    }

    public Address getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(Address permanentAddress) {
        this.permanentAddress = permanentAddress;
    }
}
