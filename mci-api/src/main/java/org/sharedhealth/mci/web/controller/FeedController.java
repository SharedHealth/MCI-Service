package org.sharedhealth.mci.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.web.service.PatientService;

public class FeedController {

    protected PatientService patientService;

    public FeedController(PatientService patientService) {
        this.patientService = patientService;
    }

    protected String buildFeedUrl(HttpServletRequest request) {
        StringBuffer feedUrl = request.getRequestURL();
        String queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            feedUrl.append("?").append(queryString);
        }
        return feedUrl.toString();
    }

    protected String buildPatientLink(String healthId, HttpServletRequest request) {
        return String.format("%s://%s:%s/%s/%s", request.getScheme(), request.getServerName(), request.getServerPort(),
                "api/v1/patients", healthId);
    }
}
