package org.sharedhealth.mci.web.infrastructure.dedup;

import org.sharedhealth.mci.web.mapper.DuplicatePatientData;

import java.util.List;
import java.util.UUID;

public abstract class DuplicatePatientEventProcessor {

    private DuplicatePatientRuleEngine ruleEngine;

    protected DuplicatePatientEventProcessor(DuplicatePatientRuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    public abstract void process(String healthId, UUID marker);

    protected List<DuplicatePatientData> findDuplicates(String healthId) {
        return getRuleEngine().apply(healthId);
    }

    protected DuplicatePatientRuleEngine getRuleEngine() {
        return ruleEngine;
    }
}
