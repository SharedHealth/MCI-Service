package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.text.ParseException;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.utils.HttpUtil.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.setupApprovalsConfig;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.setupLocation;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientControllerAuthorizationIT extends BaseControllerTest {
    @Before
    public void setup() throws ParseException {
        MockitoAnnotations.initMocks(this);
        setUpMockMvcBuilder();
        createPatientData();
        setupApprovalsConfig(cassandraOps);
        setupLocation(cassandraOps);
    }

    @Test
    public void facilityShouldCreatePatient() throws Exception {
        final String validClientId = "18548";
        final String validEmail = "facility@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForFacility.json"))));

        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }

    @Test
    public void providerShouldCreatePatient() throws Exception {
        final String validClientId = "18556";
        final String validEmail = "provider@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForProvider.json"))));

        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }

    @Test
    public void datasenseShouldNotCreatePatient() throws Exception {
        final String validClientId = "18552";
        final String validEmail = "datasense@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForDatasense.json"))));

        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void patientShouldNotCreatePatient() throws Exception {
        final String validClientId = "18558";
        final String validEmail = "patient@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForPatient.json"))));

        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void patientShouldNotCreateMCIAdmin() throws Exception {
        final String validClientId = "18557";
        final String validEmail = "mciadmin@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIAdmin.json"))));

        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void patientShouldNotCreateMCIApprover() throws Exception {
        final String validClientId = "18555";
        final String validEmail = "mciapprover@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIApprover.json"))));

        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private void createPatientData() {
        patientData = new PatientData();
        patientData.setGivenName("Scott");
        patientData.setSurName("Tiger");
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

        Address presentAddress = new Address();
        presentAddress.setAddressLine("house-12");
        presentAddress.setDivisionId("10");
        presentAddress.setDistrictId("04");
        presentAddress.setUpazilaId("09");
        presentAddress.setCityCorporationId("20");
        presentAddress.setUnionOrUrbanWardId("01");
        presentAddress.setRuralWardId(null);
        presentAddress.setVillage("10");
        presentAddress.setCountryCode("050");

        patientData.setAddress(presentAddress);

        Address permanentAddress = new Address();
        permanentAddress.setAddressLine("house-12");
        permanentAddress.setDivisionId("10");
        permanentAddress.setDistrictId("04");
        permanentAddress.setUpazilaId("09");
        permanentAddress.setCityCorporationId("20");
        permanentAddress.setUnionOrUrbanWardId("06");
        permanentAddress.setRuralWardId(null);
        permanentAddress.setVillage("10");
        permanentAddress.setCountryCode("050");

        patientData.setPermanentAddress(permanentAddress);
    }
}