package org.sharedhealth.mci.tasks;

import org.sharedhealth.mci.web.service.DuplicatePatientFeedService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DuplicatePatientFeedTask {

    private static final Logger logger = getLogger(DuplicatePatientFeedTask.class);

    private DuplicatePatientFeedService duplicatePatientFeedService;

    @Autowired
    public DuplicatePatientFeedTask(DuplicatePatientFeedService duplicatePatientFeedService) {
        this.duplicatePatientFeedService = duplicatePatientFeedService;
    }

    @Scheduled(initialDelayString = "${DUPLICATE_PATIENT_FEED_INITIAL_DELAY}", fixedDelayString = "${DUPLICATE_PATIENT_FEED_DELAY}")
    public void execute() {
        try {
            duplicatePatientFeedService.processDuplicatePatients();
        } catch (Exception e) {
            logger.error("Failed process duplicate patient feed.", e);
        }
    }
}
