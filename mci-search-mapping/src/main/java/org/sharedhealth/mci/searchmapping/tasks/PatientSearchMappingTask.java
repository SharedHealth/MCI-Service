package org.sharedhealth.mci.searchmapping.tasks;

import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.searchmapping.services.PatientSearchMappingService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PatientSearchMappingTask {
    private static final Logger logger = getLogger(PatientSearchMappingTask.class);

    @Autowired
    private PatientSearchMappingService searchMappingService;

    @Autowired
    private MCIProperties mciProperties;

    @Scheduled(initialDelayString = "${SEARCH_MAPPING_TASK_INITIAL_DELAY}", fixedDelayString = "${SEARCH_MAPPING_TASK_DELAY}")
    public void map() {
        if (!mciProperties.getIsMCIMasterNode()) return;
        logger.debug("Executing patient search mapping task.");
        searchMappingService.map();
        searchMappingService.mapFailedEvents();
    }
}
