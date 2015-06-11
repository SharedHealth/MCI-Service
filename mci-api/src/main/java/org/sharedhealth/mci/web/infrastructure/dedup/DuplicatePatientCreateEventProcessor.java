package org.sharedhealth.mci.web.infrastructure.dedup;

import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class DuplicatePatientCreateEventProcessor extends DuplicatePatientEventProcessor {

    public DuplicatePatientCreateEventProcessor(DuplicatePatientRuleEngine ruleEngine) {
        super(ruleEngine);
    }

    @Override
    public void process(String healthId, UUID marker) {

    }
}
