package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.domain.model.PatientAuditLog;
import org.sharedhealth.mci.domain.model.PatientAuditLogData;
import org.sharedhealth.mci.domain.model.PatientUpdateLog;
import org.sharedhealth.mci.domain.model.Requester;
import org.sharedhealth.mci.domain.repository.PatientAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class PatientAuditService {
    private static final Logger logger = LoggerFactory.getLogger(PatientAuditService.class);

    private PatientAuditRepository auditRepository;
    private RequesterService requesterService;

    @Autowired
    public PatientAuditService(PatientAuditRepository auditRepository,
                               RequesterService requesterService) {
        this.auditRepository = auditRepository;
        this.requesterService = requesterService;
    }

    public List<PatientAuditLogData> findByHealthId(String healthId) {
        logger.debug(String.format("Find audit log for patient: (%s)", healthId));
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

    List<PatientAuditLog> map(List<PatientUpdateLog> feeds) {
        List<PatientAuditLog> logs = new ArrayList<>();
        for (PatientUpdateLog feed : feeds) {
            PatientAuditLog log = PatientAuditLog.toPatientAuditLog(feed);
            logs.add(log);
        }
        return logs;
    }

}
