package org.sharedhealth.mci.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.sharedhealth.mci.utils.HttpUtil;
import org.sharedhealth.mci.web.exception.UnauthorizedException;
import org.sharedhealth.mci.web.infrastructure.registry.HealthIdWebClient;
import org.sharedhealth.mci.web.infrastructure.security.IdentityServiceClient;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.sharedhealth.mci.web.model.MciHealthIdStore;
import org.sharedhealth.mci.web.model.OrgHealthId;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
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
    private final MCIProperties mciProperties;

    @Autowired
    public HealthIdService(MciHealthIdStore mciHealthIdStore, IdentityServiceClient identityServiceClient, HealthIdWebClient healthIdWebClient, MCIProperties mciProperties) {
        this.mciHealthIdStore = mciHealthIdStore;
        this.identityServiceClient = identityServiceClient;
        this.healthIdWebClient = healthIdWebClient;
        this.mciProperties = mciProperties;
    }

    @PostConstruct
    public void populateHidStore() throws IOException {
        List<String> healthIdBlock = getExistingHIDsFromFile();
        mciHealthIdStore.addMciHealthIds(healthIdBlock);
    }

    public MciHealthId getNextHealthId() throws InterruptedException {
        return new MciHealthId(mciHealthIdStore.getNextHealthId());
    }

    public void putBackHealthId(MciHealthId mciHealthId) {
        mciHealthIdStore.addMciHealthIds(asList(mciHealthId.getHid()));
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

    public void markUsed(MciHealthId nextMciHealthId) {
        String markUsedUrl = String.format(mciProperties.getHidServiceMarkUsedUrlPattern(), nextMciHealthId.getHid());
        HttpHeaders headers = HttpUtil.getHIDServiceHeaders(mciProperties);
        try {
            headers.add(AUTH_TOKEN_KEY, identityServiceClient.getOrCreateToken());
            Map<String, String> map = new HashMap<>();
            map.put(USED_AT_KEY, TimeUuidUtil.uuidForDate(new Date()).toString());
            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(map, headers);

            String response = healthIdWebClient.markUsed(markUsedUrl, httpEntity);
            if (ACCEPTED.equalsIgnoreCase(response)) return;
            logger.error("HID service rejected this Health Id.");
        } catch (UnauthorizedException e) {
            logger.info("Token expired");
            identityServiceClient.clearToken();
        } catch (Exception e) {
            logger.error("Can not mark helthID as used", e);
            //move to failed events
        }
    }

    public OrgHealthId findOrgHealthId(String healthId) {
        return null;
    }

    public void markOrgHealthIdUsed(OrgHealthId orgHealthId) {
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
        try {
            String content = IOUtils.toString(new FileInputStream(mciProperties.getHidLocalStoragePath()), "UTF-8");
            String[] hids = new ObjectMapper().readValue(content, String[].class);
            return Arrays.asList(hids);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    private void persistHIDsToFile() throws IOException {
        String hidsContent = new ObjectMapper().writeValueAsString(mciHealthIdStore.getAll());
        IOUtils.write(hidsContent, new FileOutputStream(mciProperties.getHidLocalStoragePath()), CHARSET_ENCODING);
    }

}
