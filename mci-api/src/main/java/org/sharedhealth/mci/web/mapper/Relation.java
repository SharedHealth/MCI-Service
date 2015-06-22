package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.utils.WhiteSpaceRemovalDeserializer;
import org.sharedhealth.mci.validation.constraints.Code;
import org.sharedhealth.mci.validation.constraints.Length;
import org.sharedhealth.mci.validation.constraints.RelationType;

import javax.validation.constraints.Pattern;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.web.utils.ErrorConstants.*;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

@RelationType(message = ERROR_CODE_REQUIRED, field = "type")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Relation {

    private final String RELATION_TYPE = "type";
    private final String RELATIONS_CODE_TYPE = "relations";

    @JsonProperty(RELATION_TYPE)
    @JsonInclude(NON_EMPTY)
    @Code(type = RELATIONS_CODE_TYPE, message = ERROR_CODE_INVALID)
    private String type;

    @JsonProperty(HID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{11}", message = ERROR_CODE_PATTERN)
    @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
    private String healthId;

    @JsonProperty(NID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{13}|[\\d]{17}", message = ERROR_CODE_PATTERN)
    @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
    private String nationalId;

    @JsonProperty(UID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{11}", message = ERROR_CODE_PATTERN)
    @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
    private String uid;

    @JsonProperty(BIN_BRN)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{17}", message = ERROR_CODE_PATTERN)
    @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
    private String birthRegistrationNumber;

    @JsonProperty(NAME_BANGLA)
    @JsonInclude(NON_EMPTY)
    @Length(max = 125, message = ERROR_CODE_PATTERN)
    @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
    private String nameBangla;

    @JsonProperty(GIVEN_NAME)
    @JsonInclude(NON_EMPTY)
    @Length(max = 100, message = ERROR_CODE_PATTERN)
    @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
    private String givenName;

    @JsonProperty(SUR_NAME)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[A-Za-z0-9]{0,25}", message = ERROR_CODE_PATTERN)
    @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
    private String surName;

    @JsonProperty(MARRIAGE_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{8}", message = ERROR_CODE_PATTERN)
    @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
    private String marriageId;

    @JsonProperty(RELATIONAL_STATUS)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[2|3|4|5]", message = ERROR_CODE_INVALID)
    private String relationalStatus;

    @JsonProperty("id")
    private String id;

    public String getNameBangla() {
        return nameBangla;
    }

    public void setNameBangla(String nameBangla) {
        this.nameBangla = nameBangla.trim();
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName.trim();
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName.trim();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getBirthRegistrationNumber() {
        return birthRegistrationNumber;
    }

    public void setBirthRegistrationNumber(String birthRegistrationNumber) {
        this.birthRegistrationNumber = birthRegistrationNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMarriageId() {
        return marriageId;
    }

    public void setMarriageId(String marriageId) {
        this.marriageId = marriageId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRelationalStatus() {
        return relationalStatus;
    }

    public void setRelationalStatus(String relationalStatus) {
        this.relationalStatus = relationalStatus;
    }

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "type='" + type + '\'' +
                ", healthId='" + healthId + '\'' +
                ", nationalId='" + nationalId + '\'' +
                ", uid='" + uid + '\'' +
                ", birthRegistrationNumber='" + birthRegistrationNumber + '\'' +
                ", nameBangla='" + nameBangla + '\'' +
                ", givenName='" + givenName + '\'' +
                ", surName='" + surName + '\'' +
                ", marriageId='" + marriageId + '\'' +
                ", relationalStatus='" + relationalStatus + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Relation)) return false;

        Relation relation = (Relation) o;

        if (birthRegistrationNumber != null ? !birthRegistrationNumber.equals(relation.birthRegistrationNumber) : relation.birthRegistrationNumber != null)
            return false;
        if (givenName != null ? !givenName.equals(relation.givenName) : relation.givenName != null) return false;
        if (healthId != null ? !healthId.equals(relation.healthId) : relation.healthId != null) return false;
        if (marriageId != null ? !marriageId.equals(relation.marriageId) : relation.marriageId != null) return false;
        if (nameBangla != null ? !nameBangla.equals(relation.nameBangla) : relation.nameBangla != null) return false;
        if (nationalId != null ? !nationalId.equals(relation.nationalId) : relation.nationalId != null) return false;
        if (relationalStatus != null ? !relationalStatus.equals(relation.relationalStatus) : relation.relationalStatus != null)
            return false;
        if (surName != null ? !surName.equals(relation.surName) : relation.surName != null) return false;
        if (!type.equals(relation.type)) return false;
        if (uid != null ? !uid.equals(relation.uid) : relation.uid != null) return false;

        return true;
    }

    @JsonIgnore
    public boolean isEmpty() {

        if (StringUtils.isNotBlank(birthRegistrationNumber)) return false;
        if (StringUtils.isNotBlank(givenName)) return false;
        if (StringUtils.isNotBlank(healthId)) return false;
        if (StringUtils.isNotBlank(marriageId)) return false;
        if (StringUtils.isNotBlank(nameBangla)) return false;
        if (StringUtils.isNotBlank(nationalId)) return false;
        if (StringUtils.isNotBlank(relationalStatus)) return false;
        if (StringUtils.isNotBlank(surName)) return false;
        if (StringUtils.isNotBlank(uid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (healthId != null ? healthId.hashCode() : 0);
        result = 31 * result + (nationalId != null ? nationalId.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (birthRegistrationNumber != null ? birthRegistrationNumber.hashCode() : 0);
        result = 31 * result + (nameBangla != null ? nameBangla.hashCode() : 0);
        result = 31 * result + (givenName != null ? givenName.hashCode() : 0);
        result = 31 * result + (surName != null ? surName.hashCode() : 0);
        result = 31 * result + (marriageId != null ? marriageId.hashCode() : 0);
        result = 31 * result + (relationalStatus != null ? relationalStatus.hashCode() : 0);
        return result;
    }
}
