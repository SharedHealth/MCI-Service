package org.sharedhealth.mci.web.tasks;

import org.apache.log4j.Logger;
import org.sharedhealth.mci.web.handler.LocationDataSync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Configuration
@EnableScheduling
public class LRSyncTask {

    private final Logger logger = Logger.getLogger(LRSyncTask.class);

    @Autowired
    LocationDataSync locationDataSync;

    @Scheduled(fixedDelayString = "${LR_SYNC_FIXED_DELAY}")
    public void execute() {
        try {
            logger.info("Syncing start....");
            locationDataSync.sync();

        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
}
