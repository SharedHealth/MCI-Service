package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.TreeMap;
import java.util.UUID;

import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PendingApprovalDetails implements Comparable<PendingApprovalDetails> {

    @JsonProperty(FIELD_NAME)
    private String name;

    @JsonProperty(CURRENT_VALUE)
    private String currentValue;

    @JsonProperty(FIELD_DETAILS)
    TreeMap<UUID, PendingApprovalFieldDetails> details;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public TreeMap<UUID, PendingApprovalFieldDetails> getDetails() {
        return details;
    }

    public void setDetails(TreeMap<UUID, PendingApprovalFieldDetails> details) {
        this.details = details;
    }

    public void addDetails(TreeMap<UUID, PendingApprovalFieldDetails> details) {
        if (this.details == null) {
            this.details = new TreeMap<>();
        }
        this.details.putAll(details);
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
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int compareTo(PendingApprovalDetails o) {
        return this.getName().compareTo(o.getName());
    }
}
