package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.MarkerRepository;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DuplicatePatientRetireEventProcessor extends DuplicatePatientEventProcessor {

    private final DuplicatePatientRepository duplicatePatientRepository;
    private final MarkerRepository markerRepository;

    @Autowired
    public DuplicatePatientRetireEventProcessor(DuplicatePatientRuleEngine ruleEngine, DuplicatePatientMapper mapper,
                                                DuplicatePatientRepository duplicatePatientRepository,
                                                MarkerRepository markerRepository) {
        super(ruleEngine, mapper);
        this.duplicatePatientRepository = duplicatePatientRepository;
        this.markerRepository = markerRepository;
    }

    @Override
    public void process(String healthId, UUID marker) {

    }
}
