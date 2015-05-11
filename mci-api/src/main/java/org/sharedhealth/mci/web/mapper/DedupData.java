package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class DedupData {

    @JsonProperty(HID1)
    private String healthId1;

    @JsonProperty(HID2)
    private String healthId2;

    @JsonProperty(REASON)
    private String reason;

    @JsonProperty(CREATED_AT)
    private String createdAt;

}
