package org.sharedhealth.mci.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MCIProperties {

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
    @Value("${FR_URL}")
    private String frUrl;
    @Value("${LR_URL}")
    private String LrUrl;
    @Value("${IDENTITY_SERVER_BASE_URL}")
    private String identityServerBaseUrl;
    @Value("${FR_CACHE_TTL}")
    private int frCacheTtl;
    @Value("${IDP_CLIENT_ID}")
    private String idpClientId;
    @Value("${IDP_AUTH_TOKEN}")
    private String idpAuthToken;
    @Value("${WORKER_ID}")
    private String workerId;
    @Value("${SERVER_URL}")
    private String serverUrl;
    @Value("${PR_URL}")
    private String providerRegistryUrl;

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

    public String getFacilityRegistryUrl() {
        return frUrl;
    }

    public String getIdentityServerBaseUrl() {
        return identityServerBaseUrl;
    }

    public String getLocaitonRegistryUrl() {
        return LrUrl;
    }

    public int getFrCacheTtl() {
        return frCacheTtl;
    }

    public String getIdpClientId() {
        return idpClientId;
    }

    public String getIdpAuthToken() {
        return idpAuthToken;
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getProviderRegistryUrl() {
        return providerRegistryUrl;
    }
}
