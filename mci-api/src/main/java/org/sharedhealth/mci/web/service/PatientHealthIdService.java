package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.model.HealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class PatientHealthIdService {

    private Queue<HealthId> healthIds = new ConcurrentLinkedQueue<>();

    private HealthIdService healthIdService;
    private final MCIProperties mciProperties;

    @Autowired
    public PatientHealthIdService(HealthIdService healthIdService, MCIProperties mciProperties) {
        this.healthIdService = healthIdService;
        this.mciProperties = mciProperties;
    }

    public HealthId getNextHealthId() throws InterruptedException {
        return healthIds.remove();
    }

    public void putBackHealthId(HealthId healthId) {
        healthIds.add(healthId);
    }

    public void replenishIfNeeded() {
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
