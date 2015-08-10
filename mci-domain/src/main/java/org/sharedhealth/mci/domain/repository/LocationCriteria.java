package org.sharedhealth.mci.domain.repository;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;


@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationCriteria extends PaginationQuery {
    @JsonProperty("parent")
    private String parent;

    public LocationCriteria() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationCriteria)) return false;
        if (!super.equals(o)) return false;

        LocationCriteria that = (LocationCriteria) o;

        if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LocationCriteria{" +
                "parent='" + parent + '\'' +
                '}';
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
