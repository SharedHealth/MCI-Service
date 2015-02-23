package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class ChangeSet {

    @JsonProperty(FIELD_NAME)
    private String fieldName;

    @JsonProperty(OLD_VALUE)
    private String oldValue;

    @JsonProperty(NEW_VALUE)
    private String newValue;

    @JsonProperty(PROPOSED_BY)
    private String proposedBy;

    @JsonProperty(APPROVED_BY)
    private String approvedBy;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getProposedBy() {
        return proposedBy;
    }

    public void setProposedBy(String proposedBy) {
        this.proposedBy = proposedBy;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
}
