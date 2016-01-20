package org.sharedhealth.mci.web.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.utils.HttpUtil.*;
import static org.sharedhealth.mci.web.controller.HealthIdController.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
public class HealthIdControllerIT extends BaseControllerTest {

    private static final String API_END_POINT = "/healthIds";
    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    private Filter springSecurityFilterChain;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setUpMockMvcBuilder();
    }

    @Test
    public void testGenerate() throws Exception {
        validAccessToken = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        validClientId = "18564";
        validEmail = "MciAdmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIAdmin.json"))));


        mockMvc.perform(post(API_END_POINT + GENERATE_ALL_URI)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void testGenerateRange() throws Exception {
        validAccessToken = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        validClientId = "18564";
        validEmail = "MciAdmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIAdmin.json"))));


        mockMvc.perform(post(API_END_POINT + GENERATE_BLOCK_URI +"?start=9800100100&totalHIDs=1000")
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void testGetNextBlock() throws Exception {
        validAccessToken = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        validClientId = "18564";
        validEmail = "MciAdmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIAdmin.json"))));


        mockMvc.perform(get(API_END_POINT + NEXT_BLOCK_URI)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testGenerateOnlyForAdmins() throws Exception {
        validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f02";
        validClientId = "18548";
        validEmail = "facility@gmail.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForFacility.json"))));

        mockMvc.perform(post(API_END_POINT + GENERATE_ALL_URI)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void testGetNextOnlyForAdmins() throws Exception {
        validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f02";
        validClientId = "18548";
        validEmail = "facility@gmail.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForFacility.json"))));

        mockMvc.perform(get(API_END_POINT + NEXT_BLOCK_URI)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void testGenerateRangeForOrg() throws Exception {
        validAccessToken = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        validClientId = "18564";
        validEmail = "MciAdmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIAdmin.json"))));


        mockMvc.perform(post(API_END_POINT + GENERATE_BLOCK_FOR_ORG_URI + "?org=OTHER&start=9800100100&totalHIDs=1000")
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

    }
}