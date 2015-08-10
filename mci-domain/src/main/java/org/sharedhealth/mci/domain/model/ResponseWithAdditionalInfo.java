package org.sharedhealth.mci.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public interface ResponseWithAdditionalInfo {

    @JsonIgnore
    UUID getModifiedAt();
}
