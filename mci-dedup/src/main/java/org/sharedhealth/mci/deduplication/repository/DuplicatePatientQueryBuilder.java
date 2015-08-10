package org.sharedhealth.mci.deduplication.repository;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.deduplication.model.DuplicatePatient;
import org.sharedhealth.mci.deduplication.model.DuplicatePatientIgnored;
import org.sharedhealth.mci.domain.model.Catchment;
import org.springframework.data.cassandra.convert.CassandraConverter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.springframework.data.cassandra.core.CassandraTemplate.createDeleteQuery;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

public class DuplicatePatientQueryBuilder {

    public static String buildFindByCatchmentStmt(Catchment catchment, UUID after, UUID before, int limit) {
        Select.Where where = select().from(CF_PATIENT_DUPLICATE).where(eq(CATCHMENT_ID, catchment.getId()));

        if (after != null) {
            where.and(gt(CREATED_AT, after));
        }

        if (before != null) {
            where.and(lt(CREATED_AT, before)).orderBy(desc(CREATED_AT));
        }

        return where.limit(limit).toString();
    }

    public static String buildFindByCatchmentAndHealthIdsStmt(String catchmentId, String healthId1, String healthId2) {
        return select().from(CF_PATIENT_DUPLICATE).allowFiltering()
                .where(eq(CATCHMENT_ID, catchmentId))
                .and(eq(HEALTH_ID1, healthId1))
                .and(eq(HEALTH_ID2, healthId2))
                .toString();
    }

    public static String buildFindByCatchmentAndHealthIdStmt(String catchmentId, String healthId1) {
        return select().from(CF_PATIENT_DUPLICATE)
                .where(eq(CATCHMENT_ID, catchmentId))
                .and(eq(HEALTH_ID1, healthId1))
                .toString();
    }

    public static void buildDeleteDuplicatesStmt(List<DuplicatePatient> duplicates, CassandraConverter converter,
                                                 Batch batch, long timestamp) {
        for (DuplicatePatient duplicate : duplicates) {
            Delete deleteQuery = createDeleteQuery(CF_PATIENT_DUPLICATE, duplicate, null, converter);
            deleteQuery.using(timestamp(timestamp));
            batch.add(deleteQuery);
        }
    }

    public static void buildCreateIgnoreDuplicatesStmt(String healthId1, String healthId2, Set<String> reasons,
                                                       CassandraConverter converter, Batch batch) {
        DuplicatePatientIgnored duplicateIgnorePatient1 = new DuplicatePatientIgnored(healthId1, healthId2, reasons);
        DuplicatePatientIgnored duplicateIgnorePatient2 = new DuplicatePatientIgnored(healthId2, healthId1, reasons);
        batch.add(createInsertQuery(CF_PATIENT_DUPLICATE_IGNORED, duplicateIgnorePatient1, null, converter));
        batch.add(createInsertQuery(CF_PATIENT_DUPLICATE_IGNORED, duplicateIgnorePatient2, null, converter));
    }

    public static String buildFindIgnoreDuplicatesStmt(String healthId1, String healthId2) {
        return select().from(CF_PATIENT_DUPLICATE_IGNORED)
                .where(eq(HEALTH_ID1, healthId1)).and(eq(HEALTH_ID2, healthId2)).toString();
    }
}
