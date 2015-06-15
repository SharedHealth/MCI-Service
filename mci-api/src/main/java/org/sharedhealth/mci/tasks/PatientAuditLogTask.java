package org.sharedhealth.mci.tasks;

import org.sharedhealth.mci.web.service.PatientAuditService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PatientAuditLogTask {

    private static final Logger logger = getLogger(PatientAuditLogTask.class);

    @Autowired
    PatientAuditService auditService;

    @Scheduled(fixedDelayString = "${AUDIT_LOG_SYNC_DELAY}")
    public void execute() {
        try {
            logger.debug("Syncing audit log.");
            auditService.sync();

        } catch (Exception e) {
            logger.error("Failed to sync audit log.", e);
        }
    }
}
