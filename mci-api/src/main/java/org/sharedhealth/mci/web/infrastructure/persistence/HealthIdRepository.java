package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.web.exception.HealthIdExhaustedException;
import org.sharedhealth.mci.web.model.HealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.CF_HEALTH_ID;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.RESERVED_FOR;

@Component
public class HealthIdRepository extends BaseRepository{

    public static final int BLOCK_SIZE = 10000;
    private String lastReservedHealthId;

    @Autowired
    public HealthIdRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public HealthId saveHealthId(HealthId healthId) {
        return cassandraOps.insertAsynchronously(healthId);
    }

    public HealthId saveHealthIdSync(HealthId healthId) {
        return cassandraOps.insert(healthId);
    }

    public List<HealthId> getNextBlock(int blockSize) {
        Select.Where from = QueryBuilder.select().from(CF_HEALTH_ID).where(QueryBuilder.eq(RESERVED_FOR, "MCI"));
        if (null != lastReservedHealthId)
            from = from.and(QueryBuilder.gt("token(hid)",
                    QueryBuilder.raw(String.format("token('%s')", lastReservedHealthId))));

        Select nextBlockQuery = from.and(QueryBuilder.eq("Status", 0)).limit(blockSize).allowFiltering();

        List<HealthId> healthIds = cassandraOps.select(nextBlockQuery, HealthId.class);
        if (healthIds.size() < 1) throw new HealthIdExhaustedException();
        this.lastReservedHealthId = healthIds.get(healthIds.size() - 1).getHid();
        return healthIds;
    }

    public List<HealthId> getNextBlock() {
        return getNextBlock(BLOCK_SIZE);
    }

    public String getLastReservedHealthId() {
        return lastReservedHealthId;
    }

    public void resetLastReservedHealthId() {
        lastReservedHealthId = null;
    }
}
