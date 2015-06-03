package org.sharedhealth.mci.web.infrastructure.persistence;

import org.sharedhealth.mci.web.model.HealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

@Component
public class HealthIdRepository extends BaseRepository{

    @Autowired
    public HealthIdRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public HealthId saveHealthId(HealthId healthId) {
        return cassandraOps.insertAsynchronously(healthId);
    }
}
