package org.sharedhealth.mci.deduplication.task;

import org.sharedhealth.mci.deduplication.service.DuplicatePatientFeedService;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DuplicatePatientFeedTask {

    private static final Logger logger = getLogger(DuplicatePatientFeedTask.class);

    private DuplicatePatientFeedService duplicatePatientFeedService;
    private MCIProperties mciProperties;

    @Autowired
    public DuplicatePatientFeedTask(DuplicatePatientFeedService duplicatePatientFeedService, MCIProperties mciProperties) {
        this.duplicatePatientFeedService = duplicatePatientFeedService;
        this.mciProperties = mciProperties;
    }

    @Scheduled(initialDelayString = "${DUPLICATE_PATIENT_FEED_INITIAL_DELAY}", fixedDelayString = "${DUPLICATE_PATIENT_FEED_DELAY}")
    public void execute() {
        if (!mciProperties.getIsMCIMasterNode()) return;
        try {
            logger.debug("Executing duplicate patient feed task.");
            duplicatePatientFeedService.processDuplicatePatients();
        } catch (Exception e) {
            logger.error("Failed process duplicate patient feed.", e);
        }
    }
}
