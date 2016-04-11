package org.sharedhealth.mci.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.web.service.PatientService;
import org.sharedhealth.mci.web.utils.UrlUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.lang.String.format;
import static org.sharedhealth.mci.web.config.MCIConfig.getSupportedRequestUris;

public class FeedController extends MciController {

    protected PatientService patientService;

    public FeedController(PatientService patientService, MCIProperties properties) {
        super(properties);
        this.patientService = patientService;
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
        String patientUrl = format("%s/patients/%s", buildPatientRequestUri(request), healthId);
        return UrlUtil.formServerUrl(request, patientUrl);
    }

    private String buildPatientRequestUri(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        List<String> supportedRequestUris = getSupportedRequestUris(getProperties().getApiVersion(),
                getProperties().isLatestApiVersion());

        for (String mapping : supportedRequestUris) {
            if (requestUri.startsWith(mapping)) {
                return mapping;
            }
        }
        // should never happen
        return "";
    }
}
