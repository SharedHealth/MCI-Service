package org.sharedhealth.mci.web.infrastructure.persistence;

import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

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
}
