package org.mci.web.model;


import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;
import org.mci.validation.constraints.Date;

public class Patient {

    @JsonProperty("nid")
    private String nationalId;

    @JsonProperty("hid")
    private String healthId;

    @JsonProperty("first_name")
    @NotBlank
    private String firstName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("last_name")
    @NotBlank
    private String lastName;

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
}
