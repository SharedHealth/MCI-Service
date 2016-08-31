package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.utils.HttpUtil;
import org.sharedhealth.mci.web.exception.UnauthorizedException;
import org.sharedhealth.mci.web.infrastructure.registry.HealthIdWebClient;
import org.sharedhealth.mci.web.infrastructure.security.IdentityServiceClient;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.sharedhealth.mci.web.model.MciHealthIdStore;
import org.sharedhealth.mci.web.model.OrgHealthId;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Arrays.asList;
import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class HealthIdService {
    private static final Logger logger = getLogger(HealthIdService.class);

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

    public MciHealthId getNextHealthId() throws InterruptedException {
        return new MciHealthId(mciHealthIdStore.getNextHealthId());
    }

    public void putBackHealthId(MciHealthId mciHealthId) {
        mciHealthIdStore.addMciHealthIds(asList(mciHealthId.getHid()));
    }

    public void replenishIfNeeded() {
        logger.debug("Replenish, Remaining Health IDs :" + mciHealthIdStore.noOfHIDsLeft());
        List hidBlock = getNextBlockFromHidService();
        if(null != hidBlock){
            mciHealthIdStore.addMciHealthIds(hidBlock);
        }
    }

    public void markUsed(MciHealthId nextMciHealthId) {
    }

    public OrgHealthId findOrgHealthId(String healthId) {
        return null;
    }

    public void markOrgHealthIdUsed(OrgHealthId orgHealthId) {
    }

    private List getNextBlockFromHidService() {
        String hidServiceNextBlockURL = getHidServiceNextBlockURL();
        HttpHeaders headers = HttpUtil.getHIDServiceHeaders(mciProperties);
        try {
            String token = identityServiceClient.getOrCreateToken();
            headers.add(AUTH_TOKEN_KEY, token);
            return healthIdWebClient.getNextHealthIDs(hidServiceNextBlockURL, headers);
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

}
