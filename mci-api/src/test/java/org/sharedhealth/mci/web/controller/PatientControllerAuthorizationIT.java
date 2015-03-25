package org.sharedhealth.mci.web.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.ParseException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.FROM_KEY;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.setupApprovalsConfig;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.setupLocation;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientControllerAuthorizationIT extends BaseControllerTest {

    private final String patientClientId = "18558";
    private final String patientEmail = "patient@gmail.com";
    private final String patientAccessToken = "40214a6c-e27c-4223-981c-1f837be90f01";

    private final String facilityClientId = "18548";
    private final String facilityEmail = "facility@gmail.com";
    private final String facilityAccessToken = "40214a6c-e27c-4223-981c-1f837be90f02";

    private final String providerClientId = "18556";
    private final String providerEmail = "provider@gmail.com";
    private final String providerAccessToken = "40214a6c-e27c-4223-981c-1f837be90f03";

    private final String datasenseClientId = "18552";
    private final String datasenseEmail = "datasense@gmail.com";
    private final String datasenseAccessToken = "40214a6c-e27c-4223-981c-1f837be90f04";

    private final String mciAdminClientId = "18557";
    private final String mciAdminEmail = "mciadmin@gmail.com";
    private final String mciAdminAccessToken = "40214a6c-e27c-4223-981c-1f837be90f05";

    private final String mciApproverClientId = "18555";
    private final String mciApproverEmail = "mciapprover@gmail.com";
    private final String mciApproverAccessToken = "40214a6c-e27c-4223-981c-1f837be90f06";

    @Before
    public void setup() throws ParseException {
        MockitoAnnotations.initMocks(this);
        setUpMockMvcBuilder();
        createPatientData();
        setupApprovalsConfig(cassandraOps);
        setupLocation(cassandraOps);

        givenThat(WireMock.get(urlEqualTo("/token/" + patientAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForPatient.json"))));

        givenThat(WireMock.get(urlEqualTo("/token/" + facilityAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForFacility.json"))));

        givenThat(WireMock.get(urlEqualTo("/token/" + providerAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForProvider.json"))));

        givenThat(WireMock.get(urlEqualTo("/token/" + datasenseAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForDatasense.json"))));

        givenThat(WireMock.get(urlEqualTo("/token/" + mciAdminAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIAdmin.json"))));

        givenThat(WireMock.get(urlEqualTo("/token/" + mciApproverAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIApprover.json"))));

    }

    @Test
    public void facilityShouldCreatePatient() throws Exception {
        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }

    @Test
    public void facilityShouldGetPatient() throws Exception {
        MCIResponse postResponse = createPatient(patientData);

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "?nid=" + patientData.getNationalId())
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void facilityShouldUpdatePatient() throws Exception {
        String healthId = createPatient(patientData).getId();

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId)
                .accept(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patientData))
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void providerShouldCreatePatient() throws Exception {
        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, providerAccessToken)
                .header(FROM_KEY, providerEmail)
                .header(CLIENT_ID_KEY, providerClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }

    @Test
    public void providerShouldGetPatient() throws Exception {
        MCIResponse postResponse = createPatient(patientData);

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, providerAccessToken)
                .header(FROM_KEY, providerEmail)
                .header(CLIENT_ID_KEY, providerClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "?bin_brn=" + patientData.getBirthRegistrationNumber())
                .header(AUTH_TOKEN_KEY, providerAccessToken)
                .header(FROM_KEY, providerEmail)
                .header(CLIENT_ID_KEY, providerClientId))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void providerShouldUpdatePatient() throws Exception {
        String healthId = createPatient(patientData).getId();

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, providerAccessToken)
                .header(FROM_KEY, providerEmail)
                .header(CLIENT_ID_KEY, providerClientId)
                .accept(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patientData))
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void datasenseShouldNotCreatePatient() throws Exception {
        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, datasenseAccessToken)
                .header(FROM_KEY, datasenseEmail)
                .header(CLIENT_ID_KEY, datasenseClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void datasenseShouldGetPatientWithHidOnly() throws Exception {
        MCIResponse postResponse = createPatient(patientData);

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, datasenseAccessToken)
                .header(FROM_KEY, datasenseEmail)
                .header(CLIENT_ID_KEY, datasenseClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "?bin_brn=" + patientData.getBirthRegistrationNumber())
                .header(AUTH_TOKEN_KEY, datasenseAccessToken)
                .header(FROM_KEY, datasenseEmail)
                .header(CLIENT_ID_KEY, datasenseClientId))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void datasenseShouldNotUpdatePatient() throws Exception {
        String healthId = createPatient(patientData).getId();

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, datasenseAccessToken)
                .header(FROM_KEY, datasenseEmail)
                .header(CLIENT_ID_KEY, datasenseClientId)
                .accept(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patientData))
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }


    @Test
    public void patientUserShouldNotCreatePatient() throws Exception {
        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, patientAccessToken)
                .header(FROM_KEY, patientEmail)
                .header(CLIENT_ID_KEY, patientClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void patientUserShouldGetPatientWithPatientsHidOnly() throws Exception {
        MCIResponse postResponse = createPatient(patientData);

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, patientAccessToken)
                .header(FROM_KEY, patientEmail)
                .header(CLIENT_ID_KEY, patientClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "?bin_brn=" + patientData.getBirthRegistrationNumber())
                .header(AUTH_TOKEN_KEY, patientAccessToken)
                .header(FROM_KEY, patientEmail)
                .header(CLIENT_ID_KEY, patientClientId))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void patientShouldNotUpdatePatient() throws Exception {
        String healthId = createPatient(patientData).getId();

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, patientAccessToken)
                .header(FROM_KEY, patientEmail)
                .header(CLIENT_ID_KEY, patientClientId)
                .accept(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patientData))
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }


    @Test
    public void patientUserShouldNotGetPatientIfHidDoesNotMatch() throws Exception {
        MCIResponse postResponse = createPatient(patientData);

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, patientAccessToken)
                .header(FROM_KEY, patientEmail)
                .header(CLIENT_ID_KEY, patientClientId))
                .andExpect(request().asyncResult(isForbidden()))
                .andReturn();
    }

    @Test
    public void mciAdminShouldNotCreatePatient() throws Exception {
        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void mciAdminShouldGetPatient() throws Exception {
        MCIResponse postResponse = createPatient(patientData);

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT
                    + "?given_name=" + patientData.getGivenName()
                    + "&present_address=" + patientData.getAddress().getGeoCode()
                    + "&sur_name=" + patientData.getSurName())
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void mciAdminShouldUpdatePatient() throws Exception {
        String healthId = createPatient(patientData).getId();

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId)
                .accept(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patientData))
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void mciApproverShouldNotCreatePatient() throws Exception {
        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void mciApproverShouldGetPatient() throws Exception {
        MCIResponse postResponse = createPatient(patientData);

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get(API_END_POINT_FOR_PATIENT + "?bin_brn=" + patientData.getBirthRegistrationNumber())
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void mciApproverShouldNotUpdatePatient() throws Exception {
        String healthId = createPatient(patientData).getId();

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId)
                .accept(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patientData))
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
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
        patientData.setNationalId("1343313433345");
        patientData.setBirthRegistrationNumber("13433134331343323");

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