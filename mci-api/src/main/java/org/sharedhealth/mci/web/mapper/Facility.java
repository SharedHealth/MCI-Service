package org.sharedhealth.mci.web.mapper;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
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
