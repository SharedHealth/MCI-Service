package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.querybuilder.Batch;
import org.sharedhealth.mci.domain.model.PatientAuditLog;
import org.sharedhealth.mci.domain.model.PatientAuditLogData;
import org.sharedhealth.mci.domain.model.PatientAuditLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.sharedhealth.mci.domain.repository.PatientAuditLogQueryBuilder.buildFindByHidStmt;
import static org.sharedhealth.mci.domain.repository.PatientAuditLogQueryBuilder.buildSaveOrUpdateBatch;

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

    //only for tests
    public void saveOrUpdate(List<PatientAuditLog> logs) {
        Batch batch = buildSaveOrUpdateBatch(logs, cassandraOps.getConverter());
        cassandraOps.execute(batch);
    }


}
