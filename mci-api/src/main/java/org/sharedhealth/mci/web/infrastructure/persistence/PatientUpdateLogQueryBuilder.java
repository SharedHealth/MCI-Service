package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import org.sharedhealth.mci.web.builder.PatientDiffBuilder;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.springframework.data.cassandra.convert.CassandraConverter;

import java.util.Map;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.CF_PATIENT_UPDATE_LOG;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

public class PatientUpdateLogQueryBuilder {

    static void buildCreateUpdateLogStmt(PatientData patientDataToSave, PatientData existingPatientData,
                                         CassandraConverter converter, Batch batch) {
        PatientUpdateLog patientUpdateLog = new PatientUpdateLog();
        String changeSet = getChangeSet(patientDataToSave, existingPatientData);

        if (changeSet != null) {
            patientUpdateLog.setEventId(timeBased());
            patientUpdateLog.setHealthId(existingPatientData.getHealthId());
            patientUpdateLog.setChangeSet(changeSet);
            batch.add(createInsertQuery(CF_PATIENT_UPDATE_LOG, patientUpdateLog, null, converter));
        }
    }

    private static String getChangeSet(PatientData newData, PatientData oldData) {
        Map<String, Map<String, Object>> diff = new PatientDiffBuilder(oldData, newData).build();
        if (diff != null && diff.size() > 0) {
            return writeValueAsString(diff);
        }
        return null;
    }
}
