package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.sharedhealth.mci.utils.DateUtil.toIsoMillisFormat;
import static org.sharedhealth.mci.web.utils.JsonMapper.readValue;

@Component
public class PatientUpdateLogMapper {
    public List<PatientUpdateLogData> map(List<PatientUpdateLog> logs) {
        List<PatientUpdateLogData> dataList = new ArrayList<>();
        for (PatientUpdateLog log : logs) {
            dataList.add(this.map(log));
        }
        return dataList;
    }

    public PatientUpdateLogData map(PatientUpdateLog log) {
        PatientUpdateLogData data = new PatientUpdateLogData();
        data.setHealthId(log.getHealthId());
        data.setEventTime(toIsoMillisFormat(log.getEventId()));
        data.setChangeSet(buildChangeSet(log.getChangeSet()));

        String requestedBy = log.getRequestedBy();
        data.setRequestedBy((null == requestedBy) ? null : readValue(log.getRequestedBy(), new TypeReference<Map<String, Set<Requester>>>() {
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
