package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.sharedhealth.mci.web.mapper.PatientUpdateLogData;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DuplicatePatientRetireEventProcessor extends DuplicatePatientEventProcessor {

    @Override
    public void process(PatientUpdateLogData log, UUID marker) {
        getDuplicatePatientRepository().retire(log.getHealthId(), marker);
    }
}
