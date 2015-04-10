package org.sharedhealth.mci.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.service.PatientService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.lang.String.format;
import static org.sharedhealth.mci.web.config.MCIConfig.getSupportedRequestUris;
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
        String patientUrl = format("%s/patients", buildPatientRequestUri(request));
        return format("%s%s/%s", buildServerUrl(request), patientUrl, healthId);
    }

    private String buildPatientRequestUri(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        List<String> supportedRequestUris = getSupportedRequestUris(properties.getApiVersion(),
                properties.isLatestApiVersion());

        for (String mapping : supportedRequestUris) {
            if (requestUri.startsWith(mapping)) {
                return mapping;
            }
        }
        // should never happen
        return "";
    }

    protected String buildUrl(HttpServletRequest request) {
        return format("%s%s", buildServerUrl(request), request.getRequestURI());
    }

    String buildServerUrl(HttpServletRequest request) {
        String url = properties.getServerUrl();
        String host = fromHttpUrl(url).build().getHost();
        if (host.equals(request.getServerName())) {
            return url;
        }
        return format("%s://%s:%s", request.getScheme(), request.getServerName(), request.getServerPort());
    }
}
