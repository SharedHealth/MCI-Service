package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.domain.repository.BaseRepository;
import org.sharedhealth.mci.web.model.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import static org.sharedhealth.mci.domain.config.MCICacheConfiguration.SETTINGS_CACHE;

@Component
public class SettingRepository extends BaseRepository {

    @Autowired
    public SettingRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        super(cassandraOperations);
    }

    public Setting findSettingDataByKey(final String key) {

        Select select = QueryBuilder.select().from("settings");
        select.where(QueryBuilder.eq("key", key));

        return cassandraOps.selectOne(select, Setting.class);
    }

    @Cacheable(value = SETTINGS_CACHE, unless = "#result == null")
    public Setting findByKey(String key) {
        return findSettingDataByKey(key);
    }
}
