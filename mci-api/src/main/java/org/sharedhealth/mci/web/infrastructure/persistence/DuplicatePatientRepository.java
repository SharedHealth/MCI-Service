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
import static org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientQueryBuilder.*;

@Component
public class DuplicatePatientRepository extends BaseRepository {

    private PatientRepository patientRepository;

    @Autowired
    public DuplicatePatientRepository(PatientRepository patientRepository,
                                      @Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
        this.patientRepository = patientRepository;
    }

    public List<DuplicatePatient> findAllByCatchment(Catchment catchment) {
        return cassandraOps.select(buildFindByCatchmentStmt(catchment), DuplicatePatient.class);
    }

    public DuplicatePatient findByCatchmentAndHealthIds(Catchment catchment, String healthId1, String healthId2) {
        return cassandraOps.selectOne(buildFindByCatchmentAndHealthIdsStmt(catchment, healthId1, healthId2), DuplicatePatient.class);
    }

    public void processDuplicates(PatientData patient1, PatientData patient2, boolean isMerged) {
        Batch batch = batch();
        if (isMerged) {
            patientRepository.buildUpdateProcessBatch(patient1, patient1.getHealthId(), batch);
            patientRepository.buildUpdateProcessBatch(patient2, patient2.getHealthId(), batch);
            buildDeleteDuplicatesStmt(patient1, batch);
        }
        buildDeleteDuplicatesStmt(patient1, patient2, cassandraOps.getConverter(), batch);
        cassandraOps.execute(batch);
    }
}
