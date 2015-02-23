package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import org.sharedhealth.mci.web.model.PatientAuditLog;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.web.utils.JsonMapper.readValue;

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
        data.setEventTime(toIsoFormat(log.getEventId()));
        data.setChangeSet(buildChangeSet(log));
        return data;
    }

    private List<ChangeSet> buildChangeSet(PatientAuditLog log) {
        return readValue(log.getChangeSet(), new TypeReference<List<ChangeSet>>() {
        });
    }
}
