package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.sharedhealth.mci.web.mapper.PatientUpdateLogData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DuplicatePatientUpdateEventProcessor extends DuplicatePatientEventProcessor {

    @Override
    public void process(PatientUpdateLogData log, UUID marker) {
        String healthId = log.getHealthId();
        List<DuplicatePatient> duplicates = buildDuplicates(healthId);
        getDuplicatePatientRepository().update(healthId, log.getOldCatchmentFromChangeSet(), duplicates, marker);
    }
}
