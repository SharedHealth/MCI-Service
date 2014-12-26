package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.sharedhealth.mci.validation.constraints.*;
import org.sharedhealth.mci.validation.group.RequiredGroup;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

@MaritalRelation(message = "1005", field = "maritalStatus")
@PatientStatus(message = "1005")
@JsonIgnoreProperties({"created_at"})
public class PatientData {

    @JsonProperty(HID)
    private String healthId;

    @JsonProperty(NID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{13}|[\\d]{17}", message = "1002")
    private String nationalId;

    @JsonProperty(BIN_BRN)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{17}", message = "1002")
    private String birthRegistrationNumber;

    @JsonProperty(NAME_BANGLA)
    @JsonInclude(NON_EMPTY)
    @Length(max = 125, message = "1002")
    private String nameBangla;

    @JsonProperty(GIVEN_NAME)
    @NotNull(message = "1001", groups = RequiredGroup.class)
    @Length(max = 100, min = 1, message = "1002")
    private String givenName;

    @JsonProperty(SUR_NAME)
    @NotNull(message = "1001", groups = RequiredGroup.class)
    @Pattern(regexp = "^(\\s*)([A-Za-z0-9]{1,25})(\\b\\s*$)", message = "1002")
    private String surName;

    @JsonProperty(DATE_OF_BIRTH)
    @NotNull(message = "1001", groups = RequiredGroup.class)
    @Date(format = "yyyy-MM-dd", message = "1002")
    @Length(min = 1, max = 10, message = "1002")
    private String dateOfBirth;

    @JsonProperty(GENDER)
    @NotBlank(message = "1001", groups = RequiredGroup.class)
    @Code(type = GENDER, regexp = "[A-Z]{1}", message = "1004")
    private String gender;

    @JsonProperty(OCCUPATION)
    @JsonInclude(NON_EMPTY)
    @Code(type = OCCUPATION, regexp = "[\\d]{2}", message = "1004")
    private String occupation;

    @JsonProperty(EDU_LEVEL)
    @JsonInclude(NON_EMPTY)
    @Code(type = "education_level", regexp = "[\\d]{2}", message = "1004")
    private String educationLevel;

    @JsonProperty(RELATIONS)
    @JsonInclude(NON_EMPTY)
    @Valid
    private List<Relation> relations;

    @JsonProperty(UID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{11}", message = "1002")
    private String uid;

    @JsonInclude(NON_EMPTY)
    @JsonProperty(PLACE_OF_BIRTH)
    @Pattern(regexp = "^[a-zA-Z0-9]{0,20}$", message = "1002")
    private String placeOfBirth;

    @JsonProperty(RELIGION)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[1|2|3|4|8|9|0]{1}", message = "1004")
    private String religion;

    @JsonProperty(BLOOD_GROUP)
    @JsonInclude(NON_EMPTY)
    @Code(type = "blood_group", regexp = "[\\d]{1}", message = "1004")
    private String bloodGroup;

    @JsonProperty(NATIONALITY)
    @JsonInclude(NON_EMPTY)
    @Length(max = 50, message = "1002")
    private String nationality;

    @JsonProperty(DISABILITY)
    @JsonInclude(NON_EMPTY)
    @Code(type = DISABILITY, regexp = "[\\d]{1}", message = "1004")
    private String disability;

    @JsonProperty(ETHNICITY)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-9]{2}", message = "1004")
    private String ethnicity;

    @JsonProperty(PRESENT_ADDRESS)
    @NotNull(message = "1001", groups = RequiredGroup.class)
    @Valid
    @Location(message = "1004", country_code = "050")
    private Address address;

    @JsonProperty(PRIMARY_CONTACT)
    @JsonInclude(NON_EMPTY)
    @Length(max = 100, message = "1002")
    private String primaryContact;

    @JsonProperty(PHONE_NUMBER)
    @Valid
    @JsonInclude(NON_EMPTY)
    private PhoneNumber phoneNumber;

    @JsonProperty(PRIMARY_CONTACT_NUMBER)
    @Valid
    @JsonInclude(NON_EMPTY)
    private PhoneNumber primaryContactNumber;

    @JsonProperty(PERMANENT_ADDRESS)
    @Valid
    @JsonInclude(NON_EMPTY)
    @Location(message = "1004")
    private Address permanentAddress;

    @JsonProperty(MARITAL_STATUS)
    @JsonInclude(NON_EMPTY)
    @Code(type = MARITAL_STATUS, regexp = "[\\d]{1}", message = "1004")
    private String maritalStatus;

    @JsonProperty(FULL_NAME)
    @JsonInclude(NON_EMPTY)
    private String fullName;

    @JsonProperty(PATIENT_STATUS)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^(alive|deceased|unknown)$", message = "1004")
    private String status;

    @JsonProperty(DATE_OF_DEATH)
    @JsonInclude(NON_EMPTY)
    @Date(format = "yyyy-MM-dd", message = "1002")
    private String dateOfDeath;

    @JsonProperty(CREATED)
    @JsonInclude(NON_EMPTY)
    private String createdAt;

    @JsonProperty(MODIFIED)
    @JsonInclude(NON_EMPTY)
    private String updatedAt;

    @JsonIgnore
    private TreeSet<PendingApproval> pendingApprovals;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public Relation getRelationOfType(String relationType) {

        if (this.relations == null) {
            return null;
        }

        for (Relation relation : this.relations) {

            if (relation.getType() != null && relation.getType().equals(relationType)) {
                return relation;
            }
        }

        return null;
    }

    public Relation getRelationById(String id) {

        if (this.relations == null) {
            return null;
        }

        for (Relation relation : this.relations) {

            if (relation.getId() != null && relation.getId().equals(id)) {
                return relation;
            }
        }

        return null;
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

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    @JsonIgnore
    public void setCreatedAt(java.util.Date createdAt) {
        this.createdAt = (createdAt == null) ? null : DateFormatUtils.ISO_DATETIME_FORMAT.format(createdAt);
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    @JsonIgnore
    public void setUpdatedAt(java.util.Date updatedAt) {
        this.updatedAt = (updatedAt == null) ? null : DateFormatUtils.ISO_DATETIME_FORMAT.format(updatedAt);
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public PhoneNumber getPrimaryContactNumber() {
        return primaryContactNumber;
    }

    public void setPrimaryContactNumber(PhoneNumber primaryContactNumber) {
        this.primaryContactNumber = primaryContactNumber;
    }

    public TreeSet<PendingApproval> getPendingApprovals() {
        return pendingApprovals;
    }

    public void setPendingApprovals(TreeSet<PendingApproval> pendingApprovals) {
        this.pendingApprovals = pendingApprovals;
    }

    public Object getValue(String jsonKey) {
        for (Field field : PatientData.class.getDeclaredFields()) {
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            if (jsonProperty != null) {
                String value = jsonProperty.value();
                if (value != null && value.equals(jsonKey)) {
                    try {
                        return field.get(this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return null;
    }

    public List<String> findNonEmptyFieldNames() {
        List<String> fieldNames = new ArrayList<>();
        for (Field field : PatientData.class.getDeclaredFields()) {
            try {
                Object value = field.get(this);
                if (value != null) {
                    if (value instanceof String && StringUtils.isBlank(valueOf(value))) {
                        continue;
                    }
                    JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                    if (jsonProperty != null) {
                        fieldNames.add(jsonProperty.value());
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return fieldNames;
    }

    public boolean belongsTo(Catchment catchment) {
        Address address = this.getAddress();

        if (catchment == null || address == null) {
            throw new IllegalArgumentException("invalid.catchment");
        }

        String catchmentId = defaultString(catchment.getDivisionId()) + defaultString(catchment.getDistrictId())
                + defaultString(catchment.getUpazilaId()) + defaultString(catchment.getCityCorpId())
                + defaultString(catchment.getUnionOrUrbanWardId()) + defaultString(catchment.getRuralWardId());

        String addressId = defaultString(address.getDivisionId()) + defaultString(address.getDistrictId())
                + defaultString(address.getUpazilaId()) + defaultString(address.getCityCorporationId())
                + defaultString(address.getUnionOrUrbanWardId()) + defaultString(address.getRuralWardId());

        return addressId.startsWith(catchmentId);
    }

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getDateOfDeath() {
        return this.dateOfDeath;
    }

    public void setDateOfDeath(String dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }
}
