package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.infrastructure.persistence.PatientAuditRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientFeedRepository;
import org.sharedhealth.mci.web.mapper.PatientAuditLogData;
import org.sharedhealth.mci.web.mapper.Requester;
import org.sharedhealth.mci.web.model.PatientAuditLog;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Component
public class PatientAuditService {
    private static final Logger logger = LoggerFactory.getLogger(PatientAuditService.class);

    static final int UPDATE_LOG_LIMIT = 3;

    private PatientFeedRepository feedRepository;
    private PatientAuditRepository auditRepository;
    private RequesterService requesterService;

    @Autowired
    public PatientAuditService(PatientAuditRepository auditRepository,
                               PatientFeedRepository feedRepository,
                               RequesterService requesterService) {
        this.auditRepository = auditRepository;
        this.feedRepository = feedRepository;
        this.requesterService = requesterService;
    }

    public List<PatientAuditLogData> findByHealthId(String healthId) {
        logger.debug(String.format("Find audit log for patient: (%s)",healthId));
        List<PatientAuditLogData> logs = auditRepository.findByHealthId(healthId);
        populateRequesterDetails(logs);
        return logs;
    }

    private void populateRequesterDetails(List<PatientAuditLogData> logs) {
        for (PatientAuditLogData log : logs) {
            Map<String, Set<Requester>> requestedBy = log.getRequestedBy();
            if (requestedBy != null && requestedBy.size() > 0) {
                for (String fieldName : requestedBy.keySet()) {
                    requesterService.populateRequesterDetails(requestedBy.get(fieldName));
                }
            }
            requesterService.populateRequesterDetails(log.getApprovedBy());
        }
    }

    public void sync() {
        UUID marker = auditRepository.findLatestMarker();
        List<PatientUpdateLog> feeds = feedRepository.findPatientsUpdatedSince(marker, UPDATE_LOG_LIMIT);
        if (isNotEmpty(feeds)) {
            auditRepository.saveOrUpdate(map(feeds));
        }
    }

    List<PatientAuditLog> map(List<PatientUpdateLog> feeds) {
        List<PatientAuditLog> logs = new ArrayList<>();
        for (PatientUpdateLog feed : feeds) {
            PatientAuditLog log = new PatientAuditLog();
            log.setHealthId(feed.getHealthId());
            log.setEventId(feed.getEventId());
            log.setChangeSet(feed.getChangeSet());
            log.setRequestedBy(feed.getRequestedBy());
            log.setApprovedBy(feed.getApprovedBy());
            logs.add(log);
        }
        return logs;
    }
}
