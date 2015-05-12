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
import org.springframework.test.web.servlet.MvcResult;

import java.text.ParseException;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.format;
import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.utils.HttpUtil.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.setupApprovalsConfig;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.setupLocation;
import static org.sharedhealth.mci.web.utils.JsonConstants.LAST_MARKER;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class AuthorizationIT extends BaseControllerTest {

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

    private final String mciAdminClientId = "18564";
    private final String mciAdminEmail = "MciAdmin@test.com";
    private final String mciAdminAccessToken = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";

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

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?nid=" + patientData.getNationalId())
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

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, providerAccessToken)
                .header(FROM_KEY, providerEmail)
                .header(CLIENT_ID_KEY, providerClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?bin_brn=" + patientData.getBirthRegistrationNumber())
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

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, datasenseAccessToken)
                .header(FROM_KEY, datasenseEmail)
                .header(CLIENT_ID_KEY, datasenseClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?bin_brn=" + patientData.getBirthRegistrationNumber())
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

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, patientAccessToken)
                .header(FROM_KEY, patientEmail)
                .header(CLIENT_ID_KEY, patientClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?bin_brn=" + patientData.getBirthRegistrationNumber())
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

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
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

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT
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

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + postResponse.getId())
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "?bin_brn=" + patientData.getBirthRegistrationNumber())
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

    @Test
    public void mciAdminShouldFindAuditLogByHealthId() throws Exception {
        MCIResponse mciResponse = createPatient(patientData);

        mockMvc.perform(get("/audit/patients/" + mciResponse.getId())
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }

    @Test
    public void mciApproverShouldFindAuditLogByHealthId() throws Exception {
        MCIResponse mciResponse = createPatient(patientData);

        mockMvc.perform(get("/audit/patients/" + mciResponse.getId())
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void facilityShouldAccessUpdateFeed() throws Exception {
        UUID uuid1 = timeBased();

        String requestUrl = format("%s/%s/patients", "https://mci.dghs.com", "feed") + "?"
                + LAST_MARKER + "=" + uuid1.toString();

        mockMvc.perform(get(requestUrl)
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void datasenseShouldAccessUpdateFeed() throws Exception {
        UUID uuid1 = timeBased();

        String requestUrl = format("%s/%s/patients", "https://mci.dghs.com", "feed") + "?"
                + LAST_MARKER + "=" + uuid1.toString();

        mockMvc.perform(get(requestUrl)
                .header(AUTH_TOKEN_KEY, datasenseAccessToken)
                .header(FROM_KEY, datasenseEmail)
                .header(CLIENT_ID_KEY, datasenseClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void providerShouldNotAccessUpdateFeed() throws Exception {
        UUID uuid1 = timeBased();

        String requestUrl = format("%s/%s/patients", "https://mci.dghs.com", "feed") + "?"
                + LAST_MARKER + "=" + uuid1.toString();

        mockMvc.perform(get(requestUrl)
                .header(AUTH_TOKEN_KEY, providerAccessToken)
                .header(FROM_KEY, providerEmail)
                .header(CLIENT_ID_KEY, providerClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void facilityShouldFindPatientsForItsCatchmentOnly() throws Exception {
        mockMvc.perform(get("/catchments/3026/patients")
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        mockMvc.perform(get("/catchments/1030/patients")
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(isForbidden()));
    }

    @Test
    public void providerShouldFindPatientsForItsCatchmentOnly() throws Exception {
        mockMvc.perform(get("/catchments/3026/patients")
                .header(AUTH_TOKEN_KEY, providerAccessToken)
                .header(FROM_KEY, providerEmail)
                .header(CLIENT_ID_KEY, providerClientId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        mockMvc.perform(get("/catchments/102030/patients")
                .header(AUTH_TOKEN_KEY, providerAccessToken)
                .header(FROM_KEY, providerEmail)
                .header(CLIENT_ID_KEY, providerClientId))
                .andExpect(request().asyncResult(isForbidden()));
    }

    @Test
    public void datasenseShouldFindPatientsByCatchment() throws Exception {
        mockMvc.perform(get("/catchments/3026/patients")
                .header(AUTH_TOKEN_KEY, datasenseAccessToken)
                .header(FROM_KEY, datasenseEmail)
                .header(CLIENT_ID_KEY, datasenseClientId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        mockMvc.perform(get("/catchments/102030/patients")
                .header(AUTH_TOKEN_KEY, datasenseAccessToken)
                .header(FROM_KEY, datasenseEmail)
                .header(CLIENT_ID_KEY, datasenseClientId))
                .andExpect(request().asyncResult(isForbidden()));
    }

    @Test
    public void mciAdminShouldNotFindPatientsByCatchment() throws Exception {
        mockMvc.perform(get("/catchments/3026/patients")
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isForbidden());
    }

    @Test
    public void mciApproverShouldGetAllApprovalsForItsCatchmentOnly() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/catchments/3026/approvals")
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        mockMvc.perform(get("/catchments/1030/approvals")
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(request().asyncResult(isForbidden()));
    }

    @Test
    public void mciAdminShouldNotGetApprovalsList() throws Exception {
        mockMvc.perform(get("/catchments/3026/approvals")
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isForbidden());
    }

    @Test
    public void mciApproverShouldGetAllApprovalsWithHidForItsCatchmentOnly() throws Exception {
        MCIResponse mciResponse = createPatient(patientData);
        String healthId = mciResponse.getId();

        MvcResult mvcResult = mockMvc.perform(get("/catchments/3026/approvals/" + healthId)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        mockMvc.perform(get("/catchments/1030/approvals/" + healthId)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(request().asyncResult(isForbidden()));
    }

    @Test
    public void mciAdminShouldNotGetApprovalsWithHid() throws Exception {
        MCIResponse mciResponse = createPatient(patientData);

        mockMvc.perform(get("/catchments/3026/approvals/" + mciResponse.getId())
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isForbidden());
    }

    @Test
    public void mciApproverShouldApprovePatientUpdatesForItsCatchmentOnly() throws Exception {
        MCIResponse mciResponse = createPatient(asString("jsons/patient/full_payload.json"));
        String healthId = mciResponse.getId();
        updatePatient(asString("jsons/patient/payload_with_address.json"), healthId);

        MvcResult mvcResult = mockMvc.perform(put("/catchments/3026/approvals/" + healthId)
                .content(asString("jsons/patient/pending_approval_address_accept.json"))
                .contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted());

        mockMvc.perform(put("/catchments/1030/approvals/" + healthId)
                .content(asString("jsons/patient/pending_approval_address_accept.json"))
                .contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(request().asyncResult(isForbidden()));
    }

    @Test
    public void mciAdminShouldNotApprovePatientUpdates() throws Exception {
        MCIResponse mciResponse = createPatient(patientData);

        mockMvc.perform(put("/catchments/3026/approvals/" + mciResponse.getId())
                .content(mapper.writeValueAsString(patientData))
                .contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isForbidden());
    }

    @Test
    public void mciApproverShouldRejectPatientUpdatesForItsCatchmentOnly() throws Exception {
        MCIResponse mciResponse = createPatient(asString("jsons/patient/full_payload.json"));
        String healthId = mciResponse.getId();
        updatePatient(asString("jsons/patient/payload_with_address.json"), healthId);

        MvcResult mvcResult = mockMvc.perform(delete("/catchments/3026/approvals/" + healthId)
                .content(asString("jsons/patient/pending_approval_address_accept.json"))
                .contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted());

        mockMvc.perform(delete("/catchments/1030/approvals/" + healthId)
                .content(asString("jsons/patient/pending_approval_address_accept.json"))
                .contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(request().asyncResult(isForbidden()));
    }

    @Test
    public void mciAdminShouldNotRejectPatientUpdates() throws Exception {
        MCIResponse mciResponse = createPatient(patientData);

        mockMvc.perform(delete("/catchments/3026/approvals/" + mciResponse.getId())
                .content(mapper.writeValueAsString(patientData))
                .contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isForbidden());
    }

    @Test
    public void locationsByParentShouldBeAccessedOnlyByMciApproverAndMciAdmin() throws Exception {
        createPatient(patientData);

        mockMvc.perform(get(API_END_POINT_FOR_LOCATION + "?parent=11")
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        mockMvc.perform(get(API_END_POINT_FOR_LOCATION)
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        mockMvc.perform(get(API_END_POINT_FOR_LOCATION + "?parent=11")
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(API_END_POINT_FOR_LOCATION + "?parent=11")
                .header(AUTH_TOKEN_KEY, providerAccessToken)
                .header(FROM_KEY, providerEmail)
                .header(CLIENT_ID_KEY, providerClientId))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(API_END_POINT_FOR_LOCATION + "?parent=11")
                .header(AUTH_TOKEN_KEY, patientAccessToken)
                .header(FROM_KEY, patientEmail)
                .header(CLIENT_ID_KEY, patientClientId))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(API_END_POINT_FOR_LOCATION + "?parent=11")
                .header(AUTH_TOKEN_KEY, datasenseAccessToken)
                .header(FROM_KEY, datasenseEmail)
                .header(CLIENT_ID_KEY, datasenseClientId))
                .andExpect(status().isForbidden());
    }

    @Test
    public void mciApproverShouldUpdatesPatientUsingActiveUpdateApi() throws Exception {

        String healthId = createPatient(patientData).getId();
        String targetHealthId = createPatient(patientData).getId();

        PatientData patientDataWithActiveInfo = new PatientData();
        patientDataWithActiveInfo.setActive(false);
        patientDataWithActiveInfo.setMergedWith(targetHealthId);

        String json = mapper.writeValueAsString(patientDataWithActiveInfo);

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/active/" + healthId)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void createPatientData() {
        patientData = new PatientData();
        patientData.setGivenName("Scott");
        patientData.setSurName("Tiger");
        patientData.setGender("M");
        patientData.setDateOfBirth(toIsoFormat("2014-12-01"));
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
        presentAddress.setDivisionId("30");
        presentAddress.setDistrictId("26");
        presentAddress.setUpazilaId("18");
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
        patientData.setRequester("Bahmni", null);
    }
}