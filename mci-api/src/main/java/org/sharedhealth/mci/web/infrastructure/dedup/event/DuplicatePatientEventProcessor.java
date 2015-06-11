package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.web.model.DuplicatePatient;

import java.util.List;
import java.util.UUID;

public abstract class DuplicatePatientEventProcessor {

    private DuplicatePatientRuleEngine ruleEngine;
    private DuplicatePatientMapper mapper;

    protected DuplicatePatientEventProcessor(DuplicatePatientRuleEngine ruleEngine, DuplicatePatientMapper mapper) {
        this.ruleEngine = ruleEngine;
        this.mapper = mapper;
    }

    public abstract void process(String healthId, UUID marker);

    protected List<DuplicatePatient> buildDuplicates(String healthId) {
        List<DuplicatePatientData> duplicates = getRuleEngine().apply(healthId);
        return mapper.map(duplicates);
    }

    protected DuplicatePatientRuleEngine getRuleEngine() {
        return ruleEngine;
    }
}
