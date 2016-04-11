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
        HttpServletRequest request = buildHttpRequest("https", "mci.dghs.com", null, "/api/v1/catchments/102030/patients");
        String url = feedController.buildUrl(request);
        assertEquals("https://mci.dghs.com/api/v1/catchments/102030/patients", url);

        request = buildHttpRequest("http", "www.test.com", 8088, "/api/v1/catchments/102030/patients");
        url = feedController.buildUrl(request);
        assertEquals("http://www.test.com:8088/api/v1/catchments/102030/patients", url);
    }

    private MockHttpServletRequest buildHttpRequest(String scheme, String host, Integer port, String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme(scheme);
        request.setServerName(host);
        if (port != null) request.setServerPort(port);
        request.setMethod("GET");
        request.setRequestURI(uri);
        return request;
    }
}