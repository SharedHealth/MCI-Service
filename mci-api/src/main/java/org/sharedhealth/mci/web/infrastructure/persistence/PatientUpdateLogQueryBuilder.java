package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import org.sharedhealth.mci.web.builder.PatientDiffBuilder;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.Requester;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.springframework.data.cassandra.convert.CassandraConverter;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.datastax.driver.core.querybuilder.Select.Where;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.sharedhealth.mci.utils.DateUtil.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

public class PatientUpdateLogQueryBuilder {

    static void buildCreateUpdateLogStmt(PatientData patientDataToSave, PatientData existingPatientData,
                                         Map<String, Set<Requester>> requestedBy, Requester approvedBy,
                                         CassandraConverter converter, Batch batch) {
        PatientUpdateLog patientUpdateLog = new PatientUpdateLog();
        String changeSet = getChangeSet(patientDataToSave, existingPatientData);

        if (changeSet != null) {
            patientUpdateLog.setEventId(timeBased());
            patientUpdateLog.setHealthId(existingPatientData.getHealthId());
            patientUpdateLog.setChangeSet(changeSet);
            patientUpdateLog.setRequestedBy(writeValueAsString(requestedBy));
            patientUpdateLog.setApprovedBy(writeValueAsString(approvedBy));
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

    public static String buildFindUpdateLogStmt(UUID lastMarker, int limit) {
        Select select = select().from(CF_PATIENT_UPDATE_LOG);

        if (lastMarker != null) {
            List<Integer> years = getYearsSince(getYearOf(lastMarker));
            select.where(in(YEAR, years.toArray()));
            select.where(gt(EVENT_ID, lastMarker));
        }
        if (limit > 0) {
            select.limit(limit);
        }

        return select.toString();
    }

    public static String buildFindUpdateLogStmt(Date since, int limit, UUID lastMarker) {
        int year = getLastYearMarker(since, lastMarker);
        Where where = select().from(CF_PATIENT_UPDATE_LOG)
                .where(in(YEAR, getYearsSince(year).toArray()));

        if (lastMarker != null) {
            where = where.and(gt(EVENT_ID, lastMarker));
        } else if (since != null) {
            where = where.and(gte(EVENT_ID, UUIDs.startOf(since.getTime())));
        }

        return where.limit(limit).toString();
    }

    private static int getLastYearMarker(Date after, UUID lastMarker) {
        if (lastMarker != null) {
            return getYearOf(lastMarker);
        }

        if (after != null) {
            return getYearOf(after);
        }

        return getCurrentYear();
    }

    public static String buildFindUpdateLogStmt(int year, UUID eventId, String healthId) {
        return select().from(CF_PATIENT_UPDATE_LOG)
                .where(eq(YEAR, year))
                .and(eq(EVENT_ID, eventId))
                .and(eq(HEALTH_ID, healthId))
                .toString();
    }
}
