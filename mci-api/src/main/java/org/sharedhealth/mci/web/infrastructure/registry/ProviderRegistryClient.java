package org.sharedhealth.mci.web.infrastructure.registry;

import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.web.infrastructure.WebClient;
import org.sharedhealth.mci.web.mapper.ProviderResponse;
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
public class ProviderRegistryClient extends WebClient<ProviderResponse> {
    private static final Logger logger = LoggerFactory.getLogger(ProviderRegistryClient.class);

    @Autowired
    public ProviderRegistryClient(@Qualifier("MCIRestTemplate") AsyncRestTemplate restTemplate, MCIProperties properties) {
        super(restTemplate, properties);
    }

    public ProviderResponse find(String providerId) {
        String url = properties.getProviderRegistryUrl() + "/" + providerId + ".json";
        try {
            return getResponse(url);

        } catch (RestClientException | InterruptedException | ExecutionException e) {
            logger.error("No provider found with URL: " + url, e);
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
