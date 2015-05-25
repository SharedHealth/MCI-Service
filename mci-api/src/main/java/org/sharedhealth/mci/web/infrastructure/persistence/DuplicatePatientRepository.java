package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientQueryBuilder.buildDeleteDuplicatesStmt;
import static org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientQueryBuilder.buildFindByCatchmentStmt;

@Component
public class DuplicatePatientRepository extends BaseRepository {

    @Autowired
    public DuplicatePatientRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public List<DuplicatePatient> findAllByCatchment(Catchment catchment) {
        return cassandraOps.select(buildFindByCatchmentStmt(catchment), DuplicatePatient.class);
    }

    public void ignore(PatientData patient1, PatientData patient2) {
        Batch batch = batch();
        buildDeleteDuplicatesStmt(patient1, patient2, cassandraOps.getConverter(), batch);
        cassandraOps.execute(batch);
    }
}
