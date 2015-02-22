package org.sharedhealth.mci.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MCIProperties {

    public static final String SECURITY_TOKEN_HEADER = "X-Auth-Token";
    public static final String DIAGNOSTICS_SERVLET_PATH = "/diagnostics/health";

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
    @Value("${IDENTITY_SERVER_BASE_URL}")
    private String identityServerBaseUrl;
    @Value("${FR_CACHE_TTL}")
    private int frCacheTtl;

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

    public String getIdentityServerBaseUrl(){
        return identityServerBaseUrl;
    }

    public String getLocaitonRegistryToken() {
        return LrToken;
    }

    public String getLocaitonRegistryUrl() {
        return LrUrl;
    }

    public int getFrCacheTtl() {
        return frCacheTtl;
    }
}
