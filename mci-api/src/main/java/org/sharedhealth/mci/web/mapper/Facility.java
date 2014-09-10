package org.sharedhealth.mci.web.mapper;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Facility {

    @JsonProperty("id")
    private String id;

    @JsonProperty("catchments")
    private List<String> catchments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public List<String> getCatchments() {
        return catchments;
    }

    public void setCatchments(List<String> catchments) {
        this.catchments = catchments;
    }
}
