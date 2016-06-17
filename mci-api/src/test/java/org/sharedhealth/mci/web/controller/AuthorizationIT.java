package org.sharedhealth.mci.web.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.repository.LocationCriteria;
import org.sharedhealth.mci.domain.service.LocationService;
import org.sharedhealth.mci.web.service.PatientAuditService;
import org.sharedhealth.mci.web.service.PatientService;
import org.sharedhealth.mci.web.service.ProviderService;
import org.sharedhealth.mci.web.service.RequesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;

import java.text.ParseException;
import java.util.TreeSet;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.format;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.constant.JsonConstants.LAST_MARKER;
import static org.sharedhealth.mci.domain.repository.TestUtil.setupApprovalsConfig;
import static org.sharedhealth.mci.domain.repository.TestUtil.setupLocation;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.utils.HttpUtil.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("AuthorizationIT")
public class AuthorizationIT extends BaseControllerTest {
    @Autowired
    private PatientService patientService;
    @Autowired
    private PatientAuditService auditService;
    @Autowired
    private RequesterService requesterService;
    @Autowired
    private LocationService locationService;

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

    @Configuration
    @Profile("AuthorizationIT")
    static class ContextConfiguration {

        // this bean will be injected into the test class
        @Bean
        public PatientService patientService() {
            return mock(PatientService.class);
        }

        @Bean
        public ProviderService providerService() {
            return mock(ProviderService.class);
        }

        @Bean
        public PatientAuditService patientAuditService() {
            return mock(PatientAuditService.class);
        }

        @Bean
        public RequesterService requesterService() {
            return mock(RequesterService.class);
        }

        @Bean
        public LocationService locationService() {
            return mock(LocationService.class);
        }
    }

    @Before
    public void setUp() throws ParseException {
        initMocks(this);
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

        when(locationService.findByGeoCode("302618")).thenReturn(new LocationData());
        when(locationService.findByGeoCode("1004092006")).thenReturn(new LocationData());
        when(patientService.createPatientForMCI(any(PatientData.class))).thenReturn(new MCIResponse("HID", HttpStatus.CREATED));

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
        String hid = "HID";
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setNid(patientData.getNationalId());

        when(patientService.findByHealthId(hid)).thenReturn(patientData);
        when(patientService.findAllByQuery(searchQuery)).thenReturn(singletonList(patientData));

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + hid)
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
        String healthId = "HID";
        String content = mapper.writeValueAsString(patientData);

