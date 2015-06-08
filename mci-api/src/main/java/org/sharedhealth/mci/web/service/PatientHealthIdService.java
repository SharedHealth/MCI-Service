package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.model.HealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public class PatientHealthIdService {

    public static final int HEALTH_ID_CHUNK_SIZE = 11000;
    public static final int THRESHOLD = 1000;
    BlockingQueue<HealthId> healthIds = new ArrayBlockingQueue<>(HEALTH_ID_CHUNK_SIZE);

    private HealthIdService healthIdService;

    @Autowired
    public PatientHealthIdService(HealthIdService healthIdService) {
        this.healthIdService = healthIdService;
    }


    public HealthId getNextHealthId() throws InterruptedException {
        return healthIds.take();
    }

    public void replenishIfNeeded() {
        if (healthIds.size() < THRESHOLD) {
            healthIds.addAll(healthIdService.getNextBlock());
        }
    }
}
