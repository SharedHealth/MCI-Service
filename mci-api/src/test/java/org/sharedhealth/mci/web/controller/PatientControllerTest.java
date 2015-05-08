package org.sharedhealth.mci.web.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.infrastructure.security.TokenAuthentication;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.service.LocationService;
import org.sharedhealth.mci.web.service.PatientService;
import org.sharedhealth.mci.web.service.SettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.web.infrastructure.security.UserInfo.MCI_USER_GROUP;
import static org.sharedhealth.mci.web.service.PatientService.PER_PAGE_MAXIMUM_LIMIT_NOTE;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@RunWith(MockitoJUnitRunner.class)
public class PatientControllerTest {

    public static final String USER_INFO_FACILITY = "100067";
    @Mock
    private PatientService patientService;

    @Mock
    private LocationService locationService;

    @Mock
    private SettingService settingService;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    @Mock
    private SecurityContext securityContext;

    private PatientMapper mapper;

    private PatientData patientData;

    private LocationData location;
    private MockMvc mockMvc;
    private String nationalId = "1234567890123";
    private String birthRegistrationNumber = "12345678901234567";
    private String uid = "11111111111";
    private static final String householdCode = "1234";
    public static final String API_END_POINT = "/patients";
    public static final String PUT_API_END_POINT = "/patients/{healthId}";
    public static final String GEO_CODE = "1004092001";
    private SearchQuery searchQuery;
    private StringBuilder stringBuilder;
    private List<PatientData> patients;
    private int maxLimit;

