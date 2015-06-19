package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import org.sharedhealth.mci.web.model.PatientAuditLog;
import org.springframework.data.cassandra.convert.CassandraConverter;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.MarkerRepositoryQueryBuilder.buildUpdateMarkerBatch;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
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
}
