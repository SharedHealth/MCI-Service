package org.sharedhealth.mci.domain.model;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.sharedhealth.mci.domain.util.DateUtil.toIsoMillisFormat;
import static org.sharedhealth.mci.domain.util.JsonMapper.readValue;

@Component
public class PatientAuditLogMapper {

    public List<PatientAuditLogData> map(List<PatientAuditLog> logs) {
        List<PatientAuditLogData> dataList = new ArrayList<>();
        for (PatientAuditLog log : logs) {
            dataList.add(this.map(log));
        }
        return dataList;
    }

    public PatientAuditLogData map(PatientAuditLog log) {
        PatientAuditLogData data = new PatientAuditLogData();
        data.setEventTime(toIsoMillisFormat(log.getEventId()));
        data.setHealthId(log.getHealthId());
        data.setChangeSet(buildChangeSet(log.getChangeSet()));
        data.setRequestedBy(readValue(log.getRequestedBy(), new TypeReference<Map<String, Set<Requester>>>() {
        }));
        if (log.getApprovedBy() != null) {
            data.setApprovedBy(readValue(log.getApprovedBy(), Requester.class));
        }
        return data;
    }

    private Map<String, Map<String, Object>> buildChangeSet(String changeSet) {
        if (changeSet == null) {
            return null;
        }
        return readValue(changeSet, new TypeReference<Map<String, Map<String, Object>>>() {
        });
    }
}
