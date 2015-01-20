package org.sharedhealth.mci.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MCIProperties {

    public static final int ONE_DAY = 86400;

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
    @Value("${FR_TOKEN}")
    private String frToken;
    @Value("${FR_URL}")
    private String frUrl;
    @Value("${LR_TOKEN}")
    private String LrToken;
    @Value("${LR_URL}")
    private String LrUrl;

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

    public String getFacilityRegistryToken() {
        return frToken;
    }

    public String getFacilityRegistryUrl() {
        return frUrl;
    }

    public String getLocaitonRegistryToken() {
        return LrToken;
    }

    public String getLocaitonRegistryUrl() {
        return LrUrl;
    }

}
