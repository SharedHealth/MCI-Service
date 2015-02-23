package org.sharedhealth.mci.web.infrastructure.persistence;

import org.sharedhealth.mci.web.mapper.PatientAuditLogData;
import org.sharedhealth.mci.web.mapper.PatientAuditLogMapper;
import org.sharedhealth.mci.web.model.PatientAuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientAuditLogQueryBuilder.buildFindByHidStmt;

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
        List<PatientAuditLog> logs = cassandraOps.select(buildFindByHidStmt(healthId), PatientAuditLog.class);
        if (isNotEmpty(logs)) {
            return this.auditLogMapper.map(logs);
        }
        return emptyList();
    }
}
