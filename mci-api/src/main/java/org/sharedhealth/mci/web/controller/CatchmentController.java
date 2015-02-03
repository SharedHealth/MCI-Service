package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.utils.TimeUid;
import org.sharedhealth.mci.validation.group.RequiredOnUpdateGroup;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.validation.groups.Default;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.utils.DateUtil.fromIsoFormat;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@RestController
@RequestMapping("/api/v1/catchments")
public class CatchmentController extends FeedController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private static final String FEED_TITLE = "Patients";
    private static final String ENTRY_TITLE = "Patient in Catchment: ";
    private static final String ENTRY_CATEGORY = "patient";

    @Autowired
    public CatchmentController(PatientService patientService) {
        super(patientService);
    }

    @RequestMapping(value = "/{catchmentId}/approvals", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findPendingApprovalList(
            @PathVariable String catchmentId,
            @RequestParam(value = AFTER, required = false) UUID after,
            @RequestParam(value = BEFORE, required = false) UUID before) {

        logger.debug("Find list of pending approvals.");
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        Catchment catchment = new Catchment(catchmentId);
        List<PendingApprovalListResponse> response = patientService.findPendingApprovalList(catchment, after, before);

        MCIMultiResponse mciMultiResponse;
        if (response != null) {
            mciMultiResponse = new MCIMultiResponse(response, null, OK);
        } else {
            mciMultiResponse = new MCIMultiResponse(emptyList(), null, OK);
        }
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));
        return deferredResult;
    }

    @RequestMapping(value = "/{catchmentId}/approvals/{healthId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findPendingApprovalDetails(
            @PathVariable String catchmentId,
            @PathVariable String healthId) {

        logger.debug("Find list of pending approval details. Health ID : " + healthId);
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        Catchment catchment = new Catchment(catchmentId);
        TreeSet<PendingApproval> response = patientService.findPendingApprovalDetails(healthId, catchment);

        MCIMultiResponse mciMultiResponse;
        if (response != null) {
            mciMultiResponse = new MCIMultiResponse(response, null, OK);
        } else {
            mciMultiResponse = new MCIMultiResponse(emptyList(), null, OK);
        }
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));

        return deferredResult;
    }

    @RequestMapping(value = "/{catchmentId}/approvals/{healthId}", method = PUT, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIResponse>> acceptPendingApprovals(
            @PathVariable String catchmentId,
            @PathVariable String healthId,
            @Validated({RequiredOnUpdateGroup.class, Default.class}) @RequestBody PatientData patient,
            BindingResult bindingResult) {

        logger.debug("Accepting pending approvals. Health ID : " + healthId);
        return processPendingApprovals(new Catchment(catchmentId), healthId, patient, bindingResult, true);
    }

    @RequestMapping(value = "/{catchmentId}/approvals/{healthId}", method = DELETE, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIResponse>> rejectPendingApprovals(
            @PathVariable String catchmentId,
            @PathVariable String healthId,
            @Validated({RequiredOnUpdateGroup.class, Default.class}) @RequestBody PatientData patient,
            BindingResult bindingResult) {

        logger.debug("Accepting pending approvals. Health ID : " + healthId);
        return processPendingApprovals(new Catchment(catchmentId), healthId, patient, bindingResult, false);
    }

    private DeferredResult<ResponseEntity<MCIResponse>> processPendingApprovals(
            Catchment catchment, String healthId, PatientData patient, BindingResult bindingResult, boolean shouldAccept) {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        patient.setHealthId(healthId);
        String hid = patientService.processPendingApprovals(patient, catchment, shouldAccept);

        MCIResponse mciResponse = new MCIResponse(hid, ACCEPTED);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    @RequestMapping(value = "/{catchmentId}/patients", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<Feed> findAllPatients(
            @PathVariable String catchmentId,
            @RequestParam(value = SINCE, required = false) String since,
            @RequestParam(value = LAST_MARKER, required = false) String last,
            @RequestHeader(FACILITY_ID) String facilityId,
            HttpServletRequest request) {

        UUID lastMarker = TimeUid.fromString(last);

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
}
