package org.sharedhealth.mci.web.controller;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.web.mapper.Feed;
import org.sharedhealth.mci.web.mapper.FeedEntry;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.sharedhealth.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.utils.DateUtil.fromIsoFormat;
import static org.sharedhealth.mci.web.utils.JsonConstants.LAST_MARKER;
import static org.sharedhealth.mci.web.utils.JsonConstants.SINCE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@RestController
@RequestMapping("/api/v1/feed")
public class UpdateFeedController {

    private static final Logger logger = LoggerFactory.getLogger(UpdateFeedController.class);

    private static final String FEED_TITLE = "Patients";
    private static final String ENTRY_TITLE = "Patient updates: ";
    private static final String ENTRY_CATEGORY = "patient";

    private PatientService patientService;


    @Autowired
    public UpdateFeedController(PatientService patientService) {
        this.patientService = patientService;
    }

    @RequestMapping(value = "/patients", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<Feed> findAllPatients(
            @RequestParam(value = SINCE, required = false) String since,
            @RequestParam(value = LAST_MARKER, required = false) String last,
            HttpServletRequest request) {

        final DeferredResult<Feed> deferredResult = new DeferredResult<>();

        Date date = isNotBlank(since) ? fromIsoFormat(since) : null;

        logger.debug("Find all patients  updated since [" + since + "] ");

        UUID lastMarker = StringUtils.isBlank(last) ? null : UUID.fromString(last);

        List<PatientUpdateLog> patients = patientService.findPatientsUpdatedSince(date, lastMarker);

        deferredResult.setResult(buildFeedResponse(patients, request));

        return deferredResult;
    }

    Feed buildFeedResponse(List<PatientUpdateLog> patients, HttpServletRequest request) {
        try {
            Feed feed = new Feed();
            feed.setTitle(FEED_TITLE);
            feed.setFeedUrl(buildFeedUrl(request));
            feed.setPrevUrl(null);
            feed.setNextUrl(buildNextUrl(patients, request));
            feed.setEntries(buildFeedEntries(patients, request));
            return feed;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildFeedUrl(HttpServletRequest request) {
        StringBuffer feedUrl = request.getRequestURL();
        String queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            feedUrl.append("?").append(queryString);
        }
        return feedUrl.toString();
    }

    private String buildNextUrl(List<PatientUpdateLog> patients, HttpServletRequest request) throws UnsupportedEncodingException {
        if (isEmpty(patients)) {
            return null;
        }

        PatientUpdateLog lastPatient = patients.get(patients.size() - 1);

        return fromUriString(request.getRequestURL().toString())
                .queryParam(LAST_MARKER, lastPatient.getEventId())
                .build().toString();
    }

    private List<FeedEntry> buildFeedEntries(List<PatientUpdateLog> patients, HttpServletRequest request) {
        if (isEmpty(patients)) {
            return emptyList();
        }
        List<FeedEntry> entries = new ArrayList<>();
        for (PatientUpdateLog patient : patients) {
            FeedEntry entry = new FeedEntry();
            entry.setId(patient.getEventId());
            entry.setPublishedDate(patient.getEventTimeAsString());
            entry.setTitle(ENTRY_TITLE + patient.getHealthId());
            entry.setLink(buildPatientLink(patient.getHealthId(), request));
            entry.setCategories(new String[]{ENTRY_CATEGORY});
            entry.setContent(patient);
            entries.add(entry);
        }
        return entries;
    }

    private String buildPatientLink(String healthId, HttpServletRequest request) {
        return String.format("%s://%s:%s/%s/%s", request.getScheme(), request.getServerName(), request.getServerPort(),
                "api/v1/patients", healthId);
    }
}
