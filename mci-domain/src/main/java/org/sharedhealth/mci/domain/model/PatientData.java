package org.sharedhealth.mci.domain.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.sharedhealth.mci.domain.diff.DiffBuilder;
import org.sharedhealth.mci.domain.diff.DiffResult;
import org.sharedhealth.mci.domain.diff.Diffable;
import org.sharedhealth.mci.domain.util.DateStringDeserializer;
import org.sharedhealth.mci.domain.util.DateUtil;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.sharedhealth.mci.domain.util.WhiteSpaceRemovalDeserializer;
import org.sharedhealth.mci.domain.validation.constraints.*;
import org.sharedhealth.mci.domain.validation.group.RequiredGroup;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.*;
import static org.sharedhealth.mci.domain.constant.ErrorConstants.*;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;
import static org.sharedhealth.mci.domain.constant.MCIConstants.*;

@MaritalRelation(message = ERROR_CODE_DEPENDENT, field = "maritalStatus")
@JsonIgnoreProperties(ignoreUnknown = false, value = {"created_at" })
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility =
        JsonAutoDetect.Visibility.NONE)
public class PatientData implements Diffable<PatientData> {

    private static final String INVALID_CATCHMENT = "invalid.catchment";

    private static final String JSON = ".json";

    @JsonProperty(HID)
    @JsonInclude(NON_EMPTY)
    private String healthId;

    @JsonProperty(ASSIGNED_BY)
    @JsonInclude(NON_EMPTY)
    private String assignedBy;

