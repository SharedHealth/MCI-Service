package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.model.HealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class PatientHealthIdService {

    public static final int THRESHOLD = 1000;
    Queue<HealthId> healthIds = new ConcurrentLinkedQueue<>();

    private HealthIdService healthIdService;

    @Autowired
    public PatientHealthIdService(HealthIdService healthIdService) {
        this.healthIdService = healthIdService;
    }

    public HealthId getNextHealthId() throws InterruptedException {
        return healthIds.remove();
    }

    public void replenishIfNeeded() {
        if (healthIds.size() < THRESHOLD) {
            healthIds.addAll(healthIdService.getNextBlock());
        }
    }
}
