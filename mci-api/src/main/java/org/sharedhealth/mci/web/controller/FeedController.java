package org.sharedhealth.mci.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.service.PatientService;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

public class FeedController extends MciController {

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
        return String.format("%s/%s/%s", buildServerUrl(request), "api/v1/patients", healthId);
    }

    protected String buildUrl(HttpServletRequest request) {
        return String.format("%s%s", buildServerUrl(request), request.getRequestURI().toString());
    }

    String buildServerUrl(HttpServletRequest request) {
        String[] urls = properties.getServerUrls().split(",");
        for (String url : urls) {
            url = url.trim();
            String host = fromHttpUrl(url).build().getHost();
            if (host.equals(request.getServerName())) {
                return url;
            }
        }
        return urls[0];
    }
}
