package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FeedControllerTest {

    @Mock
    private MCIProperties properties;
    @Mock
    private LocalValidatorFactoryBean validatorFactory;

    private MockMvc mockMvc;
    private FeedController feedController;

    @Before
    public void setup() throws ParseException {
        initMocks(this);
        this.feedController = new FeedController(null, properties);

        mockMvc = MockMvcBuilders
                .standaloneSetup(feedController)
                .setValidator(validatorFactory)
                .build();
    }

    @Test
    public void shouldBuildServerUrl() throws Exception {
        String serverUrl = "https://mci.dghs.com";
        when(properties.getServerUrl()).thenReturn(serverUrl);

        HttpServletRequest request = buildHttpRequest("mci.dghs.com");
        String url = feedController.buildServerUrl(request);
        assertEquals(serverUrl, url);

        request = buildHttpRequest("www.test.com");
        url = feedController.buildServerUrl(request);
        assertEquals("http://www.test.com:8088", url);
    }

    private MockHttpServletRequest buildHttpRequest(String host) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName(host);
        request.setServerPort(8088);
        request.setMethod("GET");
        request.setRequestURI("/api/v1/catchments/102030/patients");
        return request;
    }
}