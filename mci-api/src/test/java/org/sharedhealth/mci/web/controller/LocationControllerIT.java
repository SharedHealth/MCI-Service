package org.sharedhealth.mci.web.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.utils.HttpUtil.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class LocationControllerIT extends BaseControllerTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Mock
    private LocationService locationService;
    public static final String API_END_POINT = "/api/v1/locations";


    @Autowired
    private Filter springSecurityFilterChain;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        validClientId = "6";
        validEmail = "some@thoughtworks.com";
        validAccessToken = "2361e0a8-f352-4155-8415-32adfb8c2472";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailsWithAllRoles.json"))));
    }

    @Test
    public void shouldReturnOkResponseIfLocationNotExist() throws Exception {

        MvcResult result = mockMvc.perform(get(API_END_POINT + "?parent=11")
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertEquals("[]", body.getResults().toString());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnOkResponseAndAllTheDivisionsIfNoParentGiven() throws Exception {

        MvcResult result = mockMvc.perform(get(API_END_POINT)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertNotSame("[]", body.getResults());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnOkResponseAndReturnDistrictIfDivisionParentMatch() throws Exception {

        String districtParent = "10";

        MvcResult result = mockMvc.perform(get(API_END_POINT + "?parent=" + districtParent)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertNotSame("[]", body.getResults());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnOkResponseAndReturnUpazilaIfDistrictParentMatch() throws Exception {

        String upazilaParent = "1004";

        MvcResult result = mockMvc.perform(get(API_END_POINT + "?parent=" + upazilaParent)
                .accept(APPLICATION_JSON).contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertNotSame("[]", body.getResults());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnOkResponseAndReturnCityCorporationIfUpazilaParentMatch() throws Exception {

        String cityCorporationParent = "100409";

        MvcResult result = mockMvc.perform(get(API_END_POINT + "?parent=" + cityCorporationParent)
                .accept(APPLICATION_JSON).contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertNotSame("[]", body.getResults());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnOkResponseAndReturnUnionIfCityCorporationCodeMatch() throws Exception {

        String unionParent = "10040920";

        MvcResult result = mockMvc.perform(get(API_END_POINT + "?parent=" + unionParent)
                .accept(APPLICATION_JSON).contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertNotSame("[]", body.getResults());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnOkResponseAndReturnWardIfUnionParentMatch() throws Exception {

        String wardParent = "1004092005";

        MvcResult result = mockMvc.perform(get(API_END_POINT + "?parent=" + wardParent)
                .accept(APPLICATION_JSON).contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        //@Todo Need to change when 6th level data available
        Assert.assertEquals("[]", body.getResults().toString());
        Assert.assertEquals(200, body.getHttpStatus());
    }
}
