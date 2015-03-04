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
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.service.LocationService;
import org.sharedhealth.mci.web.service.PatientService;
import org.sharedhealth.mci.web.service.SettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.controller.PatientController.REQUESTED_BY;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class PatientControllerTest {

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

    private PatientMapper mapper;

    private PatientData patientData;

    private LocationData location;
    private MockMvc mockMvc;
    private String nationalId = "1234567890123";
    private String birthRegistrationNumber = "12345678901234567";
    private String uid = "11111111111";
    private static final String householdCode = "1234";
    public static final String API_END_POINT = "/api/v1/patients";
    public static final String PUT_API_END_POINT = "/api/v1/patients/{healthId}";
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

        patientData = new PatientData();
        patientData.setNationalId(nationalId);
        patientData.setBirthRegistrationNumber(birthRegistrationNumber);
        patientData.setGivenName("Scott");
        patientData.setSurName("Tiger");
        patientData.setGender("M");
        patientData.setDateOfBirth("2014-12-01");
        patientData.setDateOfBirth("2014-12-01");
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
        MCIMultiResponse mciMultiResponse = new MCIMultiResponse<>(patientSummaryDataList, additionalInfo, OK);

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

        MvcResult mvcResult = mockMvc.perform(post(API_END_POINT).content(json).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        ArgumentCaptor<PatientData> argument = ArgumentCaptor.forClass(PatientData.class);
        verify(patientService).create(argument.capture());
        assertEquals(REQUESTED_BY, argument.getValue().getRequestedBy());
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
        assertEquals(REQUESTED_BY, argument1.getValue().getRequestedBy());
    }
}

