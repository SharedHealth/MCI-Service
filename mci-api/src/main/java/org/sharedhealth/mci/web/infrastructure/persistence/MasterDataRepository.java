package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.util.concurrent.SettableFuture;
import org.sharedhealth.mci.web.model.MasterData;
import org.sharedhealth.mci.web.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cassandra.core.AsynchronousQueryListener;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class MasterDataRepository extends BaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(LocationRepository.class);

    @Autowired
    public MasterDataRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        super(cassandraOperations);
    }

    public ListenableFuture<MasterData> findDataListenableFutureByKey(final String type, final String key) {

        final SettableFuture<MasterData> result = SettableFuture.create();

        Select select = QueryBuilder.select().from("master_data");

        select.where(QueryBuilder.eq("type", type));
        select.where(QueryBuilder.eq("key", key));

        cassandraOps.queryAsynchronously(select, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    Row row = rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS).one();
                    setMasterDataOnResult(row, result);
                } catch (Exception e) {
                    logger.error("Error while finding data for key: " + key + " of type: " + type, e);
                    result.setException(e);
                }
            }
        });

        return new SimpleListenableFuture<MasterData, MasterData>(result) {
            @Override
            protected MasterData adapt(MasterData adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    private void setMasterDataOnResult(Row r, SettableFuture<MasterData> result) throws InterruptedException, ExecutionException {
        MasterData masterData = getMasterDataFromRow(r);
        result.set(masterData);
    }

    private MasterData getMasterDataFromRow(Row r) {
        DatabaseRow row = new DatabaseRow(r);

        return new MasterData(row.getString("type"), row.getString("key"), row.getString("value"));
    }

    @Cacheable({"masterData"})
    public MasterData findByKey(String type, String key) {
        MasterData masterData = null;

        try {
            masterData = findDataListenableFutureByKey(type, key).get();
        } catch (Exception e) {
            logger.debug("Could not find Setting for key : [" + key + "] of type [" + type + "]");
        }

        return masterData;
    }
}
