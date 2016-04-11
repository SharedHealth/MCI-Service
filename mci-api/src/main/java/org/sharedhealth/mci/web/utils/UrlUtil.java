package org.sharedhealth.mci.web.utils;

import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

public class UrlUtil {
    public static String formServerUrl(HttpServletRequest request, String uri) {
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromUriString(uri);
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme != null) {
            uriBuilder.scheme(scheme);
        } else {
            uriBuilder.scheme(request.getScheme());
        }
        uriBuilder.host(request.getServerName());
        int port = request.getServerPort();
        if (!(port == 80 || port == 443)) {
            uriBuilder.port(port);
        }
        return uriBuilder.build().toString();
    }
}
