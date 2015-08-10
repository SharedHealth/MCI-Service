package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PatientHealthIdService {
    private static final Logger logger = getLogger(PatientHealthIdService.class);

    private Queue<MciHealthId> MciHealthIds = new ConcurrentLinkedQueue<>();

    private HealthIdService healthIdService;
    private final MCIProperties mciProperties;

    @Autowired
    public PatientHealthIdService(HealthIdService healthIdService, MCIProperties mciProperties) {
        this.healthIdService = healthIdService;
        this.mciProperties = mciProperties;
    }

    public MciHealthId getNextHealthId() throws InterruptedException {
        logger.debug("get: block size :" + MciHealthIds.size());
        logger.debug(" object id : " + this);
        return MciHealthIds.remove();
    }

    public void putBackHealthId(MciHealthId MciHealthId) {
        MciHealthIds.add(MciHealthId);
    }

    public void replenishIfNeeded() {
        logger.debug("replenish: block size :" + MciHealthIds.size());
        logger.debug("replenish: object id : " + this);
        if (MciHealthIds.size() < mciProperties.getHealthIdBlockSizeThreshold()) {
            MciHealthIds.addAll(healthIdService.getNextBlock());
        }
    }

    public void markUsed(MciHealthId nextMciHealthId) {
        healthIdService.markUsed(nextMciHealthId);
    }

    public int getHealthIdBlockSize() {
        return MciHealthIds.size();
    }

}
