package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.UUID;

import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PendingApprovalDetails implements Comparable<PendingApprovalDetails> {

    @JsonProperty(FIELD_NAME)
    private String name;

    @JsonProperty(CURRENT_VALUE)
    private Object currentValue;

    @JsonProperty(FIELD_DETAILS)
    TreeMap<UUID, PendingApprovalFieldDetails> fieldDetails;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Object currentValue) {
        this.currentValue = currentValue;
    }

    public TreeMap<UUID, PendingApprovalFieldDetails> getFieldDetails() {
        return fieldDetails;
    }

    public void setFieldDetails(TreeMap<UUID, PendingApprovalFieldDetails> fieldDetails) {
        if (this.fieldDetails == null) {
            this.fieldDetails = new TreeMap<>(new Comparator<UUID>() {
                @Override
                public int compare(UUID o1, UUID o2) {
                    return o2.compareTo(o1);
                }
            });
        }
        this.fieldDetails.putAll(fieldDetails);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingApprovalDetails)) return false;

        PendingApprovalDetails that = (PendingApprovalDetails) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(PendingApprovalDetails o) {
        return this.getName().compareTo(o.getName());
    }
}
