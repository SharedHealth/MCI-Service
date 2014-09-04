package org.sharedhealth.mci.web.model;

import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.validation.constraints.Length;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public class Relation {

    @JsonProperty("type")
    @JsonInclude(NON_EMPTY)
    private String type;

    @JsonProperty("name_bangla")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 120, message = "1013")
    private String nameBangla;

    @JsonProperty("first_name")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 25, message = "1014")
    private String firstName;

    @JsonProperty("middle_name")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 25, message = "1015")
    private String middleName;

    @JsonProperty("last_name")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 25, message = "1016")
    private String lastName;

    @JsonProperty("uid")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{11}", message = "1017")
    private String uid;

    @JsonProperty("nid")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{13}|[\\d]{17}", message = "1018")
    private String nid;

    @JsonProperty("brn")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{17}", message = "1019")
    private String brn;

    @JsonProperty("marital_status")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[1-5]{1}", message = "1028")
    private String maritalStatus;

    @JsonProperty("marriage_id")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{8}", message = "1029")
    private String marriageId;

    public String getNameBangla() {
        return nameBangla;
    }

    public void setNameBangla(String nameBangla) {
        this.nameBangla = nameBangla;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getBrn() {
        return brn;
    }

    public void setBrn(String brn) {
        this.brn = brn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
