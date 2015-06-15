package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.model.HealthId;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PatientHealthIdService {
    private static final Logger logger = getLogger(PatientHealthIdService.class);

    private Queue<HealthId> healthIds = new ConcurrentLinkedQueue<>();

    private HealthIdService healthIdService;
    private final MCIProperties mciProperties;

    @Autowired
    public PatientHealthIdService(HealthIdService healthIdService, MCIProperties mciProperties) {
        this.healthIdService = healthIdService;
        this.mciProperties = mciProperties;
    }

    public HealthId getNextHealthId() throws InterruptedException {
        logger.debug("get: block size :" + healthIds.size());
        logger.debug(" object id : " + this);
        return healthIds.remove();
    }

    public void putBackHealthId(HealthId healthId) {
        healthIds.add(healthId);
    }

    public void replenishIfNeeded() {
        logger.debug("replenish: block size :" + healthIds.size());
        logger.debug("replenish: object id : " + this);
        if (healthIds.size() < mciProperties.getHealthIdBlockSizeThreshold()) {
            healthIds.addAll(healthIdService.getNextBlock());
        }
    }

    public void markUsed(HealthId nextHealthId) {
        healthIdService.markUsed(nextHealthId);
    }

    public int getHealthIdBlockSize() {
        return healthIds.size();
    }

}
