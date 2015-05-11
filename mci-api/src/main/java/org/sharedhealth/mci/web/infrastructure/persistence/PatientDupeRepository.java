package org.sharedhealth.mci.web.infrastructure.persistence;

import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.model.PatientDupe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.sharedhealth.mci.web.infrastructure.persistence.DedupQueryBuilder.buildFindByCatchmentStmt;

@Component
public class PatientDupeRepository extends BaseRepository {

    @Autowired
    public PatientDupeRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public List<PatientDupe> findAllByCatchment(Catchment catchment) {
        return cassandraOps.select(buildFindByCatchmentStmt(catchment), PatientDupe.class);
    }
}
