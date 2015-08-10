package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.domain.repository.BaseRepository;
import org.sharedhealth.mci.web.exception.HealthIdExhaustedException;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.sharedhealth.mci.domain.constant.RepositoryConstants.CF_MCI_HEALTH_ID;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.HID;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

@Component
public class HealthIdRepository extends BaseRepository {
    private static final Logger logger = LoggerFactory.getLogger(HealthIdRepository.class);
    public static final int BLOCK_SIZE = 10000;
    private String lastTakenHidMarker;

    @Autowired
    public HealthIdRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public MciHealthId saveHealthId(MciHealthId MciHealthId) {
        logger.debug(String.format("Inserting new hid :%s", MciHealthId.getHid()));
        cassandraOps.executeAsynchronously(createInsertQuery(CF_MCI_HEALTH_ID, MciHealthId, null, cassandraOps.getConverter()).ifNotExists());
        return MciHealthId;
    }

    public MciHealthId saveHealthIdSync(MciHealthId MciHealthId) {
        logger.debug(String.format("Inserting new hid :%s", MciHealthId.getHid()));
        cassandraOps.execute(createInsertQuery(CF_MCI_HEALTH_ID, MciHealthId, null, cassandraOps.getConverter()).ifNotExists());
        return MciHealthId;
    }

    public List<MciHealthId> getNextBlock(int blockSize) {
        logger.debug(String.format("Getting next block of size : %d", blockSize));
        Select.Where from = QueryBuilder.select().from(CF_MCI_HEALTH_ID).where();
        if (null != lastTakenHidMarker) {
            from = from.and(QueryBuilder.gt("token(hid)", QueryBuilder.raw(String.format("token('%s')", lastTakenHidMarker))));
        }
        Select nextBlockQuery = from.limit(blockSize);

        List<MciHealthId> MciHealthIds = cassandraOps.select(nextBlockQuery, MciHealthId.class);
        if (MciHealthIds.size() < 1) throw new HealthIdExhaustedException();
        this.lastTakenHidMarker = MciHealthIds.get(MciHealthIds.size() - 1).getHid();
        return MciHealthIds;
    }

    public List<MciHealthId> getNextBlock() {
        return getNextBlock(BLOCK_SIZE);
    }

    public String getLastTakenHidMarker() {
        return lastTakenHidMarker;
    }

    public void resetLastReservedHealthId() {
        lastTakenHidMarker = null;
    }

    public void removedUsedHid(MciHealthId nextMciHealthId) {
        cassandraOps.deleteAsynchronously(nextMciHealthId);
    }

    public void removeUsedHidSync(MciHealthId nextMciHealthId) {
        cassandraOps.delete(nextMciHealthId);
    }

    public MciHealthId getHealthId(String hid) {
        Select selectHealthId = QueryBuilder.select().from(CF_MCI_HEALTH_ID).where(QueryBuilder.eq(HID, hid)).limit(1);
        List<MciHealthId> MciHealthIds = cassandraOps.select(selectHealthId, MciHealthId.class);
        return MciHealthIds.isEmpty() ? null : MciHealthIds.get(0);
    }
}
