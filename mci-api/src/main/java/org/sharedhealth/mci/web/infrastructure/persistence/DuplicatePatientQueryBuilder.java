package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.sharedhealth.mci.web.model.DuplicatePatientIgnored;
import org.springframework.data.cassandra.convert.CassandraConverter;

import java.util.List;
import java.util.Set;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.springframework.data.cassandra.core.CassandraTemplate.createDeleteQuery;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

public class DuplicatePatientQueryBuilder {

    public static String buildFindByCatchmentStmt(Catchment catchment) {
        return select().from(CF_PATIENT_DUPLICATE).where(eq(CATCHMENT_ID, catchment.getId())).toString();
    }

    public static String buildFindByCatchmentAndHealthIdsStmt(Catchment catchment, String healthId1, String healthId2) {
        return select().from(CF_PATIENT_DUPLICATE)
                .where(in(CATCHMENT_ID, catchment.getAllIds().toArray()))
                .and(eq(HEALTH_ID1, healthId1))
                .and(eq(HEALTH_ID2, healthId2))
                .toString();
    }

    public static void buildDeleteDuplicatesStmt(PatientData patient1, PatientData patient2,
                                                 CassandraConverter converter, Batch batch) {
        String healthId1 = patient1.getHealthId();
        List<String> catchmentIds1 = patient1.getCatchment().getAllIds();
        String healthId2 = patient2.getHealthId();
        List<String> catchmentIds2 = patient2.getCatchment().getAllIds();

        buildDeleteDuplicateBatch(catchmentIds1, healthId1, healthId2, converter, batch);
        buildDeleteDuplicateBatch(catchmentIds2, healthId2, healthId1, converter, batch);
    }

    private static void buildDeleteDuplicateBatch(List<String> catchmentIds1, String healthId1, String healthId2,
                                                  CassandraConverter converter, Batch batch) {
        for (String catchmentId : catchmentIds1) {
            DuplicatePatient duplicatePatient = new DuplicatePatient(catchmentId, healthId1, healthId2);
            batch.add(createDeleteQuery(CF_PATIENT_DUPLICATE, duplicatePatient, null, converter));
        }
    }

    public static void buildDeleteDuplicatesStmt(PatientData patient, Batch batch) {
        String healthId = patient.getHealthId();
        for (String catchmentId : patient.getCatchment().getAllIds()) {
            batch.add(delete().from(CF_PATIENT_DUPLICATE)
                    .where(eq(CATCHMENT_ID, catchmentId))
                    .and(eq(HEALTH_ID1, healthId)));
        }
    }

    public static void buildCreateIgnoreDuplicatesStmt(String healthId1, String healthId2, Set<String> reasons,
                                                       CassandraConverter converter, Batch batch) {
        DuplicatePatientIgnored duplicateIgnorePatient = new DuplicatePatientIgnored(healthId1, healthId2, reasons);
        batch.add(createInsertQuery(CF_PATIENT_DUPLICATE_IGNORED, duplicateIgnorePatient, null, converter));
    }
}
