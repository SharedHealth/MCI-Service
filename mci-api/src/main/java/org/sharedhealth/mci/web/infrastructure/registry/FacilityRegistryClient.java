package org.sharedhealth.mci.web.infrastructure.registry;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.infrastructure.WebClient;
import org.sharedhealth.mci.web.mapper.FacilityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.concurrent.ExecutionException;

import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;


@Component
public class FacilityRegistryClient extends WebClient<FacilityResponse> {
    private static final Logger logger = LoggerFactory.getLogger(FacilityRegistryClient.class);

    @Autowired
    public FacilityRegistryClient(@Qualifier("MCIRestTemplate") AsyncRestTemplate restTemplate, MCIProperties properties) {
        super(restTemplate, properties);
    }

    public FacilityResponse find(String facilityId) {
        String url = properties.getFacilityRegistryUrl() + "/" + facilityId + ".json";
        try {
            return getResponse(url);

        } catch (RestClientException | InterruptedException | ExecutionException e) {
            logger.debug("No facility found with URL: " + url, e);
            return null;
        }
    }

    @Override
    protected HttpEntity buildHeaders() {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(AUTH_TOKEN_KEY, properties.getIdpAuthToken());
        header.add(CLIENT_ID_KEY, properties.getIdpClientId());
        return new HttpEntity(header);
    }
}
