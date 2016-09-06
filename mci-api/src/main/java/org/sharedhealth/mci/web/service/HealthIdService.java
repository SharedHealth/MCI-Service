package org.sharedhealth.mci.web.service;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.sharedhealth.mci.utils.HttpUtil;
import org.sharedhealth.mci.web.exception.UnauthorizedException;
import org.sharedhealth.mci.web.infrastructure.registry.HealthIdWebClient;
import org.sharedhealth.mci.web.infrastructure.security.IdentityServiceClient;
import org.sharedhealth.mci.web.model.MciHealthIdStore;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static java.util.Arrays.asList;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.CF_PATIENT;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.HEALTH_ID;
import static org.sharedhealth.mci.utils.HttpUtil.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class HealthIdService {
    private static final Logger logger = getLogger(HealthIdService.class);
    private static final String CHARSET_ENCODING = "UTF-8";
    private static final String USED_AT_KEY = "used_at";
    private static final String ACCEPTED = "Accepted";

    private MciHealthIdStore mciHealthIdStore;
    private IdentityServiceClient identityServiceClient;
    private HealthIdWebClient healthIdWebClient;
    private MCIProperties mciProperties;
    private CassandraOperations cqlTemplate;
    private ApplicationContext applicationContext;

    @Autowired
    public HealthIdService(MciHealthIdStore mciHealthIdStore, IdentityServiceClient identityServiceClient,
                           HealthIdWebClient healthIdWebClient, MCIProperties mciProperties,
                           @Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations, ApplicationContext applicationContext) {
        this.mciHealthIdStore = mciHealthIdStore;
        this.identityServiceClient = identityServiceClient;
        this.healthIdWebClient = healthIdWebClient;
        this.mciProperties = mciProperties;
        this.cqlTemplate = cassandraOperations;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void populateHidStore() throws IOException {
        List<String> healthIdBlock = getExistingHIDsFromFile();
        Select selectHIDsQuery = select().column(HEALTH_ID).from(CF_PATIENT);
        selectHIDsQuery.where(in(HEALTH_ID, healthIdBlock));

        ResultSet resultSet = cqlTemplate.query(selectHIDsQuery);
        while (!resultSet.isExhausted()) {
            healthIdBlock.remove(resultSet.one().getString(HEALTH_ID));
        }
        mciHealthIdStore.addMciHealthIds(healthIdBlock);
    }

    @PreDestroy
    public void persistHIDsToFile() throws IOException {
        if (mciHealthIdStore.noOfHIDsLeft() > 0) {
            String hidsContent = new ObjectMapper().writeValueAsString(mciHealthIdStore.getAll());
            IOUtils.write(hidsContent, new FileOutputStream(mciProperties.getHidLocalStoragePath()), CHARSET_ENCODING);
        }
    }

    public String getNextHealthId() throws InterruptedException {
        return mciHealthIdStore.getNextHealthId();
    }

    public void putBackHealthId(String healthId) {
        mciHealthIdStore.addMciHealthIds(asList(healthId));
    }

    public void replenishIfNeeded() throws IOException {
        if (mciHealthIdStore.noOfHIDsLeft() > mciProperties.getHealthIdBlockSizeThreshold()) return;
        logger.debug("Replenish, Remaining Health IDs :" + mciHealthIdStore.noOfHIDsLeft());
        List hidBlock = getNextBlockFromHidService();
        if (null != hidBlock) {
            mciHealthIdStore.addMciHealthIds(hidBlock);
            persistHIDsToFile();
        }
    }

    public void markUsed(String healthId) {
        String markUsedUrl = String.format(mciProperties.getHidServiceMarkUsedUrlPattern(), healthId);
        HttpHeaders headers = HttpUtil.getHIDServiceHeaders(mciProperties);
        try {
            headers.add(AUTH_TOKEN_KEY, identityServiceClient.getOrCreateToken());
            Map<String, String> map = new HashMap<>();
            map.put(USED_AT_KEY, TimeUuidUtil.uuidForDate(new Date()).toString());
            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(map, headers);

            String response = healthIdWebClient.markUsed(markUsedUrl, httpEntity);
            if (ACCEPTED.equalsIgnoreCase(response)) return;
            logger.error(String.format("HID service rejected HealthId [%s].", healthId));
        } catch (UnauthorizedException e) {
            logger.info("Token expired");
            identityServiceClient.clearToken();
        } catch (Exception e) {
            logger.error("Can not mark helthID as used", e);
            //move to failed events
        }
    }

    public Map validateHIDForOrg(String hid, String orgCode) {
        String message = String.format("Can not fetch information about HealthId [%s]", hid);
        String checkHIDUrl = String.format(mciProperties.getHidServiceCheckHIDUrlPattern(), hid, orgCode);
        HttpHeaders headers = HttpUtil.getHIDServiceHeaders(mciProperties);
        try {
            String token = identityServiceClient.getOrCreateToken();
            headers.add(AUTH_TOKEN_KEY, token);
            return healthIdWebClient.validateHID(checkHIDUrl, new HttpEntity<>(headers));
        } catch (UnauthorizedException e) {
            logger.info("Token expired");
            identityServiceClient.clearToken();
        } catch (Exception e) {
            logger.error(message, e);
        }
        Map<String, Object> map = new HashMap<>();
        map.put(AVAILABILITY_KEY, Boolean.FALSE);
        map.put(REASON_KEY, message);
        return map;
    }

    private List getNextBlockFromHidService() {
        HttpHeaders headers = HttpUtil.getHIDServiceHeaders(mciProperties);
        try {
            String token = identityServiceClient.getOrCreateToken();
            headers.add(AUTH_TOKEN_KEY, token);
            return healthIdWebClient.getNextHealthIDs(getHidServiceNextBlockURL(), new HttpEntity<>(headers));
        } catch (UnauthorizedException e) {
            logger.info("Token expired");
            identityServiceClient.clearToken();
        } catch (Exception e) {
            logger.error("Can not fetch next block of HIDs", e);
        }

        return null;
    }

    private String getHidServiceNextBlockURL() {
        return String.format(mciProperties.getHidServiceNextBlockUrlPattern(),
                mciProperties.getIdpClientId(), mciProperties.getHealthIdBlockSize());
    }

    private List<String> getExistingHIDsFromFile() throws IOException {
        if (!new File(mciProperties.getHidLocalStoragePath()).exists()) return new ArrayList<>();
        try {
            String content = IOUtils.toString(new FileInputStream(mciProperties.getHidLocalStoragePath()), "UTF-8");
            String[] hids = new ObjectMapper().readValue(content, String[].class);
            return Lists.newArrayList(hids);
        } catch (IOException e) {
            e.printStackTrace();
            SpringApplication.exit(applicationContext);
        }
        return new ArrayList<>();
    }
}
