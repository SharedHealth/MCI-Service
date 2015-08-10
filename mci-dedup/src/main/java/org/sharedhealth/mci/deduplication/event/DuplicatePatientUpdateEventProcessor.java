package org.sharedhealth.mci.deduplication.event;

import org.sharedhealth.mci.deduplication.model.DuplicatePatient;
import org.sharedhealth.mci.domain.model.PatientUpdateLogData;
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