        when(locationService.findByGeoCode("302618")).thenReturn(new LocationData());
        when(locationService.findByGeoCode("1004092006")).thenReturn(new LocationData());
        when(patientService.update(patientData, healthId)).thenReturn(new MCIResponse(healthId, HttpStatus.ACCEPTED));

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId)
                .accept(APPLICATION_JSON)
                .content(content)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void providerShouldCreatePatient() throws Exception {
        String json = mapper.writeValueAsString(patientData);

        when(locationService.findByGeoCode("302618")).thenReturn(new LocationData());
        when(locationService.findByGeoCode("1004092006")).thenReturn(new LocationData());
        when(patientService.createPatientForMCI(patientData)).thenReturn(new MCIResponse("HID", HttpStatus.CREATED));

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
        String hid = "HID";
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setBin_brn(patientData.getBirthRegistrationNumber());

        when(patientService.findByHealthId(hid)).thenReturn(patientData);
        when(patientService.findAllByQuery(searchQuery)).thenReturn(singletonList(patientData));

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + hid)
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
        String healthId = "HID";
        String content = mapper.writeValueAsString(patientData);

        when(locationService.findByGeoCode("302618")).thenReturn(new LocationData());
        when(locationService.findByGeoCode("1004092006")).thenReturn(new LocationData());
        when(patientService.update(patientData, healthId)).thenReturn(new MCIResponse("HID", HttpStatus.ACCEPTED));

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
        String hid = "HID";
        when(patientService.findByHealthId(hid)).thenReturn(patientData);

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + hid)
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
        String hid = "HID";

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + hid)
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
        String hid = "HID";
        when(patientService.findByHealthId(hid)).thenReturn(patientData);

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + hid)
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
        String hid = "HID";

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + hid)
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
        String hid = "HID";

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + hid)
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
        String hid = "HID";
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setGiven_name(patientData.getGivenName());
        searchQuery.setSur_name(patientData.getSurName());
        searchQuery.setPresent_address(patientData.getAddress().getGeoCode());

        when(patientService.findByHealthId(hid)).thenReturn(patientData);
        when(patientService.findAllByQuery(searchQuery)).thenReturn(singletonList(patientData));

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + hid)
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
        String healthId = "HID";
        when(patientService.update(patientData, healthId)).thenReturn(new MCIResponse(healthId, HttpStatus.ACCEPTED));

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
        String hid = "HID";
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setBin_brn(patientData.getBirthRegistrationNumber());

        when(patientService.findByHealthId(hid)).thenReturn(patientData);
        when(patientService.findAllByQuery(searchQuery)).thenReturn(singletonList(patientData));

        mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + hid)
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
        String hid = "HID";
        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + hid)
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
        String hid = "HID";

        when(auditService.findByHealthId(hid)).thenReturn(EMPTY_LIST);
        when(patientService.findByHealthId(hid)).thenReturn(patientData);
        doNothing().when(requesterService).populateRequesterDetails(any(Requester.class));

        mockMvc.perform(get("/audit/patients/" + hid)
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }

    @Test
    public void mciApproverShouldFindAuditLogByHealthId() throws Exception {
        String hid = "HID";

        when(auditService.findByHealthId(hid)).thenReturn(EMPTY_LIST);
        when(patientService.findByHealthId(hid)).thenReturn(patientData);
        doNothing().when(requesterService).populateRequesterDetails(any(Requester.class));

        mockMvc.perform(get("/audit/patients/" + hid)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void facilityShouldAccessUpdateFeed() throws Exception {
        UUID lastMarker = timeBased();
        String requestUrl = format("%s/%s/patients", "https://mci.dghs.com", "feed") + "?"
                + LAST_MARKER + "=" + lastMarker.toString();

        when(patientService.findPatientsUpdatedSince(null, lastMarker)).thenReturn(EMPTY_LIST);

        mockMvc.perform(get(requestUrl)
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void datasenseShouldAccessUpdateFeed() throws Exception {
        UUID lastMarker = timeBased();
        String requestUrl = format("%s/%s/patients", "https://mci.dghs.com", "feed") + "?"
                + LAST_MARKER + "=" + lastMarker.toString();

        when(patientService.findPatientsUpdatedSince(null, lastMarker)).thenReturn(EMPTY_LIST);

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
        when(patientService.findAllByCatchment(new Catchment("30", "26"), null, null)).thenReturn(EMPTY_LIST);
        when(patientService.findAllByCatchment(new Catchment("10", "30"), null, null)).thenReturn(EMPTY_LIST);

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
        when(patientService.findAllByCatchment(new Catchment("30", "26"), null, null)).thenReturn(EMPTY_LIST);
        when(patientService.findAllByCatchment(new Catchment("10", "30"), null, null)).thenReturn(EMPTY_LIST);

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
        when(patientService.findAllByCatchment(new Catchment("30", "26"), null, null)).thenReturn(EMPTY_LIST);
        when(patientService.findAllByCatchment(new Catchment("10", "30"), null, null)).thenReturn(EMPTY_LIST);

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
        when(patientService.findPendingApprovalList(new Catchment("30", "26"), null, null, 0)).thenReturn(EMPTY_LIST);
        when(patientService.findPendingApprovalList(new Catchment("10", "30"), null, null, 0)).thenReturn(EMPTY_LIST);

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
        String healthId = "HID";

        TreeSet<PendingApproval> emptyTreeSet = new TreeSet<>();
        when(patientService.findPendingApprovalDetails(healthId, new Catchment("30", "26"))).thenReturn(emptyTreeSet);
        when(patientService.findPendingApprovalDetails(healthId, new Catchment("10", "30"))).thenReturn(emptyTreeSet);

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
        mockMvc.perform(get("/catchments/3026/approvals/HID")
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isForbidden());
    }

    @Test
    public void mciApproverShouldApprovePatientUpdatesForItsCatchmentOnly() throws Exception {
        String healthId = "HID";

        when(locationService.findByGeoCode("557364")).thenReturn(new LocationData());
        when(patientService.processPendingApprovals(any(PatientData.class), any(Catchment.class),
                anyBoolean())).thenReturn(healthId);

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
        mockMvc.perform(put("/catchments/3026/approvals/HID")
                .content(mapper.writeValueAsString(patientData))
                .contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isForbidden());
    }

    @Test
    public void mciApproverShouldRejectPatientUpdatesForItsCatchmentOnly() throws Exception {
        String healthId = "HID";
        when(locationService.findByGeoCode("557364")).thenReturn(new LocationData());
        when(patientService.processPendingApprovals(any(PatientData.class), any(Catchment.class),
                anyBoolean())).thenReturn(healthId);

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
        mockMvc.perform(delete("/catchments/3026/approvals/HID")
                .content(mapper.writeValueAsString(patientData))
                .contentType(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciAdminAccessToken)
                .header(FROM_KEY, mciAdminEmail)
                .header(CLIENT_ID_KEY, mciAdminClientId))
                .andExpect(status().isForbidden());
    }

    @Test
    public void locationsByParentShouldBeAccessedOnlyByMciApproverAndMciAdmin() throws Exception {
        LocationCriteria locationCriteria = new LocationCriteria();
        locationCriteria.setParent("11");

        when(locationService.findLocationsByParent(locationCriteria)).thenReturn(EMPTY_LIST);

        mockMvc.perform(get(API_END_POINT_FOR_LOCATION + "?parent=11")
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        mockMvc.perform(get(API_END_POINT_FOR_LOCATION + "?parent=11")
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
    public void mciApproverShouldUpdatePatientUsingMergerequestApi() throws Exception {
        String healthId = "HID1";
        String targetHealthId = "HID2";
        patientData.setHealthId(null);


        PatientData patientDataWithActiveInfo = new PatientData();
        patientDataWithActiveInfo.setActive(false);
        patientDataWithActiveInfo.setMergedWith(targetHealthId);

        when(patientService.update(patientDataWithActiveInfo, healthId)).thenReturn(new MCIResponse(healthId, HttpStatus.ACCEPTED));

        String json = mapper.writeValueAsString(patientDataWithActiveInfo);
        mockMvc.perform(put(API_END_POINT_FOR_MERGE_REQUEST + "/" + healthId)
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
        patientData.setDateOfBirth(parseDate("2014-12-01"));
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