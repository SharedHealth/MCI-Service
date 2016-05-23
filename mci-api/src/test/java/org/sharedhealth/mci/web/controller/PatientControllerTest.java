package org.sharedhealth.mci.web.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.utils.HttpUtil;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.infrastructure.security.TokenAuthentication;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.mapper.ProviderResponse;
import org.sharedhealth.mci.web.service.PatientService;
import org.sharedhealth.mci.web.service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import rx.Observable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;
import static org.sharedhealth.mci.domain.util.JsonMapper.writeValueAsString;
import static org.sharedhealth.mci.web.infrastructure.security.UserInfo.HRM_MCI_USER_GROUP;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@RunWith(MockitoJUnitRunner.class)
public class PatientControllerTest {

    private static final String USER_INFO_FACILITY = "100067";
    private static final String API_END_POINT = "/patients";

    @Mock
    private PatientService patientService;
    @Mock
    private LocalValidatorFactoryBean localValidatorFactoryBean;
    @Mock
    private ProviderService providerService;
    private MockMvc mockMvc;

    @Before
    public void setup() throws ParseException {
        initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PatientController(patientService, providerService))
                .setValidator(localValidatorFactoryBean)
                .build();

        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(getUserInfo(), true));
    }

    private UserInfo getUserInfo() {
        UserProfile userProfile = new UserProfile("facility", USER_INFO_FACILITY, null);
        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100", asList(HRM_MCI_USER_GROUP), asList(userProfile));
    }

    @Test
    public void shouldCreatePatientAndReturnHealthId() throws Exception {
        PatientData patient = buildPatient();
        String healthId = "healthId-100";
        MCIResponse mciResponse = new MCIResponse(healthId, CREATED);
        when(patientService.createPatientForMCI(patient)).thenReturn(Observable.just(mciResponse));

        String json = new ObjectMapper().writeValueAsString(patient);
        mockMvc.perform(post(API_END_POINT).content(json).contentType(APPLICATION_JSON))
                .andExpect(request().asyncResult(new ResponseEntity<>(mciResponse, CREATED)));
        verify(patientService).createPatientForMCI(patient);
    }

    @Test
    public void shouldCreatePatientForGivenOrganization() throws Exception {
        String healthId = "healthId-100";
        String clientIdKey = "12345";
        PatientData patient = buildPatient();
        patient.setHealthId(healthId);
        MCIResponse mciResponse = new MCIResponse(healthId, CREATED);
        when(patientService.createPatientForOrg(patient, USER_INFO_FACILITY)).thenReturn(Observable.just(mciResponse));

        String json = new ObjectMapper().writeValueAsString(patient);

        mockMvc.perform(post(API_END_POINT)
                .header(HttpUtil.CLIENT_ID_KEY, clientIdKey)
                .content(json).contentType(APPLICATION_JSON))
                .andExpect(request()
                        .asyncResult(new ResponseEntity<>(mciResponse, CREATED)));

        verify(patientService).createPatientForOrg(patient, USER_INFO_FACILITY);
    }

    @Test
    public void shouldIdentifyFacilityForProvider() throws Exception {
        String providerId = "11111";
        String facilityId = "10012";
        String healthId = "healthId-100";
        String clientIdKey = "12345";
        PatientData patient = buildPatient();
        patient.setHealthId(healthId);

        UserProfile userProfile = new UserProfile("provider", providerId, null);
        List<String> groups = new ArrayList<>();
        groups.add(HRM_MCI_USER_GROUP);
        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100", groups, asList(userProfile));
        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(userInfo, true));

        MCIResponse mciResponse = new MCIResponse(healthId, CREATED);

        when(patientService.createPatientForOrg(patient, facilityId)).thenReturn(Observable.just(mciResponse));
        ProviderResponse response = getProviderResponse(providerId, "ABC", "http://fr.com/10012.json");
        when(providerService.find(providerId)).thenReturn(response);

        String json = new ObjectMapper().writeValueAsString(patient);

        mockMvc.perform(post(API_END_POINT)
                .header(HttpUtil.CLIENT_ID_KEY, clientIdKey)
                .content(json).contentType(APPLICATION_JSON))
                .andExpect(request()
                        .asyncResult(new ResponseEntity<>(mciResponse, CREATED)));

        verify(providerService, times(1)).find(providerId);
        verify(patientService).createPatientForOrg(patient, facilityId);
    }

    @Test
    public void shouldFindPatientByHealthId() throws Exception {
        String healthId = "healthId-100";
        PatientData patient = buildPatient();
        when(patientService.findByHealthId(healthId)).thenReturn(patient);
        mockMvc.perform(get(API_END_POINT + "/" + healthId))
                .andExpect(request().asyncResult(new ResponseEntity<>(patient, OK)));
        verify(patientService).findByHealthId(healthId);
    }

    @Test
    public void shouldFindPatientsByNationalId() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        String nationalId = "1234567890123";
        searchQuery.setNid(nationalId);
        assertFindAllBy(searchQuery, "nid=" + nationalId);
    }

    @Test
    public void shouldFindPatientsByBirthRegistrationNumber() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        String birthRegNo = "12345678901234567";
        searchQuery.setBin_brn(birthRegNo);
        assertFindAllBy(searchQuery, "bin_brn=" + birthRegNo);
    }

    @Test
    public void shouldFindPatientsByUid() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        String uid = "11111111111";
        searchQuery.setUid(uid);
        assertFindAllBy(searchQuery, "uid=" + uid);
    }

    @Test
    public void shouldFindPatientsByHouseholdCode() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        String householdCode = "1234";
        searchQuery.setHousehold_code(householdCode);
        assertFindAllBy(searchQuery, "household_code=" + householdCode);
    }

    @Test
    public void shouldFindPatientsByAddress() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        String address = "102030";
        searchQuery.setPresent_address(address);
        assertFindAllBy(searchQuery, "present_address=" + address);
    }

    @Test
    public void shouldFindPatientsByAddressAndUid() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        String address = "102030";
        String uid = "11111111111";
        searchQuery.setPresent_address(address);
        searchQuery.setUid(uid);
        assertFindAllBy(searchQuery, format("uid=%s&present_address=%s", uid, address));
    }

    @Test
    public void shouldUpdatePatientAndReturnHealthId() throws Exception {
        PatientData patient = buildPatient();
        String json = new ObjectMapper().writeValueAsString(patient);
        String healthId = "healthId-100";
        MCIResponse mciResponse = new MCIResponse(healthId, ACCEPTED);
        when(patientService.update(patient, healthId)).thenReturn(Observable.just(mciResponse));

        mockMvc.perform(put(buildEndPointWithHealthId(healthId), healthId)
                .content(json).contentType(APPLICATION_JSON))
                .andExpect(request().asyncResult(new ResponseEntity<>(mciResponse, ACCEPTED)));
        verify(patientService).update(patient, healthId);
    }

    @Test
    public void shouldFindPatientsByAddressAndSurName() throws Exception {
        String address = "102030";
        PatientData patient = buildPatient();
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setPresent_address(address);
        searchQuery.setSur_name(patient.getSurName());
        assertFindAllBy(searchQuery, String.format("sur_name=%s&present_address=%s", patient.getSurName(), address));
    }

    @Test
    public void shouldFindPatientsByAddressAndGivenName() throws Exception {
        PatientData patient = buildPatient();
        String address = "102030";
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setPresent_address(address);
        searchQuery.setGiven_name(patient.getGivenName());
        assertFindAllBy(searchQuery, String.format("given_name=%s&present_address=%s", patient.getGivenName(), address));
    }

    private void assertFindAllBy(SearchQuery searchQuery, String queryString) throws Exception {
        int maxLimit = 25;
        searchQuery.setMaximum_limit(maxLimit);
        when(patientService.getPerPageMaximumLimit()).thenReturn(maxLimit);

        List<PatientSummaryData> patientSummaryDataList = new PatientMapper().mapSummary(asList(buildPatient()));
        when(patientService.findAllSummaryByQuery(searchQuery)).thenReturn(patientSummaryDataList);
        MCIMultiResponse mciMultiResponse = new MCIMultiResponse(patientSummaryDataList, new HashMap<String, String>(), OK);

        mockMvc.perform(get(API_END_POINT + "?" + queryString))
                .andExpect(request().asyncResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject)));

        verify(patientService).findAllSummaryByQuery(searchQuery);
    }

    @Test
    public void shouldFindPatientsByAddressAndShowNoteForMoreRecord() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        String address = "102030";
        searchQuery.setPresent_address(address);

        int maxLimit = 1;
        searchQuery.setMaximum_limit(maxLimit);
        when(patientService.getPerPageMaximumLimit()).thenReturn(maxLimit);
        String note = "There are more record for this search criteria. Please narrow down your search";
        when(patientService.getPerPageMaximumLimitNote()).thenReturn(note);

        PatientMapper mapper = new PatientMapper();
        PatientData patient1 = buildPatient();
        PatientData patient2 = new PatientData();
        PatientData patient3 = new PatientData();
        when(patientService.findAllSummaryByQuery(searchQuery)).thenReturn(mapper.mapSummary(asList(patient1, patient2, patient3)));

        String queryString = "present_address=" + address;
        MvcResult mvcResult = mockMvc.perform(get(API_END_POINT + "?" + queryString))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.results[0]").exists())
                .andExpect(jsonPath("$.results[0].nid", is(patient1.getNationalId())))
                .andExpect(jsonPath("$.results[0].given_name", is(patient1.getGivenName())))
                .andExpect(jsonPath("$.results[0].sur_name", is(patient1.getSurName())))
                .andExpect(jsonPath("$.results[1]").doesNotExist())
                .andExpect(jsonPath("$.additional_info.note", is(note)));

        verify(patientService).findAllSummaryByQuery(searchQuery);
    }

    @Test
    public void shouldAddRequestedByWhenPatientIsCreated() throws Exception {
        PatientData patient = buildPatient();
        String healthId = "healthId-100";
        MCIResponse mciResponse = new MCIResponse(healthId, OK);
        when(patientService.createPatientForMCI(patient)).thenReturn(Observable.just(mciResponse));

        String json = writeValueAsString(patient);
        MvcResult mvcResult = mockMvc.perform(post(API_END_POINT).content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        ArgumentCaptor<PatientData> argument = ArgumentCaptor.forClass(PatientData.class);
        verify(patientService).createPatientForMCI(argument.capture());

        Requester requester = argument.getValue().getRequester();
        assertNotNull(requester);
        assertNotNull(requester.getFacility());
        assertEquals(USER_INFO_FACILITY, requester.getFacility().getId());
    }

    @Test
    public void shouldAddRequestedByWhenPatientIsUpdated() throws Exception {
        PatientData patient = buildPatient();
        String json = writeValueAsString(patient);
        String healthId = "healthId-100";
        MCIResponse mciResponse = new MCIResponse(healthId, ACCEPTED);
        when(patientService.update(patient, healthId)).thenReturn(Observable.just(mciResponse));

        MvcResult mvcResult = mockMvc.perform(put(buildEndPointWithHealthId(healthId), healthId).content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted());

        ArgumentCaptor<PatientData> argument1 = ArgumentCaptor.forClass(PatientData.class);
        ArgumentCaptor<String> argument2 = ArgumentCaptor.forClass(String.class);
        verify(patientService).update(argument1.capture(), argument2.capture());

        Requester requester = argument1.getValue().getRequester();
        assertNotNull(requester);
        assertNotNull(requester.getFacility());
        assertEquals(USER_INFO_FACILITY, requester.getFacility().getId());
        assertNull(requester.getProvider());
    }

    private PatientData buildPatient() {
        PatientData patientData = new PatientData();
        patientData.setNationalId("1234567890123");
        patientData.setBirthRegistrationNumber("12345678901234567");
        patientData.setGivenName("Scott");
        patientData.setSurName("Tiger");
        patientData.setGender("M");
        patientData.setDateOfBirth(parseDate("2014-12-01"));
        patientData.setHouseholdCode("1234");

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazilaId("09");
        address.setCityCorporationId("20");
        address.setRuralWardId("01");
        address.setVillage("10");
        address.setCountryCode("050");

        patientData.setAddress(address);
        return patientData;
    }

    private ProviderResponse getProviderResponse(String providerId, String abc, String reference) {
        ProviderResponse response = new ProviderResponse();
        response.setId(providerId);
        response.setName(abc);
        HashMap<String, String> organization = new HashMap<>();
        organization.put("reference", reference);
        response.setOrganization(organization);
        return response;
    }

    private String buildEndPointWithHealthId(String healthId) {
        return API_END_POINT + "/" + healthId;
    }
}

