package org.sharedhealth.mci.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Approval {
    @JsonProperty("facility_id")
    private String facilityId;

    @JsonProperty("fields")
    private Map<String, String> fields;

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
