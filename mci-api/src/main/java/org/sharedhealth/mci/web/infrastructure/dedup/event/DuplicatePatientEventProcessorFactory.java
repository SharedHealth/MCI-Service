package org.sharedhealth.mci.web.infrastructure.dedup.event;

import com.fasterxml.jackson.core.type.TypeReference;
import org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.EVENT_TYPE_CREATED;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.EVENT_TYPE_UPDATED;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.sharedhealth.mci.web.utils.JsonMapper.readValue;

@Component
public class DuplicatePatientEventProcessorFactory {

    private DuplicatePatientRuleEngine ruleEngine;
    private DuplicatePatientMapper mapper;

    @Autowired
    public DuplicatePatientEventProcessorFactory(DuplicatePatientRuleEngine ruleEngine, DuplicatePatientMapper mapper) {
        this.ruleEngine = ruleEngine;
        this.mapper = mapper;
    }

    public DuplicatePatientEventProcessor getEventProcessor(String eventType, String changeSet) {
        if (EVENT_TYPE_CREATED.equals(eventType)) {
            return new DuplicatePatientCreateEventProcessor(ruleEngine, mapper);
        }

        if (EVENT_TYPE_UPDATED.equals(eventType)) {
            Map<String, Map<String, Object>> changeSetMap = readValue(changeSet,
                    new TypeReference<Map<String, Map<String, Object>>>() {
                    });
            Map<String, Object> activeField = changeSetMap.get(ACTIVE);
            if (isActiveFieldRetired(activeField)) {
                return new DuplicatePatientRetireEventProcessor(ruleEngine, mapper);
            }
            return new DuplicatePatientUpdateEventProcessor(ruleEngine, mapper);
        }
        return null;
    }

    boolean isActiveFieldRetired(Map<String, Object> activeField) {
        if (activeField != null) {
            Object newValue = activeField.get(NEW_VALUE);
            Object oldValue = activeField.get(OLD_VALUE);
            if (newValue instanceof Boolean && oldValue instanceof Boolean) {
                return FALSE.equals(newValue) & TRUE.equals(oldValue);
            }
        }
        return false;
    }
}
