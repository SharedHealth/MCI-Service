package org.sharedhealth.mci.domain.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;

import static java.lang.System.currentTimeMillis;

public class BaseRepository {

    protected static long TIMEOUT_IN_MILLIS = 10;
    protected static int PER_PAGE_LIMIT = 10000;
    protected static int MAXIMUM_RECORD = 25;
    protected CassandraOperations cassandraOps;

    public BaseRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        this.cassandraOps = cassandraOps;
    }

    public int getPerPageMaximumLimit() {
        return MAXIMUM_RECORD;
    }

    public long getCurrentTimeInMicros() {
        return currentTimeMillis() * 1000;
    }
}
