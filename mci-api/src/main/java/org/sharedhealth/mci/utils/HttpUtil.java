package org.sharedhealth.mci.utils;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.springframework.http.HttpHeaders;

public class HttpUtil {

    public static final String CLIENT_ID_KEY = "client_id";
    public static final String AUTH_TOKEN_KEY = "X-Auth-Token";
    public static final String FROM_KEY = "From";

    public static HttpHeaders getHrmIdentityHeaders(MCIProperties mciProperties) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID_KEY, mciProperties.getIdpClientId());
        httpHeaders.add(AUTH_TOKEN_KEY, mciProperties.getIdpAuthToken());
        return httpHeaders;

    }
}
