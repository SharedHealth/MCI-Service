package org.sharedhealth.mci.web.infrastructure.registry;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.infrastructure.WebClient;
import org.sharedhealth.mci.web.mapper.ProviderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.AsyncRestTemplate;

import java.nio.file.ProviderNotFoundException;
import java.util.concurrent.ExecutionException;

import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;


@Component
public class ProviderRegistryClient extends WebClient<ProviderResponse> {

    @Autowired
    public ProviderRegistryClient(@Qualifier("MCIRestTemplate") AsyncRestTemplate restTemplate, MCIProperties properties) {
        super(restTemplate, properties);
    }

    public ProviderResponse find(String providerId) {
        String url = properties.getProviderRegistryUrl() + "/" + providerId + ".json";
        try {
            return getResponse(url);

        } catch (InterruptedException | ExecutionException e) {
            throw new ProviderNotFoundException("No provider found with URL: " + url);
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
