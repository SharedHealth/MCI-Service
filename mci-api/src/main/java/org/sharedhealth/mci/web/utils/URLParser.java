package org.sharedhealth.mci.web.utils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class URLParser {
    public static Map<String, String> parseURL(URL url) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            int index = pair.indexOf("=");
            String key = index > 0 ? pair.substring(0, index) : pair;
            if (!parameters.containsKey(key)) {
                String value = index > 0 && pair.length() > index + 1 ? pair.substring(index + 1) : null;
                parameters.put(key, value);
            }
        }
        return parameters;
    }

    public static String ensureEndsWithBackSlash(String value) {
        String trimmedValue = value.trim();
        if (!trimmedValue.endsWith("/")) {
            return trimmedValue + "/";
        } else {
            return trimmedValue;
        }
    }
}
