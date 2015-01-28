package org.sharedhealth.mci.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.Feed;
import org.sharedhealth.mci.web.mapper.FeedEntry;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.utils.DateUtil.fromIsoFormat;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@RestController
@RequestMapping("/api/v1/catchments")
public class CatchmentController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private static final String FEED_TITLE = "Patients";
    private static final String ENTRY_TITLE = "Patient in Catchment: ";
    private static final String ENTRY_CATEGORY = "patient";

    private PatientService patientService;


    @Autowired
    public CatchmentController(PatientService patientService) {
        this.patientService = patientService;
    }

    @RequestMapping(value = "/{catchmentId}/patients", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<Feed> findAllPatients(
            @PathVariable String catchmentId,
            @RequestParam(value = SINCE, required = false) String since,
            @RequestParam(value = LAST_MARKER, required = false) UUID lastMarker,
            @RequestHeader(FACILITY_ID) String facilityId,
            HttpServletRequest request) {

        Catchment catchment = new Catchment(catchmentId);
        logger.debug(format("Find all patients by catchment. Catchment ID: %s, since: %s, last marker: %s", catchment, since, lastMarker));
        final DeferredResult<Feed> deferredResult = new DeferredResult<>();

        Date date = isNotBlank(since) ? fromIsoFormat(since) : null;
        List<PatientData> patients = patientService.findAllByCatchment(catchment, date, lastMarker, facilityId);

        deferredResult.setResult(buildFeedResponse(patients, request));
        return deferredResult;
    }

    Feed buildFeedResponse(List<PatientData> patients, HttpServletRequest request) {
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

    private String buildNextUrl(List<PatientData> patients, HttpServletRequest request) throws UnsupportedEncodingException {
        if (isEmpty(patients)) {
            return null;
        }
        PatientData lastPatient = patients.get(patients.size() - 1);
        String since = encode(lastPatient.getUpdatedAtAsString(), "UTF-8");
        String lastMarker = encode(lastPatient.getUpdatedAt().toString(), "UTF-8");

        return fromUriString(request.getRequestURL().toString())
                .queryParam(SINCE, since)
                .queryParam(LAST_MARKER, lastMarker)
                .build().toString();
    }

    private List<FeedEntry> buildFeedEntries(List<PatientData> patients, HttpServletRequest request) {
        if (isEmpty(patients)) {
            return emptyList();
        }
        List<FeedEntry> entries = new ArrayList<>();
        for (PatientData patient : patients) {
            FeedEntry entry = new FeedEntry();
            String healthId = patient.getHealthId();
            entry.setId(patient.getUpdatedAt());
            entry.setPublishedDate(patient.getUpdatedAtAsString());
            entry.setTitle(ENTRY_TITLE + patient.getHealthId());
            entry.setLink(buildPatientLink(healthId, request));
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
