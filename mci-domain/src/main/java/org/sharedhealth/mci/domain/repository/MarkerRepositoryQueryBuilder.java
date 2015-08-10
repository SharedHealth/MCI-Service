package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import org.sharedhealth.mci.domain.model.Marker;
import org.springframework.data.cassandra.convert.CassandraConverter;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.lang.System.currentTimeMillis;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.CF_MARKER;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.TYPE;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

public class MarkerRepositoryQueryBuilder {

    private static final long QUERY_EXEC_DELAY = 100;

    public static void buildUpdateMarkerBatch(String type, String marker, CassandraConverter converter, Batch batch) {
        long timeInMicros = currentTimeMillis() * 1000;

        Delete delete = delete().from(CF_MARKER);
        delete.where(eq(TYPE, type));
        delete.using(timestamp(timeInMicros));
        batch.add(delete);

        Marker newMarker = new Marker();
        newMarker.setType(type);
        newMarker.setCreatedAt(timeBased());
        newMarker.setMarker(marker);
        Insert insert = createInsertQuery(CF_MARKER, newMarker, null, converter);
        insert.using(timestamp(timeInMicros + QUERY_EXEC_DELAY));
        batch.add(insert);
    }
}
