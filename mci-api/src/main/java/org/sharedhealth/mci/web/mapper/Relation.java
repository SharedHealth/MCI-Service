package org.sharedhealth.mci.web.mapper;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.validation.constraints.Length;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public class Relation {

    public Relation() {
        UUID idOne = UUID.randomUUID();
        this.Id = idOne.toString();
    }

    @JsonProperty("type")
    @JsonInclude(NON_EMPTY)
    @NotNull(message = "1001", groups = CreateGroup.class)
    @Pattern(regexp = "^(father|mother|spouse)$", message = "1004")
    private String type;

    @JsonProperty("hid")
    @JsonInclude(NON_EMPTY)
    private String healthId;

    @JsonProperty("nid")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{13}|[\\d]{17}", message = "1002")
    private String nationalId;

    @JsonProperty("uid")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{11}", message = "1002")
    private String uid;

    @JsonProperty("bin_brn")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{17}", message = "1002")
    private String birthRegistrationNumber;

    @JsonProperty("name_bangla")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize = 125, message = "1002")
    private String nameBangla;

    @JsonProperty("given_name")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize = 100, message = "1002")
    private String givenName;

    @JsonProperty("sur_name")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^(\\s*)([A-Za-z0-9]{0,25})(\\b\\s*$)", message = "1002")
    private String surName;

    @JsonProperty("marriage_id")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{8}", message = "1002")
    private String marriageId;

    @JsonProperty("relational_status")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[3|4|5]", message = "1004")
    private String relationalStatus;

    @JsonProperty("id")
    private String Id;

    public String getNameBangla() {
        return nameBangla;
    }

    public void setNameBangla(String nameBangla) {
        this.nameBangla = nameBangla;
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
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
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
}
