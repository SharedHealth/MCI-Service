package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.infrastructure.registry.ProviderRegistryClient;
import org.sharedhealth.mci.web.mapper.ProviderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import static org.sharedhealth.mci.web.config.MCICacheConfiguration.PROVIDER_CACHE;

@Component
public class ProviderService {

    private ProviderRegistryClient client;

    @Autowired
    public ProviderService(ProviderRegistryClient client) {
        this.client = client;
    }

    @Cacheable(value = PROVIDER_CACHE, unless = "#result == null")
    public ProviderResponse find(String providerId) {
        return client.find(providerId);
    }
}
