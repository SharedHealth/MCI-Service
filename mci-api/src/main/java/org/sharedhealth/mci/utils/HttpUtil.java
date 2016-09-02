package org.sharedhealth.mci.utils;

import org.sharedhealth.mci.domain.config.MCIProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class HttpUtil {

    public static final String CLIENT_ID_KEY = "client_id";
    public static final String AUTH_TOKEN_KEY = "X-Auth-Token";
    public static final String FROM_KEY = "From";
    public static final String EMAIL_KEY = "email";
    public static final String PASSWORD_KEY = "password";
    public static final String ACCESS_TOKEN_KEY = "access_token";

    public static final String AVAILABILITY_KEY = "availability";
    public static final String REASON_KEY = "reason";

    public static HttpHeaders getHrmIdentityHeaders(MCIProperties mciProperties) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID_KEY, mciProperties.getIdpClientId());
        httpHeaders.add(AUTH_TOKEN_KEY, mciProperties.getIdpAuthToken());
        return httpHeaders;
    }

    public static HttpEntity getHrmSinginEntity(MCIProperties mciProperties) {
        HttpHeaders headers = getHrmIdentityHeaders(mciProperties);
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        valueMap.add(EMAIL_KEY, mciProperties.getIdpClientEmail());
        valueMap.add(PASSWORD_KEY, mciProperties.getIdpClientPassword());
        return new HttpEntity<>(valueMap, headers);
    }

    public static HttpHeaders getHIDServiceHeaders(MCIProperties mciProperties){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(FROM_KEY, mciProperties.getIdpClientEmail());
        httpHeaders.add(CLIENT_ID_KEY, mciProperties.getIdpClientId());
        return httpHeaders;
    }
}
