package org.sharedhealth.mci.deduplication.config.event;

import org.sharedhealth.mci.domain.model.PatientUpdateLogData;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DuplicatePatientRetireEventProcessor extends DuplicatePatientEventProcessor {

    @Override
    public void process(PatientUpdateLogData log, UUID marker) {
        getDuplicatePatientRepository().retire(log.getHealthId(), marker);
    }
}
