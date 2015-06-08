package org.sharedhealth.mci.web.tasks;

import org.sharedhealth.mci.web.service.PatientHealthIdService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableScheduling
public class PatientHealthIdBlockReplenishTask {

    private static final Logger logger = getLogger(PatientHealthIdBlockReplenishTask.class);

    @Autowired
    PatientHealthIdService patientHealthIdService;

    @Scheduled(initialDelayString = "${HEALTH_ID_REPLENISH_INITIAL_DELAY}", fixedDelayString = "${HEALTH_ID_REPLENISH_DELAY}")
    public void execute() {
        try {
            logger.debug("Replenishing Health Ids, if needed..");
            patientHealthIdService.replenishIfNeeded();
        } catch (Exception e) {
            logger.error("Failed to replenish health Ids", e);
        }
    }
}
