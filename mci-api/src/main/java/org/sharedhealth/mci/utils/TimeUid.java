package org.sharedhealth.mci.utils;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

public class TimeUid {

    public static UUID fromString(String uid) {
        try {
            return StringUtils.isBlank(uid) ? null : UUID.fromString(uid);
        } catch (Exception e) {
            return null;
        }
    }
}