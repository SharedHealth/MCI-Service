package org.sharedhealth.mci.web.model;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.sharedhealth.mci.validation.constraints.*;
import org.sharedhealth.mci.validation.constraints.Location;
import org.sharedhealth.mci.validation.constraints.Length;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public class maritalRelation {
    @JsonProperty("marital_status")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[1-5]{1}", message = "1028")
    private String maritalStatus;

    @JsonProperty("marriage_id")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[a-zA-Z0-9]{8}", message = "1029")
    private String marriageId;

    @JsonProperty("spouse_name")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 100, message = "1030")
    private String spouseName;

    @JsonProperty("spouse_name_bangla")
    @JsonInclude(NON_EMPTY)
    @Length(lengthSize= 120, message = "1039")
    private String spouseNameBangla;

    @JsonProperty("spouse_uid_nid")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{10}|[\\d]{17}", message = "1040")
    private String spouseUidNid;

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


}
