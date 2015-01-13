package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Facility {

    @JsonProperty("id")
    private String id;

    @JsonProperty("properties")
    protected FacilityProperties facilityProperties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FacilityProperties getFacilityProperties() {
        return this.facilityProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Facility)) return false;

        Facility facility = (Facility) o;

        if (facilityProperties != null ? !facilityProperties.equals(facility.facilityProperties) : facility.facilityProperties != null)
            return false;
        if (id != null ? !id.equals(facility.id) : facility.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (facilityProperties != null ? facilityProperties.hashCode() : 0);
        return result;
    }

    public List<Catchment> getCatchments() {

        if (facilityProperties == null) {
            return new ArrayList<>();
        }

        return facilityProperties.getCatchments();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class FacilityProperties {

        @JsonProperty("org_level")
        private String organizationLevel;

        private List<Catchment> catchments;

        public FacilityProperties() {
            this.catchments = new ArrayList<>();
        }

        public List<Catchment> getCatchments() {
            return catchments;
        }

        @JsonProperty("catchment")
        public void setCatchments(List<String> catchments) {
            if (isEmpty(catchments)) {
                return;
            }
            for (String catchment : catchments) {
                this.catchments.add(new Catchment(catchment));
            }
        }

        public String getOrganizationLevel() {
            return organizationLevel;
        }

        public void setOrganizationLevel(String organizationLevel) {
            this.organizationLevel = organizationLevel;
        }
    }
}
