package org.sharedhealth.mci.domain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.lang.Boolean.valueOf;

@Component
public class MCIProperties {

    public static final String DIAGNOSTICS_SERVLET_PATH = "/diagnostics/health";

    @Value("${CASSANDRA_KEYSPACE}")
    private String cassandraKeySpace;
    @Value("${CASSANDRA_HOST}")
    private String cassandraHost;
    @Value("${CASSANDRA_PORT}")
    private int cassandraPort;

    @Value("${CASSANDRA_USER}")
    private String cassandraUser;

    @Value("${CASSANDRA_PASSWORD}")
    private String cassandraPassword;
    @Value("${CASSANDRA_TIMEOUT}")
    private int cassandraTimeout;
    @Value("${REST_POOL_SIZE}")
    private int restPoolSize;
    @Value("${API_VERSION}")
    private String apiVersion;

    @Value("${IS_LATEST_API_VERSION}")
    private String isLatestApiVersion;
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
    @Value("${MCI_INVALID_HID_PATTERN}")
    private String mciInvalidHidPattern;
    @Value("${ORG_INVALID_HID_PATTERN}")
    private String orgInvalidHidPattern;

    @Value("${MCI_START_HID}")
    private String mciStartHid;
    @Value("${MCI_END_HID}")
    private String mciEndHid;

    @Value("${HEALTH_ID_BLOCK_SIZE}")
    private String healthIdBlockSize;

    @Value("${HEALTH_ID_BLOCK_SIZE_THRESHOLD}")
    private String healthIdBlockSizeThreshold;

    @Value("${SEARCH_MAPPING_TASK_BLOCK_SIZE}")
    private String searchMappingTaskBlockSize;

    @Value("${MAX_FAILED_EVENTS}")
    private String maxFailedEvents;

    @Value("${FAILED_EVENT_RETRY_LIMIT}")
    private String failedEventRetryLimit;

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

    public String getApiVersion() {
        return apiVersion;
    }

    public boolean isLatestApiVersion() {
        return valueOf(isLatestApiVersion);
    }

    public String getCassandraUser() {
        return cassandraUser;
    }

    public String getCassandraPassword() {
        return cassandraPassword;
    }

    public String getMciInvalidHidPattern() {
        return mciInvalidHidPattern;
    }

    public Long getMciEndHid() {
        return Long.valueOf(mciEndHid);
    }

    public Long getMciStartHid() {
        return Long.valueOf(mciStartHid);
    }

    public void setMciInvalidHidPattern(String mciInvalidHidPattern) {
        this.mciInvalidHidPattern = mciInvalidHidPattern;
    }

    public void setMciStartHid(String mciStartHid) {
        this.mciStartHid = mciStartHid;
    }

    public void setMciEndHid(String mciEndHid) {
        this.mciEndHid = mciEndHid;
    }

    public int getHealthIdBlockSize() {
        return Integer.parseInt(healthIdBlockSize);
    }

    public int getHealthIdBlockSizeThreshold() {
        return Integer.parseInt(healthIdBlockSizeThreshold);
    }

    public void setHealthIdBlockSizeThreshold(String healthIdBlockSizeThreshold) {
        this.healthIdBlockSizeThreshold = healthIdBlockSizeThreshold;
    }

    public void setHealthIdBlockSize(String healthIdBlockSize) {
        this.healthIdBlockSize = healthIdBlockSize;
    }

    public int getSearchMappingTaskBlockSize() {
        return Integer.parseInt(searchMappingTaskBlockSize);
    }

    public int getMaxFailedEvents() {
        return Integer.parseInt(maxFailedEvents);
    }

    public int getFailedEventRetryLimit() {
        return Integer.parseInt(failedEventRetryLimit);
    }

    public String getOrgInvalidHidPattern() {
        return orgInvalidHidPattern;
    }

    public void setOrgInvalidHidPattern(String orgInvalidHidPattern) {
        this.orgInvalidHidPattern = orgInvalidHidPattern;
    }


}
