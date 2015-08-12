package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.querybuilder.Batch;
import org.sharedhealth.mci.domain.diff.PatientDiffBuilder;
import org.sharedhealth.mci.domain.model.PatientAuditLog;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.Requester;
import org.springframework.data.cassandra.convert.CassandraConverter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.sharedhealth.mci.domain.repository.MarkerRepositoryQueryBuilder.buildUpdateMarkerBatch;
import static org.sharedhealth.mci.domain.util.JsonMapper.writeValueAsString;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

public class PatientAuditLogQueryBuilder {

    public static String buildFindByHidStmt(String healthId) {
        return select().from(CF_PATIENT_AUDIT_LOG).where(eq(HEALTH_ID, healthId)).toString();
    }

    public static String buildFindLatestMarkerStmt() {
        return select().from(CF_MARKER).where(eq(TYPE, AUDIT_MARKER_TYPE)).limit(1).toString();
    }

    public static Batch buildSaveOrUpdateBatch(List<PatientAuditLog> logs, CassandraConverter converter) {
        Batch batch = batch();

        for (PatientAuditLog log : logs) {
            batch.add(createInsertQuery(CF_PATIENT_AUDIT_LOG, log, null, converter));
        }

        String marker = logs.get(logs.size() - 1).getEventId().toString();
        buildUpdateMarkerBatch(AUDIT_MARKER_TYPE, marker, converter, batch);
        return batch;
    }

    public static void buildUpdateAuditLogStmt(PatientData patientDataToSave, PatientData existingPatientData,
                                               Map<String, Set<Requester>> requestedBy, Requester approvedBy,
                                               CassandraConverter converter, Batch batch) {
        PatientAuditLog patientAuditLog = new PatientAuditLog();
        String changeSet = getChangeSet(patientDataToSave, existingPatientData);

        if (changeSet != null) {
            patientAuditLog.setEventId(timeBased());
            patientAuditLog.setHealthId(existingPatientData.getHealthId());
            patientAuditLog.setChangeSet(changeSet);
            patientAuditLog.setRequestedBy(writeValueAsString(requestedBy));
            patientAuditLog.setApprovedBy(writeValueAsString(approvedBy));
            batch.add(createInsertQuery(CF_PATIENT_AUDIT_LOG, patientAuditLog, null, converter));
        }
    }

    public static void buildCreateAuditLogStmt(PatientData patientDataToSave,
                                               Map<String, Set<Requester>> requestedBy,
                                               CassandraConverter converter, Batch batch) {
        PatientAuditLog patientAuditLog = new PatientAuditLog();
        String changeSet = getChangeSet(patientDataToSave, new PatientData());

        if (changeSet != null) {
            patientAuditLog.setEventId(timeBased());
            patientAuditLog.setHealthId(patientDataToSave.getHealthId());
            patientAuditLog.setChangeSet(changeSet);
            patientAuditLog.setRequestedBy(writeValueAsString(requestedBy));
            batch.add(createInsertQuery(CF_PATIENT_AUDIT_LOG, patientAuditLog, null, converter));
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
