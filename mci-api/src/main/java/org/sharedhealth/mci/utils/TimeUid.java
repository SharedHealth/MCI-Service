package org.sharedhealth.mci.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class TimeUid {

    public static UUID fromString(String uid) {

        if(StringUtils.isBlank(uid)) {
            return null;
        }

        try {
            final UUID uuid = UUID.fromString(uid);
            return uuid.version() != 1 ? null : uuid;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}