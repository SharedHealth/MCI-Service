package org.sharedhealth.mci.web.infrastructure.persistence;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;

public class BaseRepository {

    protected static long TIMEOUT_IN_MILLIS = 10;
    protected static int PER_PAGE_LIMIT = 10000;
    protected static int MAXIMUM_RECORD = 25;
    protected CassandraOperations cassandraOperations;

    public BaseRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        this.cassandraOperations = cassandraOperations;
    }

    public int getPerPageMaximumLimit() {
        return MAXIMUM_RECORD;
    }
}
