package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import org.sharedhealth.mci.web.mapper.PatientAuditLogData;
import org.sharedhealth.mci.web.mapper.PatientAuditLogMapper;
import org.sharedhealth.mci.web.model.Marker;
import org.sharedhealth.mci.web.model.PatientAuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientAuditLogQueryBuilder.buildFindByHidStmt;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientAuditLogQueryBuilder.buildFindLatestMarkerStmt;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientAuditLogQueryBuilder.buildSaveOrUpdateBatch;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientAuditLogQueryBuilder.updateMarker;

@Component
public class PatientAuditRepository extends BaseRepository {
    private static final Logger logger = LoggerFactory.getLogger(PatientAuditRepository.class);

    private final PatientAuditLogMapper auditLogMapper;

    @Autowired
    public PatientAuditRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations,
                                  PatientAuditLogMapper auditLogMapper) {
        super(cassandraOperations);
        this.auditLogMapper = auditLogMapper;
    }

    public List<PatientAuditLogData> findByHealthId(String healthId) {
        logger.debug(String.format("get audit log for patient: healthId (%s)", healthId));
        List<PatientAuditLog> logs = cassandraOps.select(buildFindByHidStmt(healthId), PatientAuditLog.class);
        if (isNotEmpty(logs)) {
            return this.auditLogMapper.map(logs);
        }
        return emptyList();
    }

    public UUID findLatestMarker() {
        List<Marker> markers = cassandraOps.select(buildFindLatestMarkerStmt(), Marker.class);
        if (isEmpty(markers)) {
            return null;
        }
        return fromString(markers.get(0).getMarker());
    }

    public void saveOrUpdate(List<PatientAuditLog> logs) {
        Batch batch = buildSaveOrUpdateBatch(logs, cassandraOps.getConverter());
        cassandraOps.execute(batch);
    }

    public void updateMarkerTable(List<PatientAuditLog> logs) {
        Batch batch = batch();
        updateMarker(logs.get(logs.size() - 1).getEventId().toString(), cassandraOps.getConverter(), batch);
        cassandraOps.execute(batch);
    }

}