    @JsonProperty(NID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^|[\\d]{13}|[\\d]{17}|[\\d]{10}", message = ERROR_CODE_PATTERN)
    private String nationalId;

    @JsonProperty(BIN_BRN)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^|[\\d]{17}", message = ERROR_CODE_PATTERN)
    private String birthRegistrationNumber;

    @JsonProperty(NAME_BANGLA)
    @JsonInclude(NON_EMPTY)
    @Length(max = 125, message = ERROR_CODE_PATTERN)
    private String nameBangla;

    @JsonProperty(GIVEN_NAME)
    @JsonInclude(NON_EMPTY)
    @NotNull(message = ERROR_CODE_REQUIRED, groups = RequiredGroup.class)
    @Length(max = 100, min = 1, message = ERROR_CODE_PATTERN)
    private String givenName;

    @JsonProperty(SUR_NAME)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^|([A-Za-z0-9]{1,25})$", message = ERROR_CODE_PATTERN)
    @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
    private String surName;

    @JsonProperty(DATE_OF_BIRTH)
    @JsonInclude(NON_EMPTY)
    @NotNull(message = ERROR_CODE_REQUIRED, groups = RequiredGroup.class)
    @Length(min = 1, message = ERROR_CODE_PATTERN)
    @Date(message = ERROR_CODE_PATTERN)
    @JsonDeserialize(using = DateStringDeserializer.class)
    private String dateOfBirth; 

    @JsonProperty(DOB_TYPE)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[123]", message = ERROR_CODE_INVALID)
    private String dobType;

    @JsonProperty(GENDER)
    @JsonInclude(NON_EMPTY)
    @NotBlank(message = ERROR_CODE_REQUIRED, groups = RequiredGroup.class)
    @Code(type = GENDER, regexp = "[A-Z]{1}", message = ERROR_CODE_INVALID)
    private String gender;

    @JsonProperty(OCCUPATION)
    @JsonInclude(NON_EMPTY)
    @Code(type = OCCUPATION, allowBlank = true, regexp = "[\\d]{2}", message = ERROR_CODE_INVALID)
    private String occupation;

    @JsonProperty(EDU_LEVEL)
    @JsonInclude(NON_EMPTY)
    @Code(type = EDUCATION_LEVEL, allowBlank = true, regexp = "[\\d]{2}", message = ERROR_CODE_INVALID)
    private String educationLevel;

    @JsonProperty(RELATIONS)
    @JsonInclude(NON_EMPTY)
    @Valid
    private List<Relation> relations;

    @JsonProperty(UID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^|[a-zA-Z0-9]{11}", message = ERROR_CODE_PATTERN)
    private String uid;

    @JsonInclude(NON_EMPTY)
    @JsonProperty(PLACE_OF_BIRTH)
    @Pattern(regexp = "^[a-zA-Z0-9]{0,20}$", message = ERROR_CODE_PATTERN)
    private String placeOfBirth;

    @JsonProperty(RELIGION)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[1|2|3|4|8|9|0]{1}", message = ERROR_CODE_INVALID)
    private String religion;

    @JsonProperty(BLOOD_GROUP)
    @JsonInclude(NON_EMPTY)
    @Code(type = "blood_group", regexp = "[\\d]{1}", message = ERROR_CODE_INVALID)
    private String bloodGroup;

    @JsonProperty(NATIONALITY)
    @JsonInclude(NON_EMPTY)
    @Length(max = 50, message = ERROR_CODE_PATTERN)
    private String nationality;

    @JsonProperty(DISABILITY)
    @JsonInclude(NON_EMPTY)
    @Code(type = DISABILITY, regexp = "[\\d]{1}", message = ERROR_CODE_INVALID)
    private String disability;

    @JsonProperty(ETHNICITY)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "|[0-9]{2}", message = ERROR_CODE_INVALID)
    private String ethnicity;

    @JsonProperty(PRESENT_ADDRESS)
    @JsonInclude(NON_EMPTY)
    @NotNull(message = ERROR_CODE_REQUIRED, groups = RequiredGroup.class)
    @Valid
    @org.sharedhealth.mci.domain.validation.constraints.Location(message = ERROR_CODE_INVALID, country_code = COUNTRY_CODE_BANGLADESH,
            required = true)
    private Address address;

    @JsonProperty(PRIMARY_CONTACT)
    @JsonInclude(NON_EMPTY)
    @Length(max = 100, message = ERROR_CODE_PATTERN)
    private String primaryContact;

    @JsonProperty(PHONE_NUMBER)
    @Valid
    @JsonInclude(NON_EMPTY)
    @PhoneBlock(message = ERROR_CODE_INVALID)
    private PhoneNumber phoneNumber;

    @JsonProperty(PRIMARY_CONTACT_NUMBER)
    @Valid
    @JsonInclude(NON_EMPTY)
    @PhoneBlock(message = ERROR_CODE_INVALID)
    private PhoneNumber primaryContactNumber;

    @JsonProperty(PERMANENT_ADDRESS)
    @Valid
    @JsonInclude(NON_EMPTY)
    @org.sharedhealth.mci.domain.validation.constraints.Location(message = ERROR_CODE_INVALID, required = false)
    private Address permanentAddress;

    @JsonProperty(MARITAL_STATUS)
    @JsonInclude(NON_EMPTY)
    @Code(type = MARITAL_STATUS, regexp = "[\\d]{1}", message = ERROR_CODE_INVALID)
    private String maritalStatus;

    @JsonProperty(CONFIDENTIAL)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^(yes|no)$", message = ERROR_CODE_INVALID, flags = Flag.CASE_INSENSITIVE)
    private String confidential;

    @JsonProperty(HOUSEHOLD_CODE)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^([\\d]*)$", message = ERROR_CODE_INVALID)
    private String householdCode;

    private UUID createdAt;

    @JsonProperty(CREATED_BY)
    @JsonInclude(NON_EMPTY)
    private Requester createdBy;

    private UUID updatedAt;

    @JsonProperty(UPDATED_BY)
    @JsonInclude(NON_EMPTY)
    private Requester updatedBy;

    @JsonProperty(STATUS)
    @Valid
    @JsonInclude(NON_EMPTY)
    @PatientStatusBlock(message = ERROR_CODE_INVALID)
    private PatientStatus patientStatus;

    @JsonProperty(ACTIVE)
    @JsonInclude(NON_EMPTY)
    private Boolean active;

    @JsonProperty(MERGED_WITH)
    @JsonInclude(NON_EMPTY)
    private String mergedWith;

    @JsonProperty(HID_CARD_STATUS)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^(" + HID_CARD_STATUS_REGISTERED + "|" + HID_CARD_STATUS_ISSUED + ")$", message = ERROR_CODE_INVALID, flags = Flag.CASE_INSENSITIVE)
    private String hidCardStatus;

    @JsonIgnore
    private TreeSet<PendingApproval> pendingApprovals;

    @JsonIgnore
    private Requester requester;

    @ProviderUrl
    private String provider;


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

    public java.util.Date getDateOfBirth() {
        return dateOfBirth == null ? null : DateUtil.parseDate(dateOfBirth);
    }

    public void setDateOfBirth(java.util.Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth == null ? null : DateUtil.toIsoMillisFormat(dateOfBirth);
    }

    public String getDobType() {
        return dobType;
    }

    public void setDobType(String dobType) {
        this.dobType = dobType;
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

    public void setPermanentAddress(Address permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
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

    @JsonProperty(CREATED)
    @JsonInclude(NON_EMPTY)
    public String getCreatedAtAsString() {
        return this.createdAt != null ? DateUtil.toIsoMillisFormat(TimeUuidUtil.getTimeFromUUID(this.createdAt)) : null;
    }

    @JsonProperty(CREATED)
    @JsonInclude(NON_EMPTY)
    public void setCreatedAtAsString(String createdAt) {
    }

    @JsonIgnore
    public UUID getCreatedAt() {
        return this.createdAt;
    }

    @JsonIgnore
    public void setCreatedAt(UUID createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty(MODIFIED)
    @JsonInclude(NON_EMPTY)
    public String getUpdatedAtAsString() {
        return this.updatedAt != null ? DateUtil.toIsoMillisFormat(TimeUuidUtil.getTimeFromUUID(this.updatedAt)) : null;
    }

    @JsonProperty(MODIFIED)
    @JsonInclude(NON_EMPTY)
    public void setUpdatedAtAsString(String updatedAt) {
    }

    @JsonIgnore
    public UUID getUpdatedAt() {
        return this.updatedAt;
    }

    @JsonIgnore
    public void setUpdatedAt(UUID updatedAt) {
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

    public void addPendingApproval(PendingApproval pendingApproval) {
        TreeSet<PendingApproval> pendingApprovals = this.getPendingApprovals();
        if (pendingApprovals == null) {
            pendingApprovals = new TreeSet<>();
        }

        if (!pendingApprovals.contains(pendingApproval)) {
            pendingApprovals.add(pendingApproval);

        } else {
            for (PendingApproval p : pendingApprovals) {
                if (p.equals(pendingApproval)) {
                    p.addFieldDetails(pendingApproval.getFieldDetails());
                }
            }
        }
        this.setPendingApprovals(pendingApprovals);
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
            throw new IllegalArgumentException(INVALID_CATCHMENT);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatientData)) return false;

        PatientData that = (PatientData) o;

        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (birthRegistrationNumber != null ? !birthRegistrationNumber.equals(that.birthRegistrationNumber) : that
                .birthRegistrationNumber != null)
            return false;
        if (bloodGroup != null ? !bloodGroup.equals(that.bloodGroup) : that.bloodGroup != null) return false;
        if (confidential != null ? !confidential.equals(that.confidential) : that.confidential != null) return false;
        if (dateOfBirth != null ? !DateUtil.isEqualTo(getDateOfBirth(), that.getDateOfBirth()) : that.dateOfBirth != null)
            return false;
        if (dobType != null ? !dobType.equals(that.dobType) : that.dobType != null) return false;
        if (disability != null ? !disability.equals(that.disability) : that.disability != null) return false;
        if (educationLevel != null ? !educationLevel.equals(that.educationLevel) : that.educationLevel != null)
            return false;
        if (ethnicity != null ? !ethnicity.equals(that.ethnicity) : that.ethnicity != null) return false;
        if (gender != null ? !gender.equals(that.gender) : that.gender != null) return false;
        if (givenName != null ? !givenName.equals(that.givenName) : that.givenName != null) return false;
        if (healthId != null ? !healthId.equals(that.healthId) : that.healthId != null) return false;
        if (maritalStatus != null ? !maritalStatus.equals(that.maritalStatus) : that.maritalStatus != null)
            return false;
        if (nameBangla != null ? !nameBangla.equals(that.nameBangla) : that.nameBangla != null) return false;
        if (nationalId != null ? !nationalId.equals(that.nationalId) : that.nationalId != null) return false;
        if (nationality != null ? !nationality.equals(that.nationality) : that.nationality != null) return false;
        if (occupation != null ? !occupation.equals(that.occupation) : that.occupation != null) return false;
        if (pendingApprovals != null ? !pendingApprovals.equals(that.pendingApprovals) : that.pendingApprovals != null)
            return false;
        if (permanentAddress != null ? !permanentAddress.equals(that.permanentAddress) : that.permanentAddress != null)
            return false;
        if (phoneNumber != null ? !phoneNumber.equals(that.phoneNumber) : that.phoneNumber != null) return false;
        if (patientStatus != null ? !patientStatus.equals(that.patientStatus) : that.patientStatus != null)
            return false;
        if (placeOfBirth != null ? !placeOfBirth.equals(that.placeOfBirth) : that.placeOfBirth != null) return false;
        if (primaryContact != null ? !primaryContact.equals(that.primaryContact) : that.primaryContact != null)
            return false;
        if (primaryContactNumber != null ? !primaryContactNumber.equals(that.primaryContactNumber) : that.primaryContactNumber != null)
            return false;
        if (CollectionUtils.isNotEmpty(relations) ? !relations.equals(that.relations) : CollectionUtils.isNotEmpty(that.relations))
            return false;
        if (religion != null ? !religion.equals(that.religion) : that.religion != null) return false;
        if (surName != null ? !surName.equals(that.surName) : that.surName != null) return false;
        if (uid != null ? !uid.equals(that.uid) : that.uid != null) return false;
        if (householdCode != null ? !householdCode.equals(that.householdCode) : that.householdCode != null)
            return false;
        if (active != null ? !active.equals(that.active) : that.active != null) return false;
        if (mergedWith != null ? !mergedWith.equals(that.mergedWith) : that.mergedWith != null) return false;
        if (hidCardStatus != null ? !hidCardStatus.equals(that.hidCardStatus) : that.hidCardStatus != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = healthId != null ? healthId.hashCode() : 0;
        result = 31 * result + (nationalId != null ? nationalId.hashCode() : 0);
        result = 31 * result + (birthRegistrationNumber != null ? birthRegistrationNumber.hashCode() : 0);
        result = 31 * result + (nameBangla != null ? nameBangla.hashCode() : 0);
        result = 31 * result + (givenName != null ? givenName.hashCode() : 0);
        result = 31 * result + (surName != null ? surName.hashCode() : 0);
        result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
        result = 31 * result + (dobType != null ? dobType.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (occupation != null ? occupation.hashCode() : 0);
        result = 31 * result + (educationLevel != null ? educationLevel.hashCode() : 0);
        result = 31 * result + (relations != null ? relations.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (placeOfBirth != null ? placeOfBirth.hashCode() : 0);
        result = 31 * result + (religion != null ? religion.hashCode() : 0);
        result = 31 * result + (bloodGroup != null ? bloodGroup.hashCode() : 0);
        result = 31 * result + (nationality != null ? nationality.hashCode() : 0);
        result = 31 * result + (disability != null ? disability.hashCode() : 0);
        result = 31 * result + (ethnicity != null ? ethnicity.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (primaryContact != null ? primaryContact.hashCode() : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        result = 31 * result + (patientStatus != null ? patientStatus.hashCode() : 0);
        result = 31 * result + (primaryContactNumber != null ? primaryContactNumber.hashCode() : 0);
        result = 31 * result + (permanentAddress != null ? permanentAddress.hashCode() : 0);
        result = 31 * result + (maritalStatus != null ? maritalStatus.hashCode() : 0);
        result = 31 * result + (confidential != null ? confidential.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        result = 31 * result + (pendingApprovals != null ? pendingApprovals.hashCode() : 0);
        result = 31 * result + (householdCode != null ? householdCode.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (mergedWith != null ? mergedWith.hashCode() : 0);
        result = 31 * result + (hidCardStatus != null ? hidCardStatus.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PatientData{");
        sb.append("healthId='").append(healthId).append('\'');
        sb.append(", nationalId='").append(nationalId).append('\'');
        sb.append(", birthRegistrationNumber='").append(birthRegistrationNumber).append('\'');
        sb.append(", nameBangla='").append(nameBangla).append('\'');
        sb.append(", givenName='").append(givenName).append('\'');
        sb.append(", surName='").append(surName).append('\'');
        sb.append(", dateOfBirth='").append(dateOfBirth).append('\'');
        sb.append(", dobType='").append(dobType).append('\'');
        sb.append(", gender='").append(gender).append('\'');
        sb.append(", occupation='").append(occupation).append('\'');
        sb.append(", educationLevel='").append(educationLevel).append('\'');
        sb.append(", relations=").append(relations);
        sb.append(", uid='").append(uid).append('\'');
        sb.append(", placeOfBirth='").append(placeOfBirth).append('\'');
        sb.append(", religion='").append(religion).append('\'');
        sb.append(", bloodGroup='").append(bloodGroup).append('\'');
        sb.append(", nationality='").append(nationality).append('\'');
        sb.append(", disability='").append(disability).append('\'');
        sb.append(", ethnicity='").append(ethnicity).append('\'');
        sb.append(", address=").append(address);
        sb.append(", primaryContact='").append(primaryContact).append('\'');
        sb.append(", phoneNumber=").append(phoneNumber);
        sb.append(", patientStatus=").append(patientStatus);
        sb.append(", primaryContactNumber=").append(primaryContactNumber);
        sb.append(", permanentAddress=").append(permanentAddress);
        sb.append(", maritalStatus='").append(maritalStatus).append('\'');
        sb.append(", confidential='").append(confidential).append('\'');
        sb.append(", householdCode='").append(householdCode).append('\'');
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", pendingApprovals=").append(pendingApprovals);
        sb.append(", requester='").append(requester).append('\'');
        sb.append(", active='").append(active).append('\'');
        sb.append(", mergedWith='").append(mergedWith).append('\'');
        sb.append(", hidCardStatus='").append(hidCardStatus).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getConfidential() {
        return confidential;
    }

    public void setConfidential(String confidential) {
        this.confidential = confidential;
    }

    @JsonIgnore
    public Catchment getCatchment() {
        Address address = this.getAddress();
        String divisionId = address.getDivisionId();
        String districtId = address.getDistrictId();
        if (isBlank(divisionId) || isBlank(districtId)) {
            return null;
        }
        return new Catchment(divisionId, districtId, address.getUpazilaId(),
                address.getCityCorporationId(), address.getUnionOrUrbanWardId(), address.getRuralWardId());
    }

    public String getHouseholdCode() {
        return householdCode;
    }

    public void setHouseholdCode(String householdCode) {
        this.householdCode = householdCode;
    }

    @JsonProperty(PROVIDER)
    public void setProvider(String provider) {
        this.provider = provider;
    }

    @JsonIgnore
    public void setRequester(String facilityId, String providerId, String adminId, String name) {
        RequesterDetails facility = null;
        RequesterDetails provider = null;
        RequesterDetails admin = null;

        if (isNotBlank(facilityId)) {
            facility = new RequesterDetails(facilityId);
        }

        if (providerId == null) providerId = this.getProviderId();

        if (isNotBlank(providerId)) {
            provider = new RequesterDetails(providerId);
        }

        if (isNotBlank(adminId)) {
            admin = new RequesterDetails(adminId, name);
        }

        this.requester = new Requester(facility, provider, admin);
    }

    @JsonIgnore
    public String getProviderId() {
        String idWithJson = substringAfterLast(this.provider, "/");
        return StringUtils.substringBefore(idWithJson, JSON);
    }

    @JsonIgnore
    public void setRequester(String facilityId, String providerId) {
        RequesterDetails facility = null;
        RequesterDetails provider = null;

        if (isNotBlank(facilityId)) {
            facility = new RequesterDetails(facilityId);
        }

        if (isNotBlank(providerId)) {
            provider = new RequesterDetails(providerId);
        }

        this.requester = new Requester(facility, provider, null);
    }

    @JsonIgnore
    public void setRequester(String facilityId, String providerId, String adminId) {
        RequesterDetails facility = null;
        RequesterDetails provider = null;
        RequesterDetails admin = null;

        if (isNotBlank(facilityId)) {
            facility = new RequesterDetails(facilityId);
        }

        if (isNotBlank(providerId)) {
            provider = new RequesterDetails(providerId);
        }

        if (isNotBlank(adminId)) {
            admin = new RequesterDetails(adminId);
        }

        this.requester = new Requester(facility, provider, admin);
    }

    public Requester getRequester() {
        return requester;
    }

    public Requester getCreatedBy() {
        return createdBy;
    }

    @JsonIgnore
    public void setCreatedBy(Requester createdBy) {
        this.createdBy = createdBy;
    }

    public PatientStatus getPatientStatus() {
        return patientStatus;
    }

    public void setPatientStatus(PatientStatus patientStatus) {
        this.patientStatus = patientStatus;
    }

    public Boolean isActive() {
        return active;
    }

    @JsonIgnore
    public boolean isRetired() {
        return active != null && !active;
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

    public Requester getUpdatedBy() {
        return updatedBy;
    }

    @JsonIgnore
    public void setUpdatedBy(Requester updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getHidCardStatus() {
        return StringUtils.upperCase(hidCardStatus);
    }

    public void setHidCardStatus(String hidCardStatus) {
        this.hidCardStatus = hidCardStatus;
    }


    @Override
    public DiffResult diff(PatientData that) {
        return new DiffBuilder(this, that)
                .append(HID, this.healthId, that.healthId)
                .append(NID, this.nationalId, that.nationalId)
                .append(BIN_BRN, this.birthRegistrationNumber, that.birthRegistrationNumber)
                .append(UID, this.uid, that.uid)

                .append(NAME_BANGLA, this.nameBangla, that.nameBangla)
                .append(GIVEN_NAME, this.givenName, that.givenName)
                .append(SUR_NAME, this.surName, that.surName)
                .append(DATE_OF_BIRTH, this.dateOfBirth, that.dateOfBirth)
                .append(DOB_TYPE, this.dobType, that.dobType)
                .append(GENDER, this.gender, that.gender)
                .append(OCCUPATION, this.occupation, that.occupation)
                .append(EDU_LEVEL, this.educationLevel, that.educationLevel)
                .append(PLACE_OF_BIRTH, this.placeOfBirth, that.placeOfBirth)
                .append(RELIGION, this.religion, that.religion)
                .append(BLOOD_GROUP, this.bloodGroup, that.bloodGroup)
                .append(NATIONALITY, this.nationality, that.nationality)
                .append(DISABILITY, this.disability, that.disability)
                .append(ETHNICITY, this.ethnicity, that.ethnicity)
                .append(MARITAL_STATUS, this.maritalStatus, that.maritalStatus)
                .append(CONFIDENTIAL, this.confidential, that.confidential)
                .append(HOUSEHOLD_CODE, this.householdCode, that.householdCode)

                .append(PHONE_NUMBER, this.phoneNumber, that.phoneNumber)
                .append(STATUS, this.patientStatus, that.patientStatus)
                .append(PRIMARY_CONTACT_NUMBER, this.primaryContactNumber, that.primaryContactNumber)
                .append(PRIMARY_CONTACT, this.primaryContact, that.primaryContact)

                .append(PRESENT_ADDRESS, this.address, that.address)
                .append(PERMANENT_ADDRESS, this.permanentAddress, that.permanentAddress)

                .append(RELATIONS, this.relations, that.relations)
                .append(ACTIVE, this.active, that.active)
                .append(MERGED_WITH, this.mergedWith, that.mergedWith)
                .append(HID_CARD_STATUS, this.getHidCardStatus(), that.getHidCardStatus())

                .build();
    }
}
