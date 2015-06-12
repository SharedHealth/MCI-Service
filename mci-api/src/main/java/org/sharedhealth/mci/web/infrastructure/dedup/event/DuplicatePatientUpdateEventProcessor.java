package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DuplicatePatientUpdateEventProcessor extends DuplicatePatientEventProcessor {

    @Override
    public void process(String healthId, UUID marker) {
        List<DuplicatePatient> duplicates = buildDuplicates(healthId);
        getDuplicatePatientRepository().update(healthId, duplicates, marker);
    }
}
