package org.sharedhealth.mci.web.tasks;

import org.apache.log4j.Logger;
import org.sharedhealth.mci.web.handler.LocationDataSync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Configuration
@EnableScheduling
public class LRSyncTask {

    private final Logger logger = Logger.getLogger(LRSyncTask.class);

    @Autowired
    LocationDataSync locationDataSync;

    @Value("${LR_SYNC_SCHEDULED_ENABLE}")
    private int enable;

    @Scheduled(cron = "${LR_SYNC_CRON_EXPRESSION}")
    public void execute() {
        try {
            logger.info("Syncing start....");
            if (enable == 1) {
                locationDataSync.sync();
            } else {
                logger.info("Syncing disabled here");
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
}
