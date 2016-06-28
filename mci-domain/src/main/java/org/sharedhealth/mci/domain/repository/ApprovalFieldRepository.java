package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.domain.model.ApprovalField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import static org.sharedhealth.mci.domain.config.MCICacheConfiguration.APPROVAL_FIELDS_CACHE;

@Component
public class ApprovalFieldRepository extends BaseRepository {

    @Autowired
    public ApprovalFieldRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        super(cassandraOperations);
    }

    public ApprovalField findFieldDataByKey(final String field) {
        Select select = QueryBuilder.select().from("approval_fields");
        select.where(QueryBuilder.eq("field", field));

        return cassandraOps.selectOne(select, ApprovalField.class);
    }

    @Cacheable(value = APPROVAL_FIELDS_CACHE, key = "#field", unless = "#result == null")
    public ApprovalField findByField(String field) {
        return findFieldDataByKey(field);
    }
}
