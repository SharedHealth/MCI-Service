package org.sharedhealth.mci.web.infrastructure.dedup.rule;

import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DuplicatePatientRuleEngine {

    private List<DuplicatePatientRule> rules;

    @Autowired
    public DuplicatePatientRuleEngine(List<DuplicatePatientRule> rules) {
        this.rules = rules;
    }

    public List<DuplicatePatientData> apply(String healthId) {
        List<DuplicatePatientData> duplicates = new ArrayList<>();
        for (DuplicatePatientRule rule : rules) {
            if (rule != null) {
                rule.apply(healthId, duplicates);
            }
        }
        return duplicates;
    }
}
