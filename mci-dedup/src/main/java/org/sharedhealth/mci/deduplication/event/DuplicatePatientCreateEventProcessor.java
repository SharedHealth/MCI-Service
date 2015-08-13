package org.sharedhealth.mci.deduplication.event;

import org.sharedhealth.mci.deduplication.model.DuplicatePatient;
import org.sharedhealth.mci.deduplication.repository.DuplicatePatientRepository;
import org.sharedhealth.mci.domain.model.PatientUpdateLogData;
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
