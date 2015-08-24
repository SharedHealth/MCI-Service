package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.domain.model.PatientUpdateLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.sharedhealth.mci.domain.repository.PatientUpdateLogQueryBuilder.buildFindUpdateLogStmt;
import static org.sharedhealth.mci.domain.util.DateUtil.getYearOf;
import static org.sharedhealth.mci.domain.util.DateUtil.getYearsSince;

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

    public PatientUpdateLog findPatientUpdateLogByEventId(UUID eventId) {
        Select select = select().from(CF_PATIENT_UPDATE_LOG);
        List<Integer> years = getYearsSince(getYearOf(eventId));
        select.where(in(YEAR, years.toArray()));
        select.where(eq(EVENT_ID, eventId));
        List<PatientUpdateLog> logs = cassandraOps.select(select, PatientUpdateLog.class);
        return isNotEmpty(logs) ? logs.get(0) : null;
    }
}
