package org.sharedhealth.mci.deduplication.event;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.EVENT_TYPE_CREATED;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.EVENT_TYPE_UPDATED;
import static org.sharedhealth.mci.domain.util.JsonMapper.readValue;

@Component
public class DuplicatePatientEventProcessorFactory {

    private DuplicatePatientEventProcessor createEventProcessor;
    private DuplicatePatientEventProcessor updateEventProcessor;
    private DuplicatePatientEventProcessor retireEventProcessor;

    @Autowired
    public DuplicatePatientEventProcessorFactory(DuplicatePatientCreateEventProcessor createEventProcessor,
                                                 DuplicatePatientUpdateEventProcessor updateEventProcessor,
                                                 DuplicatePatientRetireEventProcessor retireEventProcessor) {
        this.createEventProcessor = createEventProcessor;
        this.updateEventProcessor = updateEventProcessor;
        this.retireEventProcessor = retireEventProcessor;
    }

    public DuplicatePatientEventProcessor getEventProcessor(String eventType, String changeSet) {
        if (EVENT_TYPE_CREATED.equals(eventType)) {
            return createEventProcessor;
        }

        if (EVENT_TYPE_UPDATED.equals(eventType)) {
            Map<String, Map<String, Object>> changeSetMap = readValue(changeSet,
                    new TypeReference<Map<String, Map<String, Object>>>() {
                    });
            Map<String, Object> activeField = changeSetMap.get(ACTIVE);
            if (isActiveFieldRetired(activeField)) {
                return retireEventProcessor;
            }
            return updateEventProcessor;
        }
        return null;
    }

    public boolean isActiveFieldRetired(Map<String, Object> activeField) {
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