    @Before
    public void setup() throws ParseException {
        initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PatientController(patientService))
                .setValidator(validator())
                .build();

        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(getUserInfo(), true));

        patientData = new PatientData();
        patientData.setNationalId(nationalId);
        patientData.setBirthRegistrationNumber(birthRegistrationNumber);
        patientData.setGivenName("Scott");
        patientData.setSurName("Tiger");
        patientData.setGender("M");
        patientData.setDateOfBirth(toIsoFormat("2014-12-01"));
        patientData.setHouseholdCode(householdCode);

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazilaId("09");
        address.setCityCorporationId("20");
        address.setVillage("10");
        address.setRuralWardId("01");
        address.setCountryCode("050");

        patientData.setAddress(address);

        location = new LocationData();

        location.setGeoCode(GEO_CODE);
        location.setDivisionId("10");
        location.setDistrictId("04");
        location.setUpazilaId("09");
        location.setCityCorporationId("20");
        location.setUnionOrUrbanWardId("01");

        searchQuery = new SearchQuery();
        stringBuilder = new StringBuilder(200);
        patients = new ArrayList<>();
        maxLimit = 25;
        mapper = new PatientMapper();
    }

    private UserInfo getUserInfo() {
        UserProfile userProfile = new UserProfile("facility", USER_INFO_FACILITY, null);
        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100", asList(MCI_USER_GROUP), asList(userProfile));
    }

    @Test
    public void shouldCreatePatientAndReturnHealthId() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patientData);
        String healthId = "healthId-100";
        MCIResponse mciResponse = new MCIResponse(healthId, CREATED);
        when(locationService.findByGeoCode(GEO_CODE)).thenReturn(location);
        when(patientService.create(patientData)).thenReturn(mciResponse);

        mockMvc.perform(post(API_END_POINT).content(json).contentType(APPLICATION_JSON))
                .andExpect(request().asyncResult(new ResponseEntity<>(mciResponse, CREATED)));
        verify(patientService).create(patientData);
    }

    @Test
    public void shouldFindPatientByHealthId() throws Exception {
        String healthId = "healthId-100";
        when(patientService.findByHealthId(healthId)).thenReturn(patientData);
        mockMvc.perform(get(API_END_POINT + "/" + healthId))
                .andExpect(request().asyncResult(new ResponseEntity<>(patientData, OK)));
        verify(patientService).findByHealthId(healthId);
    }

    @Test
    public void shouldFindPatientsByNationalId() throws Exception {
        searchQuery.setNid(nationalId);
        stringBuilder.append("nid=" + nationalId);
        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    @Test
    public void shouldFindPatientsByBirthRegistrationNumber() throws Exception {
        searchQuery.setBin_brn(birthRegistrationNumber);
        stringBuilder.append("bin_brn=" + birthRegistrationNumber);
        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    @Test
    public void shouldFindPatientsByUid() throws Exception {
        searchQuery.setUid(uid);
        stringBuilder.append("uid=" + uid);
        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    @Test
    public void shouldFindPatientsByHouseholdCode() throws Exception {
        searchQuery.setHousehold_code(householdCode);
        stringBuilder.append("household_code=" + householdCode);
        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    @Test
    public void shouldFindPatientsByAddress() throws Exception {
        String address = location.getDivisionId() + location.getDistrictId() + location.getUpazilaId();
        searchQuery.setPresent_address(address);
        stringBuilder.append("present_address=" + address);
        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    @Test
    public void shouldFindPatientsByAddressAndUid() throws Exception {
        StringBuilder stringBuilder = new StringBuilder(200);
        String address = location.getDivisionId() + location.getDistrictId() + location.getUpazilaId();
        searchQuery.setPresent_address(address);
        searchQuery.setUid(uid);
        stringBuilder.append("uid=" + uid);
        stringBuilder.append("&present_address=" + address);

        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    @Test
    public void shouldFindPatientsByAddressAndShowNoteForMoreRecord() throws Exception {

        StringBuilder stringBuilder = new StringBuilder(200);
        String address = location.getDivisionId() + location.getDistrictId() + location.getUpazilaId();
        searchQuery.setPresent_address(address);
        stringBuilder.append("present_address=" + address);

        patients.add(patientData);
        patients.add(patientData);
        patients.add(patientData);
        maxLimit = 4;

        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    private void assertFindAllBy(SearchQuery searchQuery, String queryString) throws Exception {
        patients.add(patientData);

        searchQuery.setMaximum_limit(maxLimit);

        when(patientService.getPerPageMaximumLimit()).thenReturn(maxLimit);
        when(patientService.getPerPageMaximumLimitNote()).thenReturn("There are more record for this search criteria. Please narrow down your search");

        final int limit = patientService.getPerPageMaximumLimit();
        final String note = patientService.getPerPageMaximumLimitNote();
        HashMap<String, String> additionalInfo = new HashMap<>();
        if (patients.size() > limit) {
            patients.remove(limit);
            additionalInfo.put("note", note);
        }

        List<PatientSummaryData> patientSummaryDataList = mapper.mapSummary(patients);

        when(patientService.findAllSummaryByQuery(searchQuery)).thenReturn(patientSummaryDataList);
        MCIMultiResponse mciMultiResponse = new MCIMultiResponse(patientSummaryDataList, additionalInfo, OK);

        mockMvc.perform(get(API_END_POINT + "?" + queryString))
                .andExpect(request().asyncResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject)));

        verify(patientService).findAllSummaryByQuery(searchQuery);
    }

    @Test
    public void shouldUpdatePatientAndReturnHealthId() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patientData);
        String healthId = "healthId-100";
        MCIResponse mciResponse = new MCIResponse(healthId, ACCEPTED);
        when(locationService.findByGeoCode(GEO_CODE)).thenReturn(location);
        when(patientService.update(patientData, healthId)).thenReturn(mciResponse);

        mockMvc.perform(put(PUT_API_END_POINT, healthId).content(json).contentType(APPLICATION_JSON))
                .andExpect(request().asyncResult(new ResponseEntity<>(mciResponse, ACCEPTED)));
        verify(patientService).update(patientData, healthId);
    }

    @Test
    public void shouldFindPatientsByAddressAndSurName() throws Exception {
        StringBuilder stringBuilder = new StringBuilder(200);
        String address = location.getDivisionId() + location.getDistrictId() + location.getUpazilaId();
        searchQuery.setPresent_address(address);
        searchQuery.setSur_name(patientData.getSurName());
        stringBuilder.append("sur_name=" + patientData.getSurName());
        stringBuilder.append("&present_address=" + address);

        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    @Test
    public void shouldFindPatientsByAddressAndGivenName() throws Exception {
        StringBuilder stringBuilder = new StringBuilder(200);
        String address = location.getDivisionId() + location.getDistrictId() + location.getUpazilaId();
        searchQuery.setPresent_address(address);
        searchQuery.setGiven_name(patientData.getGivenName());
        stringBuilder.append("given_name=" + patientData.getGivenName());
        stringBuilder.append("&present_address=" + address);

        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    @Test
    public void shouldFindPatientsByAddressAndSurNameAndShowNoteForMoreRecord() throws Exception {

        StringBuilder stringBuilder = new StringBuilder(200);
        String address = location.getDivisionId() + location.getDistrictId() + location.getUpazilaId();
        searchQuery.setPresent_address(address);
        stringBuilder.append("present_address=" + address);
        stringBuilder.append("&sur_name=" + patientData.getSurName());
        searchQuery.setSur_name(patientData.getSurName());

        patients.add(patientData);
        patients.add(patientData);
        patients.add(patientData);
        maxLimit = 4;

        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    private LocalValidatorFactoryBean validator() {
        return localValidatorFactoryBean;
    }

    @Test
    public void shouldAddRequestedByWhenPatientIsCreated() throws Exception {
        String json = writeValueAsString(patientData);
        String healthId = "healthId-100";
        MCIResponse mciResponse = new MCIResponse(healthId, OK);
        when(patientService.create(patientData)).thenReturn(mciResponse);

        MvcResult mvcResult = mockMvc.perform(post(API_END_POINT).content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        ArgumentCaptor<PatientData> argument = ArgumentCaptor.forClass(PatientData.class);
        verify(patientService).create(argument.capture());

        Requester requester = argument.getValue().getRequester();
        assertNotNull(requester);
        assertNotNull(requester.getFacility());
        assertEquals(USER_INFO_FACILITY, requester.getFacility().getId());
    }

    @Test
    public void shouldAddRequestedByWhenPatientIsUpdated() throws Exception {
        String json = writeValueAsString(patientData);
        String healthId = "healthId-100";
        MCIResponse mciResponse = new MCIResponse(healthId, ACCEPTED);
        when(patientService.update(patientData, healthId)).thenReturn(mciResponse);

        MvcResult mvcResult = mockMvc.perform(put(PUT_API_END_POINT, healthId).content(json).contentType(APPLICATION_JSON))
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

    @Test
    public void shouldTruncateSearchResultWhenResultSizeExceedsMaxLimit() throws Exception {
        when(patientService.getPerPageMaximumLimitNote()).thenReturn(PER_PAGE_MAXIMUM_LIMIT_NOTE);
        when(patientService.getPerPageMaximumLimit()).thenReturn(3);

        searchQuery.setMaximum_limit(3);
        searchQuery.setNid(nationalId);
        List<PatientSummaryData> patientSummaryDataList = asList(buildPatientSummaryData("100"),
                buildPatientSummaryData("200"), buildPatientSummaryData("300"), buildPatientSummaryData("400"),
                buildPatientSummaryData("500"));
        when(patientService.findAllSummaryByQuery(searchQuery)).thenReturn(patientSummaryDataList);

        MvcResult mvcResult = mockMvc.perform(get(API_END_POINT + "?nid=" + nationalId))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.results[0]").exists())
                .andExpect(jsonPath("$.results[1]").exists())
                .andExpect(jsonPath("$.results[2]").exists())
                .andExpect(jsonPath("$.results[3]").doesNotExist())
                .andExpect(jsonPath("$.additional_info.note", is(PER_PAGE_MAXIMUM_LIMIT_NOTE)));

        verify(patientService).findAllSummaryByQuery(searchQuery);
    }

    private PatientSummaryData buildPatientSummaryData(String healthId) {
        PatientSummaryData data = new PatientSummaryData();
        data.setHealthId(healthId);
        data.setGivenName("Name" + healthId);
        return data;
    }
}

