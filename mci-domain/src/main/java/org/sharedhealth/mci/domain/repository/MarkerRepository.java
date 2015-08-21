package org.sharedhealth.mci.domain.repository;

import org.sharedhealth.mci.domain.model.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.CF_MARKER;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.TYPE;

@Component
public class MarkerRepository extends BaseRepository {

    @Autowired
    public MarkerRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public String find(String markerType) {
        String cql = select().from(CF_MARKER).where(eq(TYPE, markerType)).limit(1).toString();
        List<Marker> markers = cassandraOps.select(cql, Marker.class);
        if (isEmpty(markers)) {
            return null;
        }
        return markers.get(0).getMarker();
    }
}
