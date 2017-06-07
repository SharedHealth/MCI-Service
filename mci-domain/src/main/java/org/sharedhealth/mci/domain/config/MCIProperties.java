package org.sharedhealth.mci.domain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.lang.Boolean.valueOf;
import static org.sharedhealth.mci.domain.util.StringUtil.ensureSuffix;
import static org.sharedhealth.mci.domain.util.StringUtil.removePrefix;

@Component
public class MCIProperties {

    public static final String DIAGNOSTICS_SERVLET_PATH = "/diagnostics/health";
    private final String URL_SEPARATOR = "/";

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
    @Value("${WORKER_ID}")
    private String workerId;
    @Value("${SERVER_URL}")
    private String serverUrl;

    @Value("${FR_URL}")
    private String frUrl;
    @Value("${FR_CACHE_TTL}")
    private int frCacheTtl;
    @Value("${PR_URL}")
    private String providerRegistryUrl;

    @Value("${IDENTITY_SERVER_BASE_URL}")
    private String identityServerBaseUrl;
    @Value("${IDENTITY_SERVER_SIGNIN_PATH}")
    private String identityServerSignInPath;
    @Value("${IDENTITY_SERVER_USER_INFO_PATH}")
    private String identityServerUserInfoPath;
    @Value("${IDP_CLIENT_ID}")
    private String idpClientId;
    @Value("${IDP_AUTH_TOKEN}")
    private String idpAuthToken;
    @Value("${IDP_CLIENT_EMAIL}")
    private String idpClientEmail;
    @Value("${IDP_CLIENT_PASSWORD}")
    private String idpClientPassword;

    @Value("${MCI_ORG_CODE}")
    private String mciOrgCode;

    @Value("${HID_SERVICE_BASE_URL}")
    private String hidServiceBaseUrl;
    @Value("${HID_SERVICE_NEXT_BLOCK_URL}")
    private String hidServiceNextBlockPathPattern;
    @Value("${HID_SERVICE_CHECK_HID_URL}")
    private String checkHIDUrlPattern;
    @Value("${HID_LOCAL_STORAGE_PATH}")
    private String hidLocalStoragePath;
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

    @Value("${IS_MCI_MASTER_NODE}")
    private String isMCIMasterNode;

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

    public String getIdentityServerUserInfoUrl() {
        return ensureSuffix(identityServerBaseUrl, URL_SEPARATOR) + ensureSuffix(removePrefix(identityServerUserInfoPath, URL_SEPARATOR), URL_SEPARATOR);
    }

    public String getIdentityServerSignInUrl() {
        return ensureSuffix(identityServerBaseUrl, URL_SEPARATOR) + removePrefix(identityServerSignInPath, URL_SEPARATOR);
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

    public String getIdpClientEmail() {
        return idpClientEmail;
    }

    public String getIdpClientPassword() {
        return idpClientPassword;
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

    public String getHidServiceNextBlockUrlPattern() {
        return ensureSuffix(hidServiceBaseUrl, URL_SEPARATOR) +
                removePrefix(hidServiceNextBlockPathPattern, URL_SEPARATOR);
    }

    public String getHidServiceCheckHIDUrlPattern() {
        return ensureSuffix(hidServiceBaseUrl, URL_SEPARATOR) +
                removePrefix(checkHIDUrlPattern, URL_SEPARATOR);
    }

    public String getHidLocalStoragePath() {
        return hidLocalStoragePath;
    }

    public int getHealthIdBlockSize() {
        return Integer.parseInt(healthIdBlockSize);
    }

    public int getHealthIdBlockSizeThreshold() {
        return Integer.parseInt(healthIdBlockSizeThreshold);
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

    public String getMciOrgCode() {
        return mciOrgCode;
    }

    public boolean getIsMCIMasterNode() {
        return Boolean.parseBoolean(isMCIMasterNode);
    }


}
