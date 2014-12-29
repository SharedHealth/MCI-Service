package org.sharedhealth.mci.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import static org.sharedhealth.mci.web.utils.JsonConstants.FACILITY_ID;
import static org.sharedhealth.mci.web.utils.JsonConstants.FIELDS;

public class PendingApprovalRequest {
    @JsonProperty(FACILITY_ID)
    private String facilityId;

    @JsonProperty(FIELDS)
    private Map<String, Object> fields;

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }
}
