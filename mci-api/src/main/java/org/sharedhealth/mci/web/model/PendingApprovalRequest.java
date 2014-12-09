package org.sharedhealth.mci.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import static org.sharedhealth.mci.web.utils.JsonConstants.FACILITY_ID;
import static org.sharedhealth.mci.web.utils.JsonConstants.FIELDS;

public class PendingApprovalRequest {
    @JsonProperty(FACILITY_ID)
    private String facilityId;

    @JsonProperty(FIELDS)
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
