package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.mapper.PatientUpdateLogData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DuplicatePatientCreateEventProcessor extends DuplicatePatientEventProcessor {

    @Override
    public void process(PatientUpdateLogData log, UUID marker) {
        List<DuplicatePatient> duplicates = buildDuplicates(log.getHealthId());
        DuplicatePatientRepository duplicatePatientRepository = getDuplicatePatientRepository();
        duplicates = filterPersistentDuplicates(duplicatePatientRepository, duplicates);
        duplicatePatientRepository.create(duplicates, marker);
    }


}
