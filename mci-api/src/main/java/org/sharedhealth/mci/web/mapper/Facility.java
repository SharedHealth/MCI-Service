package org.sharedhealth.mci.web.mapper;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public List<String> getCatchments() {

        if(facilityProperties == null) {
            return new ArrayList<>();
        }

        return facilityProperties.getCatchments();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class FacilityProperties {

        @JsonProperty("org_level")
        private String organizaitonLevel;

        @JsonProperty("catchment")
        private List<String> catchments;

        public FacilityProperties(){}

        public List<String> getCatchments() {
            return catchments;
        }

        public void setCatchments(List<String> catchments) {
            this.catchments = catchments;
        }

        public String getOrganizaitonLevel() {
            return organizaitonLevel;
        }

        public void setOrganizaitonLevel(String organizaitonLevel) {
            this.organizaitonLevel = organizaitonLevel;
        }
    }
}
