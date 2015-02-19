package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "approval_fields")
public class ApprovalField {

    @PrimaryKey("field")
    private String field;

    @Column("option")
    private String option;

    public ApprovalField(){}

    public ApprovalField(String field, String option){
        this.field = field;
        this.option = option;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApprovalField)) return false;

        ApprovalField approvalField = (ApprovalField) o;

        if (field != null ? !field.equals(approvalField.field) : approvalField.field != null) return false;
        if (option != null ? !option.equals(approvalField.option) : approvalField.option != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (option != null ? option.hashCode() : 0);
        return result;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
