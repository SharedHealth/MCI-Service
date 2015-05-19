package org.sharedhealth.mci.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.utils.TimeUid;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.mapper.Feed;
import org.sharedhealth.mci.web.mapper.FeedEntry;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.sharedhealth.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.sharedhealth.mci.utils.DateUtil.parseDate;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.EVENT_TYPE_CREATED;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.EVENT_TYPE_UPDATED;
import static org.sharedhealth.mci.web.utils.JsonConstants.LAST_MARKER;
import static org.sharedhealth.mci.web.utils.JsonConstants.SINCE;
import static org.springframework.http.MediaType.APPLICATION_ATOM_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@RestController
@RequestMapping("/feed")
public class UpdateFeedController extends FeedController {

    private static final Logger logger = LoggerFactory.getLogger(UpdateFeedController.class);

    private static final String FEED_TITLE = "Patients";
    private static final String ENTRY_TITLE = "Patient updates: ";
    private static final String CATEGORY_PATIENT = "patient";
    private static final String CATEGORY_CREATE = "create";
    private static final String CATEGORY_UPDATE = "update";

    @Autowired
    public UpdateFeedController(PatientService patientService, MCIProperties properties) {
        super(patientService, properties);
    }

    @PreAuthorize("hasAnyRole('ROLE_FACILITY', 'ROLE_SHR System Admin')")
    @RequestMapping(value = "/patients", method = GET, produces = {APPLICATION_JSON_VALUE, APPLICATION_ATOM_XML_VALUE})
    public Feed findAllPatients(
            @RequestParam(value = SINCE, required = false) String since,
            @RequestParam(value = LAST_MARKER, required = false) String last,
            HttpServletRequest request) {
        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, String.format("Find all patients  updated since [%s] given last marker [%s]", since, last));

        Date date = isNotBlank(since) ? parseDate(since) : null;
        logger.debug("Find all patients  updated since [" + since + "] ");
        UUID lastMarker = TimeUid.fromString(last);
        List<PatientUpdateLog> patients = patientService.findPatientsUpdatedSince(date, lastMarker);
        return buildFeedResponse(patients, request);
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

    private String buildNextUrl(List<PatientUpdateLog> patients, HttpServletRequest request)
            throws UnsupportedEncodingException {
        if (isEmpty(patients)) {
            return null;
        }
        PatientUpdateLog lastPatient = patients.get(patients.size() - 1);
        return fromUriString(buildUrl(request))
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
            entry.setTitle(CATEGORY_PATIENT + StringUtils.SPACE + getEventType(patient.getEventType()) + ": " + patient.getHealthId());
            entry.setLink(buildPatientLink(patient.getHealthId(), request));
            entry.setEventType(getEventType(patient.getEventType()));
            entry.setCategories(buildCategoryArray(patient));
            entry.setContent(patient);
            entries.add(entry);
        }
        return entries;
    }

    private String getEventType(String eventType) {
        if (isBlank(eventType) || eventType.equalsIgnoreCase(EVENT_TYPE_UPDATED)) {
            return EVENT_TYPE_UPDATED;
        }
        return EVENT_TYPE_CREATED;
    }

    private String[] buildCategoryArray(PatientUpdateLog patient) {
        if (StringUtils.isBlank(patient.getChangeSet())) {
            return new String[]{CATEGORY_PATIENT};
        }
        String updateCategory;
        if (getEventType(patient.getEventType()).equalsIgnoreCase(EVENT_TYPE_CREATED)) {
            updateCategory = CATEGORY_CREATE;
        } else {
            updateCategory = CATEGORY_UPDATE + ":" + join(patient.getChangeSetMap().keySet().toArray(), ",");
        }
        return new String[]{CATEGORY_PATIENT, updateCategory};
    }
}
