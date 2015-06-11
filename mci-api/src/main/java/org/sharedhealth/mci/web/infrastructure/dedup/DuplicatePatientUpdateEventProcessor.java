package org.sharedhealth.mci.web.infrastructure.dedup;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class DuplicatePatientUpdateEventProcessor extends DuplicatePatientEventProcessor {

    @Autowired
    public DuplicatePatientUpdateEventProcessor(DuplicatePatientRuleEngine ruleEngine) {
        super(ruleEngine);
    }

    @Override
    public void process(String healthId, UUID marker) {

    }
}
