package org.sharedhealth.mci.web.mapper;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationCriteria extends PaginationQuery {
    @JsonProperty("parent")
    private String parent;

    public LocationCriteria() {
    }

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }


    @Override
    public String toString() {
        return "LocationCriteria{" +
                "parent='" + parent + '\'' + '}';
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return !StringUtils.isNotBlank(parent);
    }
}
