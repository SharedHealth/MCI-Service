package org.sharedhealth.mci.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfig;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.sharedhealth.mci.web.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class SearchRestApiTest extends BaseControllerTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Mock
    private LocationService locationService;

    private static final String PER_PAGE_MAXIMUM_LIMIT_NOTE = "There are more record for this search criteria. Please narrow down your search";
    private static final int PER_PAGE_MAXIMUM_LIMIT = 25;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        patientData = new PatientData();
        patientData.setGivenName("Raju");
        patientData.setSurName("Mazumder");
        patientData.setGender("M");
        patientData.setDateOfBirth("2014-12-01");
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
        address.setUpazillaId("09");
        address.setCityCorporationId("20");
        address.setVillage("10");
        address.setWardId("01");
        address.setCountryCode("050");

        patientData.setAddress(address);
    }

    @Test
    public void shouldReturnBadRequestIfOnlySurNameGiven() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(get(API_END_POINT + "?sur_name=Mazumder").accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1006,\"message\":\"Invalid search parameter\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestIfOnlyGivenNameGiven() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(get(API_END_POINT + "?given_name=Mazumder").accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1006,\"message\":\"Invalid search parameter\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnOkResponseIfPatientNotExistWithGivenNameAndAddress() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patientData);
        String present_address = patientData.getAddress().getDivisionId() +
                patientData.getAddress().getDistrictId() + patientData.getAddress().getUpazillaId();
        String givenName = "Rajus";
        MvcResult result = mockMvc.perform(get(API_END_POINT + "?given_name=" + givenName + "&present_address=" + present_address).accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertEquals("[]", body.getResults().toString());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnPatientIfGivenNameAndAddressMatchWithAnyPatient() throws Exception {
        String json = asString("jsons/patient/full_payload.json");

        PatientData original = getPatientObjectFromString(json);

        MvcResult result = createPatient(json);
        String present_address = original.getAddress().getDivisionId() +
                original.getAddress().getDistrictId() + original.getAddress().getUpazillaId();
        String givenName = "Zaman";

        MvcResult searchResult = mockMvc.perform(get(API_END_POINT + "?given_name=" + givenName + "&present_address=" + present_address).accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(searchResult);
        PatientData patient = getPatientObjectFromString(mapper.writeValueAsString(body.getResults().get(0)));

        assertPatientEquals(original, patient);
    }

    @Test
    public void shouldReturnPatientIfGivenNameAndSurNameAndAddressMatchWithCaseSensetiveSupport() throws Exception {

        String json = asString("jsons/patient/full_payload.json");

        PatientData original = getPatientObjectFromString(json);

        MvcResult result = createPatient(json);
        String present_address = original.getAddress().getDivisionId() +
                original.getAddress().getDistrictId() + original.getAddress().getUpazillaId();
        String givenName = "zaman";
        String surName = "aymaan";
        MvcResult searchResult = mockMvc.perform(get(API_END_POINT + "?given_name=" + givenName +
                "&sur_name=" + surName + "&present_address=" + present_address).accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(searchResult);
        PatientData patient = getPatientObjectFromString(mapper.writeValueAsString(body.getResults().get(0)));

        assertPatientEquals(original, patient);
    }

    @Test
    public void shouldReturnPatientWithAdditionalNoteWhenSearchFindMorePatient() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patientData);

        for (int x = 0; x <= PER_PAGE_MAXIMUM_LIMIT; x++) {
            createPatient(json);
        }
        String present_address = patientData.getAddress().getDivisionId() +
                patientData.getAddress().getDistrictId() + patientData.getAddress().getUpazillaId();
        String givenName = "raju";
        String surName = "mazumder";
        MvcResult result = mockMvc.perform(get(API_END_POINT + "?given_name=" + givenName +
                "&sur_name=" + surName + "&present_address=" + present_address).accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertEquals("{note=" + PER_PAGE_MAXIMUM_LIMIT_NOTE + "}", body.getAdditionalInfo().toString());

        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnBadRequestIfOnlyExtensionOrCountryCodeOrAreaCodeGiven() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(get(API_END_POINT +
                "?country_code=880&area_code=02&extension=122").accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1006,\"message\":\"Invalid search parameter\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestIfPresentAddressNotGivenWithPhoneNumber() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(get(API_END_POINT +
                "?phone_number=1716528608").accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1006,\"message\":\"Invalid search parameter\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestIfOnlyCountryCodeGiven() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(get(API_END_POINT +
                "?country_code=880").accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1006,\"message\":\"Invalid search parameter\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnOkResponseIfPatientNotExistWithPhoneNumber() throws Exception {
        patientData.setHealthId("health-100");
        String json = new ObjectMapper().writeValueAsString(patientData);
        String present_address = patientData.getAddress().getDivisionId() +
                patientData.getAddress().getDistrictId() + patientData.getAddress().getUpazillaId();
        MvcResult result = mockMvc.perform(get(API_END_POINT +
                "?phone_no=123456&country_code=880&present_address=" + present_address).accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertEquals("[]", body.getResults().toString());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnAllTheCreatedPatientIfPhoneNumberMatchBySearch() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patientData);
        String present_address = patientData.getAddress().getDivisionId() +
                patientData.getAddress().getDistrictId() + patientData.getAddress().getUpazillaId();
        createPatient(json);
        createPatient(json);
        createPatient(json);

        MvcResult result = mockMvc.perform(get(API_END_POINT +
                "?phone_no=1716528608&country_code=880&present_address=" + present_address).accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final MCIMultiResponse body = getMciMultiResponse(result);
        PatientData patientData1 = (PatientData) body.getResults().get(0);
        Assert.assertEquals("1716528608", patientData1.getPhoneNumber().getNumber());
        Assert.assertEquals(200, body.getHttpStatus());
    }
}
