package org.sharedhealth.mci.web.infrastructure.persistence;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.util.concurrent.SettableFuture;
import org.sharedhealth.mci.web.model.Setting;
import org.sharedhealth.mci.web.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cassandra.core.AsynchronousQueryListener;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Component
public class SettingRepository extends BaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(LocationRepository.class);

    @Autowired
    public SettingRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        super(cassandraOperations);
    }

    public ListenableFuture<Setting> findSettingListenableFutureByKey(final String key) {

        final SettableFuture<Setting> result = SettableFuture.create();

        Select select = QueryBuilder.select().from("settings");

        select.where(QueryBuilder.eq("key", key));

        cassandraOps.queryAsynchronously(select, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    Row row = rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS).one();
                    setSettingOnResult(row, result);
                } catch (Exception e) {
                    logger.error("Error while finding settings for key: " + key, e);
                    result.setException(e);
                }
            }
        });

        return new SimpleListenableFuture<Setting, Setting>(result) {
            @Override
            protected Setting adapt(Setting adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    private void setSettingOnResult(Row r, SettableFuture<Setting> result) throws InterruptedException, ExecutionException {
        Setting setting = getSettingFromRow(r);
        result.set(setting);
    }

    private Setting getSettingFromRow(Row r) {
        DatabaseRow row = new DatabaseRow(r);

        Setting setting = new Setting();

        setting.setKey(row.getString("key"));
        setting.setValue(row.getString("settings"));

        return setting;
    }

    @CacheEvict("mciSettings")
    public void save(Setting setting) {
        cassandraOps.insert(setting);
    }

    @Cacheable({"mciSettings"})
    public Setting findByKey(String key) {
        Setting setting = null;
        try {
            setting = findSettingListenableFutureByKey(key).get();
        } catch (Exception e) {
            logger.debug("Could not find Setting for key : [" + key + "]");
        }

        return setting;
    }
}
