package org.sharedhealth.mci.web.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.exception.Forbidden;
import org.sharedhealth.mci.domain.exception.ValidationException;
import org.sharedhealth.mci.domain.model.Catchment;
import org.sharedhealth.mci.domain.model.MCIResponse;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.PendingApproval;
import org.sharedhealth.mci.domain.util.DateUtil;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.sharedhealth.mci.domain.validation.group.RequiredOnUpdateGroup;
import org.sharedhealth.mci.utils.TimeUid;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.mapper.Feed;
import org.sharedhealth.mci.web.mapper.FeedEntry;
import org.sharedhealth.mci.web.mapper.PendingApprovalListResponse;
import org.sharedhealth.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.domain.constant.JsonConstants.AFTER;
import static org.sharedhealth.mci.domain.constant.JsonConstants.BEFORE;
import static org.sharedhealth.mci.domain.constant.JsonConstants.LAST_MARKER;
import static org.sharedhealth.mci.domain.constant.JsonConstants.SINCE;
import static org.sharedhealth.mci.domain.util.DateUtil.convertToDateStringIsoMillisFormat;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;
import static org.sharedhealth.mci.web.infrastructure.security.UserProfile.ADMIN_TYPE;
import static org.sharedhealth.mci.web.infrastructure.security.UserProfile.FACILITY_TYPE;
import static org.sharedhealth.mci.web.infrastructure.security.UserProfile.PROVIDER_TYPE;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_ATOM_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@RestController
@RequestMapping("/catchments")
public class CatchmentController extends FeedController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private static final String FEED_TITLE = "Patients";
    private static final String ENTRY_TITLE = "Patient in Catchment: ";
    private static final String ENTRY_CATEGORY = "patient";

    @Autowired
    public CatchmentController(PatientService patientService, MCIProperties properties) {
        super(patientService, properties);
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Approver')")
    @RequestMapping(value = "/{catchmentId}/approvals", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findPendingApprovalList(
            @PathVariable String catchmentId,
            @RequestParam(value = AFTER, required = false) UUID after,
            @RequestParam(value = BEFORE, required = false) UUID before,
            HttpServletRequest request) {

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, String.format("Find list of pending approvals for catchment %s", catchmentId));

        logger.debug("Find list of pending approvals.");
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        if (!userInfo.getProperties().hasCatchmentForProfileType(catchmentId, asList(ADMIN_TYPE))) {
            deferredResult.setErrorResult(new Forbidden
                    (format("Access is denied to user %s for catchment %s",
                            userInfo.getProperties().getId(), catchmentId)));
            logger.error(format("Access is denied to user %s for catchment %s", userInfo.getProperties().getEmail(), catchmentId));
            return deferredResult;
        }

        Catchment catchment = new Catchment(catchmentId);
        int limit = patientService.getPerPageMaximumLimit() + 1;
        List<PendingApprovalListResponse> response = patientService.findPendingApprovalList(catchment, after, before, limit);

        MCIMultiResponse mciMultiResponse;
        if (response != null) {
            mciMultiResponse = buildPaginatedResponse(request, response, after, before, patientService.getPerPageMaximumLimit());
        } else {
            mciMultiResponse = new MCIMultiResponse(emptyList(), null, OK);
        }
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));
        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Approver')")
    @RequestMapping(value = "/{catchmentId}/approvals/{healthId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findPendingApprovalDetails(
            @PathVariable String catchmentId,
            @PathVariable String healthId) {

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, String.format("Find list of pending approval details for patient (Health Id) : %s, catchment %s",
                healthId, catchmentId));

        logger.debug("Find list of pending approval details. Health ID : " + healthId);

        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        if (!userInfo.getProperties().hasCatchmentForProfileType(catchmentId, asList(ADMIN_TYPE))) {
            deferredResult.setErrorResult(new Forbidden
                    (format("Access is denied to user %s for catchment %s",
                            userInfo.getProperties().getId(), catchmentId)));
            logger.error(format("Access is denied to user %s for catchment %s", userInfo.getProperties().getEmail(), catchmentId));
            return deferredResult;
        }

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

    @PreAuthorize("hasAnyRole('ROLE_MCI Approver')")
    @RequestMapping(value = "/{catchmentId}/approvals/{healthId}", method = PUT, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIResponse>> acceptPendingApprovals(
            @PathVariable String catchmentId,
            @PathVariable String healthId,
            @Validated({RequiredOnUpdateGroup.class, Default.class}) @RequestBody PatientData patient,
            BindingResult bindingResult) {

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, String.format("Accepting pending approval for patient (Health Id) : %s, catchment: %s",
                healthId, catchmentId));

        UserInfo.UserInfoProperties properties = userInfo.getProperties();
        patient.setRequester(properties.getFacilityId(), properties.getProviderId(),
                properties.getAdminId(), properties.getName());

        logger.info("Accepting pending approvals. Health ID : " + healthId);
        return processPendingApprovals(catchmentId, healthId, patient, userInfo, bindingResult, true);
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Approver')")
    @RequestMapping(value = "/{catchmentId}/approvals/{healthId}", method = DELETE, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIResponse>> rejectPendingApprovals(
            @PathVariable String catchmentId,
            @PathVariable String healthId,
            @Validated({RequiredOnUpdateGroup.class, Default.class}) @RequestBody PatientData patient,
            BindingResult bindingResult) {

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, String.format("Rejecting pending approval for patient (Health Id) : %s, catchment: %s",
                healthId, catchmentId));

        UserInfo.UserInfoProperties properties = userInfo.getProperties();
        patient.setRequester(properties.getFacilityId(), properties.getProviderId(),
                properties.getAdminId(), properties.getName());

        logger.info("Rejecting pending approvals. Health ID : " + healthId);
        return processPendingApprovals(catchmentId, healthId, patient, userInfo, bindingResult, false);
    }

    @PreAuthorize("hasAnyRole('ROLE_PROVIDER', 'ROLE_FACILITY', 'ROLE_SHR System Admin')")
    @RequestMapping(value = "/{catchmentId}/patients", method = GET, produces = {APPLICATION_JSON_VALUE, APPLICATION_ATOM_XML_VALUE})
    public DeferredResult<Feed> findAllPatients(
            @PathVariable String catchmentId,
            @RequestParam(value = SINCE, required = false) String since,
            @RequestParam(value = LAST_MARKER, required = false) String last,
            HttpServletRequest request) {

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, String.format("Find all patients by catchment: %s", catchmentId));

        UUID lastMarker = TimeUid.fromString(last);

        final DeferredResult<Feed> deferredResult = new DeferredResult<>();
        if (!userInfo.getProperties().hasCatchmentForProfileType(catchmentId, asList(FACILITY_TYPE, PROVIDER_TYPE))) {
            deferredResult.setErrorResult(new Forbidden
                    (format("Access is denied to user %s for catchment %s",
                            userInfo.getProperties().getEmail(), catchmentId)));
            logger.error(format("Access is denied to user %s for catchment %s", userInfo.getProperties().getEmail(), catchmentId));
            return deferredResult;
        }
        Catchment catchment = new Catchment(catchmentId);
        logger.debug(format("Find all patients by catchment. Catchment ID: %s", catchment));

        Date date = isNotBlank(since) ? parseDate(since) : null;
        List<Map<String, Object>> catchmentEvents = patientService.findAllByCatchment(catchment, date, lastMarker);

        deferredResult.setResult(buildFeedResponse(catchmentEvents, request));
        return deferredResult;
    }

    private DeferredResult<ResponseEntity<MCIResponse>> processPendingApprovals(
            String catchmentId, String healthId, PatientData patient, UserInfo userInfo,
            BindingResult bindingResult, boolean shouldAccept) {

        if (bindingResult.hasErrors()) {
            logger.error("ValidationException while processing pending approvals");
            throw new ValidationException(bindingResult);
        }

        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        if (!userInfo.getProperties().hasCatchmentForProfileType(catchmentId, asList(ADMIN_TYPE))) {
            deferredResult.setErrorResult(new Forbidden
                    (format("Access is denied to user %s for catchment %s",
                            userInfo.getProperties().getId(), catchmentId)));
            return deferredResult;
        }


        Catchment catchment = new Catchment(catchmentId);
        patient.setHealthId(healthId);
        logger.info(format("process pending approval for healthId: %s and for catchment: %s", healthId, catchment
                .toString()));
        String hid = patientService.processPendingApprovals(patient, catchment, shouldAccept);

        MCIResponse mciResponse = new MCIResponse(hid, ACCEPTED);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    Feed buildFeedResponse(List<Map<String, Object>> catchmentEvents, HttpServletRequest request) {
        try {
            Feed feed = new Feed();
            feed.setTitle(FEED_TITLE);
            feed.setFeedUrl(buildFeedUrl(request));
            feed.setPrevUrl(null);
            String nextUrl = CollectionUtils.isEmpty(catchmentEvents) ? null : buildNextUrl(catchmentEvents.get(catchmentEvents.size() - 1), request);
            feed.setNextUrl(nextUrl);
            feed.setEntries(buildFeedEntries(catchmentEvents, request));
            return feed;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildNextUrl(Map<String, Object> lastCatchmentEvent, HttpServletRequest request) throws UnsupportedEncodingException {
        UUID lastEventId = (UUID) lastCatchmentEvent.get("eventId");
        String since = encode(DateUtil.toIsoMillisFormat(convertToDateStringIsoMillisFormat(lastEventId)), "UTF-8");
        String lastMarker = encode(lastEventId.toString(), "UTF-8");

        return fromUriString(buildUrl(request))
                .queryParam(SINCE, since)
                .queryParam(LAST_MARKER, lastMarker)
                .build().toString();
    }

    private List<FeedEntry> buildFeedEntries(List<Map<String, Object>> catchmentEvents, HttpServletRequest request) {
        if (isEmpty(catchmentEvents)) {
            return emptyList();
        }
        List<FeedEntry> entries = new ArrayList<>();
        for (Map<String, Object> catchmentEvent : catchmentEvents) {
            FeedEntry entry = new FeedEntry();
            PatientData patient = (PatientData) catchmentEvent.get("patientData");
            String healthId = patient.getHealthId();
            UUID eventId = (UUID) catchmentEvent.get("eventId");
            entry.setId(eventId);
            entry.setPublishedDate(DateUtil.toIsoMillisFormat(TimeUuidUtil.getDateFromUUID(eventId)));
            entry.setTitle(ENTRY_TITLE + healthId);
            entry.setLink(buildPatientLink(healthId, request));
            entry.setCategories(new String[]{ENTRY_CATEGORY});
            entry.setContent(patient);
            entries.add(entry);
        }
        return entries;
    }
}
