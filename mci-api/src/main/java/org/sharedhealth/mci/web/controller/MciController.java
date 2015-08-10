package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.model.ResponseWithAdditionalInfo;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

public class MciController {
    private static final Logger logger = LoggerFactory.getLogger(MciController.class);

    private MCIProperties properties;

    public MciController() {
        properties = new MCIProperties();
    }

    public MciController(MCIProperties properties) {
        this.properties = properties;
    }

    protected UserInfo getUserInfo() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected void logAccessDetails(UserInfo userInfo, String action) {
        logger.info(String.format("ACCESS: USER=%s EMAIL=%s ACTION=%s",
                userInfo.getProperties().getId(), userInfo.getProperties().getEmail(), action));
    }

    protected MCIMultiResponse buildPaginatedResponse(HttpServletRequest request,
                                                      List<? extends ResponseWithAdditionalInfo> response,
                                                      UUID after, UUID before, int limit) {

        HashMap<String, String> additionalInfo = new HashMap<>();

        if (response.size() > 0) {
            if (after == null && before == null && response.size() > limit) {
                response = response.subList(0, limit);
                additionalInfo.put(NEXT, buildNextUrl(request, response.get(response.size() - 1).getModifiedAt()));
            } else if (after != null && before == null && response.size() > 0) {
                if (response.size() > limit) {
                    response = response.subList(0, limit);
                    additionalInfo.put(NEXT, buildNextUrl(request, response.get(response.size() - 1).getModifiedAt()));
                    additionalInfo.put(PREVIOUS, buildPreviousUrl(request, response.get(0).getModifiedAt()));
                } else {
                    additionalInfo.put(PREVIOUS, buildPreviousUrl(request, response.get(0).getModifiedAt()));
                }
            } else if (before != null && after == null) {
                if (response.size() > limit) {
                    response = response.subList(response.size() - limit, response.size());
                    additionalInfo.put(PREVIOUS, buildPreviousUrl(request, response.get(0).getModifiedAt()));
                    additionalInfo.put(NEXT, buildNextUrl(request, response.get(response.size() - 1).getModifiedAt()));
                } else {
                    additionalInfo.put(NEXT, buildNextUrl(request, response.get(response.size() - 1).getModifiedAt()));
                }
            }
        }
        return new MCIMultiResponse(response, additionalInfo, OK);
    }

    protected String buildNextUrl(HttpServletRequest request, UUID lastUUID) {
        return fromUriString(buildUrl(request))
                .queryParam(AFTER, lastUUID)
                .build().toString();
    }

    protected String buildPreviousUrl(HttpServletRequest request, UUID lastUUID) {
        return fromUriString(buildUrl(request))
                .queryParam(BEFORE, lastUUID)
                .build().toString();
    }

    protected String buildUrl(HttpServletRequest request) {
        return format("%s%s", buildServerUrl(request), request.getRequestURI());
    }

    protected String buildServerUrl(HttpServletRequest request) {
        String url = getProperties().getServerUrl();
        String host = fromHttpUrl(url).build().getHost();
        if (host.equals(request.getServerName())) {
            return url;
        }
        return format("%s://%s:%s", request.getScheme(), request.getServerName(), request.getServerPort());
    }

    protected MCIProperties getProperties() {
        return properties;
    }
}
