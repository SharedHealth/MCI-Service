package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import org.sharedhealth.mci.web.model.Marker;
import org.sharedhealth.mci.web.model.PatientAuditLog;
import org.springframework.data.cassandra.convert.CassandraConverter;

import java.util.Date;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

public class PatientAuditLogQueryBuilder {

    private static final String AUDIT_MARKER_TYPE = "audit_marker";
    private static final long QUERY_EXEC_DELAY = 1;

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

        updateMarker(logs.get(logs.size() - 1).getEventId().toString(), converter, batch);

        return batch;
    }

    private static void updateMarker(String marker, CassandraConverter converter, Batch batch) {
        long time = new Date().getTime();
        Delete delete = delete().from(CF_MARKER);
        delete.where(eq(TYPE, AUDIT_MARKER_TYPE));
        delete.using(timestamp(time));
        batch.add(delete);

        Marker newMarker = new Marker();
        newMarker.setType(AUDIT_MARKER_TYPE);
        newMarker.setCreatedAt(timeBased());
        newMarker.setMarker(marker);
        Insert insert = createInsertQuery(CF_MARKER, newMarker, null, converter);
        insert.using(timestamp(time + QUERY_EXEC_DELAY));
        batch.add(insert);
    }
}
