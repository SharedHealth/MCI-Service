package org.sharedhealth.mci.domain.repository;

import org.sharedhealth.mci.domain.model.PatientUpdateLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.sharedhealth.mci.domain.repository.PatientUpdateLogQueryBuilder.buildFindUpdateLogStmt;

@Component
public class PatientFeedRepository extends BaseRepository {

    @Autowired
    public PatientFeedRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        super(cassandraOperations);
    }

    public List<PatientUpdateLog> findPatientsUpdatedSince(Date since, int limit, UUID lastMarker) {
        return cassandraOps.select(buildFindUpdateLogStmt(since, limit, lastMarker),
                PatientUpdateLog.class);
    }

    public List<PatientUpdateLog> findPatientsUpdatedSince(UUID lastMarker, int limit) {
        return cassandraOps.select(buildFindUpdateLogStmt(lastMarker, limit),
                PatientUpdateLog.class);
    }

    public PatientUpdateLog findPatientUpdateLog(UUID marker) {
        List<PatientUpdateLog> logs = this.findPatientsUpdatedSince(marker, 1);
        return isNotEmpty(logs) ? logs.get(0) : null;
    }
}
