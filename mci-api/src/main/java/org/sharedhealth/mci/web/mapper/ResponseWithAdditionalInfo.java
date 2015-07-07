package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public interface ResponseWithAdditionalInfo {

    @JsonIgnore
    UUID getModifiedAt();
}
