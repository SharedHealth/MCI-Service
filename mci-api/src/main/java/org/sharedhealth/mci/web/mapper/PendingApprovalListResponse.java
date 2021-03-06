package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.domain.model.ResponseWithAdditionalInfo;

import java.util.UUID;

import static org.sharedhealth.mci.domain.constant.JsonConstants.*;

public class PendingApprovalListResponse implements ResponseWithAdditionalInfo {

    @JsonProperty(HID)
    private String healthId;

    @JsonProperty(GIVEN_NAME)
    private String givenName;

    @JsonProperty(SUR_NAME)
    private String surname;

    @JsonProperty(LAST_UPDATED)
    private UUID lastUpdated;

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public UUID getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(UUID lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public UUID getModifiedAt() {
        return getLastUpdated();
    }
}
