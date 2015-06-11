package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;

import java.util.UUID;

public class DuplicatePatientRetireEventProcessor extends DuplicatePatientEventProcessor {

    public DuplicatePatientRetireEventProcessor(DuplicatePatientRuleEngine ruleEngine, DuplicatePatientMapper mapper) {
        super(ruleEngine, mapper);
    }

    @Override
    public void process(String healthId, UUID marker) {

    }
}
