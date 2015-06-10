package org.sharedhealth.mci.web.infrastructure.dedup;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.EVENT_TYPE_CREATED;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.EVENT_TYPE_UPDATED;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.sharedhealth.mci.web.utils.JsonMapper.readValue;

public class DedupEventProcessorFactory {

    public static DedupEventProcessor getEventProcessor(String eventType, String changeSet) {
        if (EVENT_TYPE_CREATED.equals(eventType)) {
            return new DedupCreateEventProcessor();
        }

        if (EVENT_TYPE_UPDATED.equals(eventType)) {
            Map<String, Map<String, Object>> changeSetMap = readValue(changeSet,
                    new TypeReference<Map<String, Map<String, Object>>>() {
                    });
            Map<String, Object> activeField = changeSetMap.get(ACTIVE);
            if (isRetired(activeField)) {
                return new DedupRetireEventProcessor();
            }
            return new DedupUpdateEventProcessor();
        }
        return null;
    }

    static boolean isRetired(Map<String, Object> activeField) {
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
