package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.sharedhealth.mci.utils.DateStringDeserializer;

import javax.validation.constraints.Pattern;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_PATTERN;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientSummaryData {

    @JsonProperty(HID)
    private String healthId;

    @JsonProperty(NID)
    @JsonInclude(NON_EMPTY)
    private String nationalId;

    @JsonProperty(UID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{11}", message = ERROR_CODE_PATTERN)
    private String uid;

    @JsonProperty(BIN_BRN)
    @JsonInclude(NON_EMPTY)
    private String birthRegistrationNumber;

    @JsonProperty(GIVEN_NAME)
    @JsonInclude(NON_EMPTY)
    private String givenName;

    @JsonProperty(SUR_NAME)
    @JsonInclude(NON_EMPTY)
    private String surName;

    @JsonProperty(GENDER)
    @JsonInclude(NON_EMPTY)
    private String gender;

    @JsonProperty(DATE_OF_BIRTH)
    @JsonInclude(NON_EMPTY)
    @JsonDeserialize(using = DateStringDeserializer.class)
    private String dateOfBirth;

    @JsonProperty(PRESENT_ADDRESS)
    @JsonInclude(NON_EMPTY)
    private Address address;

    @JsonProperty(PHONE_NUMBER)
    @JsonInclude(NON_EMPTY)
    private PhoneNumber phoneNumber;

    @JsonProperty(ACTIVE)
    @JsonInclude(NON_EMPTY)
    private Boolean active;

    @JsonProperty(MERGED_WITH)
    @JsonInclude(NON_EMPTY)
    private String mergedWith;

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
        this.dateOfBirth = toIsoFormat(dateOfBirth);
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

    public String getBirthRegistrationNumber() {
        return birthRegistrationNumber;
    }

    public void setBirthRegistrationNumber(String birthRegistrationNumber) {
        this.birthRegistrationNumber = birthRegistrationNumber;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getMergedWith() {
        return mergedWith;
    }

    public void setMergedWith(String mergedWith) {
        this.mergedWith = mergedWith;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatientSummaryData)) return false;

        PatientSummaryData that = (PatientSummaryData) o;

        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (birthRegistrationNumber != null ? !birthRegistrationNumber.equals(that.birthRegistrationNumber) : that.birthRegistrationNumber != null)
            return false;
        if (dateOfBirth != null ? !dateOfBirth.equals(that.dateOfBirth) : that.dateOfBirth != null) return false;
        if (gender != null ? !gender.equals(that.gender) : that.gender != null) return false;
        if (givenName != null ? !givenName.equals(that.givenName) : that.givenName != null) return false;
        if (healthId != null ? !healthId.equals(that.healthId) : that.healthId != null) return false;
        if (nationalId != null ? !nationalId.equals(that.nationalId) : that.nationalId != null) return false;
        if (phoneNumber != null ? !phoneNumber.equals(that.phoneNumber) : that.phoneNumber != null) return false;
        if (surName != null ? !surName.equals(that.surName) : that.surName != null) return false;
        if (uid != null ? !uid.equals(that.uid) : that.uid != null) return false;
        if (active != null ? !active.equals(that.active) : that.active != null) return false;
        if (mergedWith != null ? !mergedWith.equals(that.mergedWith) : that.mergedWith != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = healthId != null ? healthId.hashCode() : 0;
        result = 31 * result + (nationalId != null ? nationalId.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (birthRegistrationNumber != null ? birthRegistrationNumber.hashCode() : 0);
        result = 31 * result + (givenName != null ? givenName.hashCode() : 0);
        result = 31 * result + (surName != null ? surName.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (mergedWith != null ? mergedWith.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PatientSummaryData{" +
                "healthId='" + healthId + '\'' +
                ", nationalId='" + nationalId + '\'' +
                ", uid='" + uid + '\'' +
                ", birthRegistrationNumber='" + birthRegistrationNumber + '\'' +
                ", givenName='" + givenName + '\'' +
                ", surName='" + surName + '\'' +
                ", gender='" + gender + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", address=" + address +
                ", phoneNumber=" + phoneNumber +
                ", active=" + active +
                ",mergedWith=" + mergedWith +
                '}';
    }
}
