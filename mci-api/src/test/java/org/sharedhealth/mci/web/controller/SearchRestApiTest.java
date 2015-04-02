package org.sharedhealth.mci.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PatientSummaryData;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.sharedhealth.mci.web.service.LocationService;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.FROM_KEY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class SearchRestApiTest extends BaseControllerTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Mock
    private LocationService locationService;

    private static final String PER_PAGE_MAXIMUM_LIMIT_NOTE = "There are more record for this search criteria. Please" +
            " narrow down your search";
    private static final int PER_PAGE_MAXIMUM_LIMIT = 25;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        setUpMockMvcBuilder();

        validClientId = "6";
        validEmail = "some@thoughtworks.com";
        validAccessToken = "2361e0a8-f352-4155-8415-32adfb8c2472";
        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailsWithAllRoles.json"))));
        createPatientData();
    }

    @Test
    public void shouldReturnBadRequestIfOnlySurNameGiven() throws Exception {

        MvcResult result = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?sur_name=Mazumder")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        JSONAssert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\"," +
                "\"errors\":[{\"code\":1001,\"field\":\"given_name\",\"message\":\"invalid given_name\"}]}", result
                .getResponse().getContentAsString(), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldReturnBadRequestIfOnlyGivenNameGiven() throws Exception {

        MvcResult result = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?given_name=Mazumder")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        String expected = "{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\"," +
                "\"errors\":[{\"code\":1006,\"message\":\"Please provide a valid ID, Household code, Address or Phone number\"}]}";
        JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldReturnBadRequestIfOnlyAddressGiven() throws Exception {

        String present_address = patientData.getAddress().getDivisionId() +
                patientData.getAddress().getDistrictId() + patientData.getAddress().getUpazilaId();

        MvcResult result = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?present_address=" + present_address)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        String expected = "{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\"," +
                "\"errors\":[{\"code\":1006,\"message\":\"Please provide a valid ID, Household code, Name or Phone number\"}]}";
        JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldReturnOkResponseIfPatientNotExistWithGivenNameAndAddress() throws Exception {
        String present_address = patientData.getAddress().getDivisionId() +
                patientData.getAddress().getDistrictId() + patientData.getAddress().getUpazilaId();
        String givenName = "Rajus";

        MvcResult result = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?given_name=" + givenName + "&present_address=" +
                present_address)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertEquals("[]", body.getResults().toString());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnPatientIfGivenNameAndAddressMatchWithAnyPatient() throws Exception {
        String json = asString("jsons/patient/full_payload.json");

        PatientSummaryData original = getPatientSummaryObjectFromString(json);

        MvcResult result = postPatient(json);
        String present_address = original.getAddress().getDivisionId() +
                original.getAddress().getDistrictId() + original.getAddress().getUpazilaId();
        String givenName = "Zaman";

        MvcResult searchResult = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?given_name=" + givenName + "&present_address="
                + present_address)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(searchResult);
        PatientSummaryData patient = getPatientSummaryObjectFromString(mapper.writeValueAsString(body.getResults()
                .iterator().next()));

        original.setHealthId(patient.getHealthId());
        Assert.assertEquals(original, patient);
    }

    @Test
    public void shouldReturnPatientIfGivenNameAndSurNameAndAddressMatchWithCaseSensetiveSupport() throws Exception {
        String json = asString("jsons/patient/full_payload.json");

        PatientSummaryData original = getPatientSummaryObjectFromString(json);

        MvcResult result = postPatient(json);
        String present_address = original.getAddress().getDivisionId() +
                original.getAddress().getDistrictId() + original.getAddress().getUpazilaId();
        String givenName = "zaman";
        String surName = "aymaan";

        MvcResult searchResult = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?given_name=" + givenName +
                "&sur_name=" + surName + "&present_address=" + present_address)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).contentType
                (APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(searchResult);

        PatientSummaryData patient = getPatientSummaryObjectFromString(mapper.writeValueAsString(body.getResults()
                .iterator().next()));

        original.setHealthId(patient.getHealthId());
    }

    @Test
    public void shouldReturnPatientWithAdditionalNoteWhenSearchFindMorePatient() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patientData);

        for (int x = 0; x <= PER_PAGE_MAXIMUM_LIMIT; x++) {
            postPatient(json);
        }
        String present_address = patientData.getAddress().getDivisionId() +
                patientData.getAddress().getDistrictId() + patientData.getAddress().getUpazilaId();
        String givenName = "raju";
        String surName = "mazumder";

        MvcResult result = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?given_name=" + givenName +
                "&sur_name=" + surName + "&present_address=" + present_address)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).contentType
                (APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertEquals("{note=" + PER_PAGE_MAXIMUM_LIMIT_NOTE + "}", body.getAdditionalInfo().toString());

        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnBadRequestIfOnlyExtensionOrCountryCodeOrAreaCodeGiven() throws Exception {


        MvcResult result = mockMvc.perform(get(API_END_POINT_FOR_PATIENT +
                "?country_code=880&area_code=02&extension=122")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        JSONAssert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\"," +
                "\"errors\":[{\"code\":1001,\"field\":\"phone_no\",\"message\":\"invalid phone_no\"}]}", result
                .getResponse().getContentAsString(), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldReturnBadRequestIfOnlyCountryCodeGiven() throws Exception {

        MvcResult result = mockMvc.perform(get(API_END_POINT_FOR_PATIENT +
                "?country_code=880")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        JSONAssert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\"," +
                "\"errors\":[{\"code\":1001,\"field\":\"phone_no\",\"message\":\"invalid phone_no\"}]}", result
                .getResponse().getContentAsString(), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldReturnOkResponseIfPatientNotExistWithPhoneNumber() throws Exception {
        patientData.setHealthId("health-100");
        String present_address = patientData.getAddress().getDivisionId() +
                patientData.getAddress().getDistrictId() + patientData.getAddress().getUpazilaId();

        MvcResult result = mockMvc.perform(get(API_END_POINT_FOR_PATIENT +
                "?phone_no=123456&country_code=880&present_address=" + present_address)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertEquals("[]", body.getResults().toString());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    private void createPatientData() {
        patientData = new PatientData();
        patientData.setGivenName("Raju");
        patientData.setSurName("Mazumder");
        patientData.setGender("M");
        patientData.setDateOfBirth(toIsoFormat("2014-12-01"));
        patientData.setEducationLevel("01");
        patientData.setOccupation("02");
        patientData.setMaritalStatus("1");
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("1716528608");
        phoneNumber.setCountryCode("880");
        phoneNumber.setExtension("02");
        phoneNumber.setAreaCode("01");

        patientData.setPhoneNumber(phoneNumber);
        patientData.setPrimaryContactNumber(phoneNumber);

        Address address = new Address();
        address.setAddressLine("house-12");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazilaId("09");
        address.setCityCorporationId("20");
        address.setVillage("10");
        address.setUnionOrUrbanWardId("01");
        address.setCountryCode("050");

        patientData.setAddress(address);
    }

    @Test
    public void shouldReturnAllTheCreatedPatientIfPhoneNumberMatchBySearch() throws Exception {
        String present_address = patientData.getAddress().getDivisionId() +
                patientData.getAddress().getDistrictId() + patientData.getAddress().getUpazilaId();

        createPatient(patientData);
        createPatient(patientData);
        createPatient(patientData);

        MvcResult result = mockMvc.perform(get(API_END_POINT_FOR_PATIENT +
                "?phone_no=1716528608&country_code=880&present_address=" + present_address)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final MCIMultiResponse body = getMciMultiResponse(result);
        PatientSummaryData patientData1 = (PatientSummaryData) body.getResults().iterator().next();
        Assert.assertEquals("1716528608", patientData1.getPhoneNumber().getNumber());
        Assert.assertEquals(200, body.getHttpStatus());
    }
}
