package org.sharedhealth.mci.tasks;

import org.sharedhealth.mci.web.service.PatientHealthIdService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PatientHealthIdBlockReplenishTask {

    private static final Logger logger = getLogger(PatientHealthIdBlockReplenishTask.class);
    PatientHealthIdService patientHealthIdService;

    @Autowired
    public PatientHealthIdBlockReplenishTask(PatientHealthIdService patientHealthIdService) {
        this.patientHealthIdService = patientHealthIdService;
    }

    @Scheduled(initialDelayString = "${HEALTH_ID_REPLENISH_INITIAL_DELAY}", fixedDelayString = "${HEALTH_ID_REPLENISH_DELAY}")
    public void execute() {
        try {
            patientHealthIdService.replenishIfNeeded();
        } catch (Exception e) {
            logger.error("Failed to replenish health Ids", e);
        }
    }
}
