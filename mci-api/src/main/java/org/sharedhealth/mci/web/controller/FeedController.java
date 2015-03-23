package org.sharedhealth.mci.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;

public class FeedController {

    protected PatientService patientService;
    protected MCIProperties properties;

    public FeedController(PatientService patientService, MCIProperties properties) {
        this.patientService = patientService;
        this.properties = properties;
    }

    protected String buildFeedUrl(HttpServletRequest request) {
        StringBuilder feedUrl = new StringBuilder(buildUrl(request));
        String queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            feedUrl.append("?").append(queryString);
        }
        return feedUrl.toString();
    }

    protected String buildPatientLink(String healthId, HttpServletRequest request) {
        return String.format("%s://%s:%s/%s/%s", properties.getHttpScheme(), request.getServerName(),
                request.getServerPort(), "api/v1/patients", healthId);
    }

    protected String buildUrl(HttpServletRequest request) {
        return String.format("%s://%s:%s%s", properties.getHttpScheme(), request.getServerName(),
                request.getServerPort(), request.getRequestURI());
    }

    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

    protected UserInfo getUserInfo() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected void logAccessDetails(UserInfo userInfo, String action) {
        logger.info(String.format("ACCESS: USER=%s TYPE=%s ACTION=%s",
                userInfo.getProperties().getId(), userInfo.getProperties().getName(), action));
    }

}
