package org.sharedhealth.mci.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MCIProperties {

    @Value("${CASSANDRA_KEYSPACE}")
    private String cassandraKeySpace;
    @Value("${CASSANDRA_HOST}")
    private String cassandraHost;
    @Value("${CASSANDRA_PORT}")
    private int cassandraPort;
    @Value("${CASSANDRA_TIMEOUT}")
    private int cassandraTimeout;
    @Value("${REST_POOL_SIZE}")
    private int restPoolSize;

    public String getCassandraKeySpace() {
        return cassandraKeySpace;
    }

    public String getContactPoints() {
        return cassandraHost;
    }

    public int getCassandraPort() {
        return cassandraPort;
    }

    public int getCassandraTimeout() {
        return cassandraTimeout;
    }

    public int getRestPoolSize() {
        return restPoolSize;
    }
}
