package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.web.model.MasterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

@Component
public class MasterDataRepository extends BaseRepository {

    @Autowired
    public MasterDataRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        super(cassandraOperations);
    }

    public MasterData findDataByKey(final String type, final String key) {

        Select select = QueryBuilder.select().from("master_data");

        select.where(QueryBuilder.eq("type", type));
        select.where(QueryBuilder.eq("key", key));

        return cassandraOps.selectOne(select, MasterData.class);
    }

    @Cacheable({"masterData"})
    public MasterData findByKey(String type, String key) {
        return findDataByKey(type, key);
    }
}
