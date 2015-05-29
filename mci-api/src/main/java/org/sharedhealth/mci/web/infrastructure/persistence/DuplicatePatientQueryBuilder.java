package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.data.cassandra.convert.CassandraConverter;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.springframework.data.cassandra.core.CassandraTemplate.createDeleteQuery;

public class DuplicatePatientQueryBuilder {

    public static String buildFindByCatchmentStmt(Catchment catchment) {
        return select().from(CF_PATIENT_DUPLICATE).where(eq(CATCHMENT_ID, catchment.getId())).toString();
    }

    public static String buildFindByCatchmentAndHealthIdsStmt(Catchment catchment, String healthId1, String healthId2) {
        return select().from(CF_PATIENT_DUPLICATE)
                .where(eq(CATCHMENT_ID, catchment.getId()))
                .and(eq(HEALTH_ID1, healthId1))
                .and(eq(HEALTH_ID2, healthId2))
                .toString();
    }

    public static void buildDeleteDuplicatesStmt(PatientData patient1, PatientData patient2,
                                                 CassandraConverter converter, Batch batch) {
        String healthId1 = patient1.getHealthId();
        String catchmentId1 = patient1.getCatchment().getId();
        String healthId2 = patient2.getHealthId();
        String catchmentId2 = patient2.getCatchment().getId();

        DuplicatePatient duplicatePatient1 = new DuplicatePatient(catchmentId1, healthId1, healthId2);
        batch.add(createDeleteQuery(CF_PATIENT_DUPLICATE, duplicatePatient1, null, converter));

        DuplicatePatient duplicatePatient2 = new DuplicatePatient(catchmentId2, healthId2, healthId1);
        batch.add(createDeleteQuery(CF_PATIENT_DUPLICATE, duplicatePatient2, null, converter));
    }

    public static void buildDeleteDuplicatesStmt(PatientData patient, Batch batch) {
        String catchmentId = patient.getCatchment().getId();
        String healthId = patient.getHealthId();
        batch.add(delete().from(CF_PATIENT_DUPLICATE)
                .where(eq(CATCHMENT_ID, catchmentId))
                .and(eq(HEALTH_ID1, healthId)));
    }
}
