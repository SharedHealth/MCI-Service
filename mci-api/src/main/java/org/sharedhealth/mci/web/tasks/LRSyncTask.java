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

    public static final String LR_DIVISION_URI_PATH = "/list/division";
    public static final String LR_DISTRICT_URI_PATH = "/list/district";
    public static final String LR_UPAZILA_URI_PATH = "/list/upazila";
    public static final String LR_PAURASAVA_PATH = "/list/paurasava";
    public static final String LR_UNION_URI_PATH = "/list/union";
    public static final String LR_WARD_URI_PATH = "/list/ward";

    public static final String DIVISION_TYPE = "DIVISION";
    public static final String DISTRICT_TYPE = "DISTRICT";
    public static final String UPAZILA_TYPE = "UPAZILA";
    public static final String PAURASAVA_TYPE = "PAURASAVA";
    public static final String UNION_TYPE = "UNION";
    public static final String WARD_TYPE = "WARD";

    @Autowired
    LocationDataSync locationDataSync;

    @Scheduled(fixedDelayString = "${LR_SYNC_FIXED_DELAY}")
    public void syncDivision() {
        logger.info("Division Syncing start....");
        locationDataSync.syncLRData(LR_DIVISION_URI_PATH, DIVISION_TYPE);
    }

    @Scheduled(fixedDelayString = "${LR_SYNC_FIXED_DELAY}")
    public void syncDistrict() {
        logger.info("District Syncing start....");
        locationDataSync.syncLRData(LR_DISTRICT_URI_PATH, DISTRICT_TYPE);
    }

    @Scheduled(fixedDelayString = "${LR_SYNC_FIXED_DELAY}")
    public void syncUpazila() {
        logger.info("Upazila Syncing start....");
        locationDataSync.syncLRData(LR_UPAZILA_URI_PATH, UPAZILA_TYPE);
    }

    @Scheduled(fixedDelayString = "${LR_SYNC_FIXED_DELAY}")
    public void syncPaurasava() {
        logger.info("Paurasava Syncing start....");
        locationDataSync.syncLRData(LR_PAURASAVA_PATH, PAURASAVA_TYPE);
    }

    @Scheduled(fixedDelayString = "${LR_SYNC_FIXED_DELAY}")
    public void syncUnion() {
        logger.info("Union Syncing start....");
        locationDataSync.syncLRData(LR_UNION_URI_PATH, UNION_TYPE);
    }

    @Scheduled(fixedDelayString = "${LR_SYNC_FIXED_DELAY}")
    public void syncWard() {
        logger.info("Ward Syncing start....");
        locationDataSync.syncLRData(LR_WARD_URI_PATH, WARD_TYPE);
    }
}