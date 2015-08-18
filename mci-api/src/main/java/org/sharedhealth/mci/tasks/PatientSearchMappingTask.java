package org.sharedhealth.mci.tasks;

import org.apache.log4j.Logger;
import org.sharedhealth.mci.web.service.PatientSearchMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PatientSearchMappingTask {
    private final Logger logger = Logger.getLogger(PatientSearchMappingTask.class);

    @Autowired
    private PatientSearchMappingService searchMappingService;

    @Scheduled(initialDelayString = "${SEARCH_MAPPING_TASK_INITIAL_DELAY}", fixedDelayString = "${SEARCH_MAPPING_TASK_DELAY}")
    public void map() {
        searchMappingService.map();
    }
}
