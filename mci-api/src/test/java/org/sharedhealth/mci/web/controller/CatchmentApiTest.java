package org.sharedhealth.mci.web.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfig;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class CatchmentApiTest extends BaseControllerTest {

    private static final String FACILITY_ID = "10010408";
    private static final int RECORDS_COUNT = 10;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void shouldReturnAllPatientsBelongsToSpecificLocation() throws Exception {

        initializeFacilityRegistry(FACILITY_ID, 200, "jsons/facility.json");
        generatePatientRecords();

        MvcResult result = mockMvc.perform(get(API_END_POINT + "/facility/" + FACILITY_ID).accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final MCIMultiResponse body = getMciMultiResponse(result);

        Assert.assertEquals(200, body.getHttpStatus());
        Assert.assertEquals(RECORDS_COUNT, body.getResults().size());

    }

    private void generatePatientRecords() throws Exception {
        String json = asString("jsons/patient/required_only_payload.json");
        for (int x = 0; x < RECORDS_COUNT; x++) {
            createPatient(json);
        }
    }

    @Test
    public void shouldReturnNotFoundResponseWhenFacilityNotExist() throws Exception {
        initializeFacilityRegistry("1", 404, "jsons/response/error_facility_404.json");

        mockMvc.perform(get(API_END_POINT + "/facility/1").accept(APPLICATION_JSON_UTF8).contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(asString("jsons/response/error_facility_404.json")))
                .andReturn();

    }

    private void initializeFacilityRegistry(String facility, int status, String path) {
        givenThat(WireMock.get(urlEqualTo("/api/1.0/facilities/" + facility + ".json"))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString(path))));
    }
}
