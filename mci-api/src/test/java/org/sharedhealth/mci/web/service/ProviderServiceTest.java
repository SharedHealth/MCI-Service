package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.registry.ProviderRegistryClient;
import org.sharedhealth.mci.web.mapper.ProviderResponse;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProviderServiceTest {

    @Mock
    private ProviderRegistryClient prClient;

    private ProviderService providerService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        providerService = new ProviderService(prClient);
    }

    @Test
    public void shouldFindProviderById() throws Exception {
        String providerId = "p100";
        String providerName = "Dr. Monika";
        ProviderResponse provider = new ProviderResponse();
        provider.setId(providerId);
        provider.setName(providerName);
        when(prClient.find(providerId)).thenReturn(provider);

        ProviderResponse providerResponse = providerService.find(providerId);
        assertNotNull(providerResponse);
        assertEquals(providerId, providerResponse.getId());
        assertEquals(providerName, providerResponse.getName());
    }
}