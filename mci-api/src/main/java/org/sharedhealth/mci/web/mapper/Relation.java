package org.sharedhealth.mci.web.mapper;

import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.validation.constraints.Length;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public class Relation {
    private static final Logger logger = LoggerFactory.getLogger(Relation.class);

    public Relation() {
        UUID idOne = UUID.randomUUID();
        this.Id = idOne.toString();
    }

    @JsonProperty("type")
    @JsonInclude(NON_EMPTY)
    private String type;

    @JsonProperty("name_bangla")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 120, message = "1002")
    private String nameBangla;

    @JsonProperty("given_name")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 100, message = "1002")
    private String givenName;

    @JsonProperty("sur_name")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[a-zA-Z0-9]{0,25}$",message = "1002")
    private String surName;

    @JsonProperty("uid")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{11}", message = "1002")
    private String uid;

    @JsonProperty("nid")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{13}|[\\d]{17}", message = "1002")
    private String nid;

    @JsonProperty("bin_brn")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{17}", message = "1002")
    private String binBrn;

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

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getBinBrn() {
        return binBrn;
    }

    public void setBinBrn(String binBrn) {
        this.binBrn = binBrn;
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
}
