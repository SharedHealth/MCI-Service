package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.web.model.Facility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

@Component
public class FacilityRepository extends BaseRepository {

    @Autowired
    public FacilityRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public void save(Facility facility, int ttl) {
        cassandraOps.execute(buildInsertStatement(facility, ttl));
    }

    private Insert buildInsertStatement(Facility facility, int ttl) {
        return QueryBuilder
                .insertInto("facilities")
                .using(QueryBuilder.ttl(ttl))
                .value("id", facility.getId())
                .value("name", facility.getName())
                .value("type", facility.getType())
                .value("catchments", facility.getCatchments())
                .value("location", facility.getLocation())
                ;
    }

    public Facility find(String id) {
        Select select = QueryBuilder.select().from("facilities");
        select.where(QueryBuilder.eq("id", id));

        return cassandraOps.selectOne(select, Facility.class);
    }
}
