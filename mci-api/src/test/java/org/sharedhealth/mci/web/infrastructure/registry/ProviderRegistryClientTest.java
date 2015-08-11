package org.sharedhealth.mci.web.infrastructure.registry;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.ProviderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class ProviderRegistryClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private ProviderRegistryClient prClient;

    @Test
    public void shouldFindProviderById() {
        String providerId = "113068";

        givenThat(get(urlEqualTo("/api/1.0/providers/" + providerId + ".json"))
                .withHeader(CLIENT_ID_KEY, equalTo("18554"))
                .withHeader(AUTH_TOKEN_KEY, equalTo("b43d2b284fa678fb8248b7cc3ab391f9c21e5d7f8e88f815a9ef4346e426bd33"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/provider.json"))));

        ProviderResponse provider = prClient.find(providerId);
        assertNotNull(provider);
        assertEquals(providerId, provider.getId());
        assertEquals("Dr. Monika", provider.getName());
    }
}