package org.sharedhealth.mci.web.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacilityResponse {

    @JsonProperty("properties")
    protected FacilityProperties facilityProperties;
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FacilityProperties getFacilityProperties() {
        return this.facilityProperties;
    }

    public List<String> getCatchments() {

        if (facilityProperties == null) {
            return new ArrayList<>();
        }

        return facilityProperties.getCatchments();
    }

    public String getGeoCode() {

        if (facilityProperties == null) {
            return null;
        }

        return facilityProperties.getGeoCode();
    }

    public String getType() {

        if (facilityProperties == null) {
            return null;
        }

        return facilityProperties.getOrganizationType();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class FacilityProperties {

        @JsonProperty("org_level")
        private String organizationLevel;

        @JsonProperty("org_type")
        private String organizationType;

        private HashMap<String, String> locations;

        private String geoCode;

        @JsonProperty("catchment")
        private List<String> catchments;

        public FacilityProperties() {
            this.catchments = new ArrayList<>();
        }

        public List<String> getCatchments() {
            return catchments;
        }

        public void setCatchments(List<String> catchments) {
            this.catchments = catchments;
        }

        public String getOrganizationLevel() {
            return organizationLevel;
        }

        public void setOrganizationLevel(String organizationLevel) {
            this.organizationLevel = organizationLevel;
        }

        public String getGeoCode() {
            return geoCode;
        }

        public HashMap<String, String> getLocations() {
            return locations;
        }

        public void setLocations(HashMap<String, String> locations) {
            this.locations = locations;

            for (String location : locations.values()) {

                if (StringUtils.isBlank(location)) {
                    break;
                }

                this.geoCode = this.geoCode + location;
            }
        }

        public String getOrganizationType() {
            return organizationType;
        }

        public void setOrganizationType(String organizationType) {
            this.organizationType = organizationType;
        }
    }
}
