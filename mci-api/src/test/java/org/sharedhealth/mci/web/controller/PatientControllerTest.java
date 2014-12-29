package org.sharedhealth.mci.web.controller;


import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.service.LocationService;
import org.sharedhealth.mci.web.service.PatientService;
import org.sharedhealth.mci.web.service.SettingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.text.ParseException;
import java.util.*;

import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

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

    private PatientData patient1;

    private PatientController controller;
    private PatientData patient2;
    private LocationData location;
    private MockMvc mockMvc;
    private String nationalId = "1234567890123";
    private String birthRegistrationNumber = "12345678901234567";
    private String fullname = "Scott Tiger";
    private String uid = "11111111111";
    public static final String API_END_POINT = "/api/v1/patients";
    public static final String PUT_API_END_POINT = "/api/v1/patients/{healthId}";
    public static final String PENDING_APPROVALS_API = "/api/v1/patients/pendingapprovals";
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

        patient1 = new PatientData();
        patient1.setNationalId(nationalId);
        patient1.setBirthRegistrationNumber(birthRegistrationNumber);
        patient1.setGivenName("Scott");
        patient1.setSurName("Tiger");
        patient1.setGender("M");
        patient1.setDateOfBirth("2014-12-01");

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazilaId("09");
        address.setCityCorporationId("20");
        address.setVillage("10");
        address.setRuralWardId("01");
        address.setCountryCode("050");

        patient1.setAddress(address);

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
        String json = new ObjectMapper().writeValueAsString(patient2);
        String healthId = "healthId-100";
        MCIResponse mciResponse = new MCIResponse(healthId, CREATED);
        when(locationService.findByGeoCode(GEO_CODE)).thenReturn(location);
        when(patientService.create(patient2)).thenReturn(mciResponse);

        mockMvc.perform(post(API_END_POINT).content(json).contentType(APPLICATION_JSON))
                .andExpect(request().asyncResult(new ResponseEntity<>(mciResponse, CREATED)));
        verify(patientService).create(patient2);
    }

    @Test
    public void shouldFindPatientByHealthId() throws Exception {
        String healthId = "healthId-100";
        when(patientService.findByHealthId(healthId)).thenReturn(patient2);
        mockMvc.perform(get(API_END_POINT + "/" + healthId))
                .andExpect(request().asyncResult(new ResponseEntity<>(patient2, OK)));
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
    public void shouldFindPatientsByName() throws Exception {
        searchQuery.setFull_name(fullname);
        stringBuilder.append("full_name=" + fullname);
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

        patients.add(patient2);
        patients.add(patient2);
        patients.add(patient2);
        maxLimit = 4;

        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    private void assertFindAllBy(SearchQuery searchQuery, String queryString) throws Exception {
        patients.add(patient1);

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
        String json = new ObjectMapper().writeValueAsString(patient2);
        String healthId = "healthId-100";
        MCIResponse mciResponse = new MCIResponse(healthId, ACCEPTED);
        when(locationService.findByGeoCode(GEO_CODE)).thenReturn(location);
        when(patientService.update(patient2, healthId)).thenReturn(mciResponse);

        mockMvc.perform(put(PUT_API_END_POINT, healthId).content(json).contentType(APPLICATION_JSON))
                .andExpect(request().asyncResult(new ResponseEntity<>(mciResponse, ACCEPTED)));
        verify(patientService).update(patient2, healthId);

    }

    @Test
    public void shouldFindPatientsByAddressAndSurName() throws Exception {
        StringBuilder stringBuilder = new StringBuilder(200);
        String address = location.getDivisionId() + location.getDistrictId() + location.getUpazilaId();
        searchQuery.setPresent_address(address);
        searchQuery.setSur_name(patient1.getSurName());
        stringBuilder.append("sur_name=" + patient1.getSurName());
        stringBuilder.append("&present_address=" + address);

        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    @Test
    public void shouldFindPatientsByAddressAndGivenName() throws Exception {
        StringBuilder stringBuilder = new StringBuilder(200);
        String address = location.getDivisionId() + location.getDistrictId() + location.getUpazilaId();
        searchQuery.setPresent_address(address);
        searchQuery.setGiven_name(patient1.getGivenName());
        stringBuilder.append("given_name=" + patient1.getGivenName());
        stringBuilder.append("&present_address=" + address);

        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    @Test
    public void shouldFindPatientsByAddressAndSurNameAndShowNoteForMoreRecord() throws Exception {

        StringBuilder stringBuilder = new StringBuilder(200);
        String address = location.getDivisionId() + location.getDistrictId() + location.getUpazilaId();
        searchQuery.setPresent_address(address);
        stringBuilder.append("present_address=" + address);
        stringBuilder.append("&sur_name=" + patient1.getSurName());
        searchQuery.setSur_name(patient1.getSurName());

        patients.add(patient2);
        patients.add(patient2);
        patients.add(patient2);
        maxLimit = 4;

        assertFindAllBy(searchQuery, stringBuilder.toString());
    }

    private LocalValidatorFactoryBean validator() {
        return localValidatorFactoryBean;
    }

    public void shouldNotUpdatePatient_FieldsMarkedForApproval() throws Exception {

        String healthId = "healthId-100";
        patient2.setHealthId(healthId);

        PatientData patientUpdated = buildPatientData();
        patientUpdated.setGivenName("Bulla");
        patientUpdated.setGender("F");
        patientUpdated.setDateOfBirth("2000-02-10");
        patientUpdated.setHealthId(healthId);
        String json = new ObjectMapper().writeValueAsString(patientUpdated);
        PatientData patientModified = buildPatientData();
        patientModified.setGivenName("Bulla");

        when(patientService.findByHealthId(healthId)).thenReturn(patientModified);
        when(patientRepository.findByHealthId(healthId)).thenReturn(patient2);
        when(patientService.update(patientUpdated, healthId)).thenReturn(new MCIResponse(healthId, ACCEPTED));

        mockMvc.perform(put(PUT_API_END_POINT, healthId).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isAccepted());
        mockMvc.perform(get(API_END_POINT + "/" + healthId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nid", is(nationalId)))
                .andExpect(jsonPath("$.bin_brn", is(birthRegistrationNumber)))
                .andExpect(jsonPath("$.given_name", is("Bulla")))
                .andExpect(jsonPath("$.sur_name", is("Tiger")))
                .andExpect(jsonPath("$.date_of_birth", is("2014-12-01")))
                .andExpect(jsonPath("$.present_address.address_line", is("house-10")));
        verify(patientService).update(patientUpdated, healthId);
        verify(patientService).findByHealthId(healthId);

    }

    private PatientData buildPatientData() throws ParseException {
        patient2 = new PatientData();
        patient2.setNationalId(nationalId);
        patient2.setBirthRegistrationNumber(birthRegistrationNumber);
        patient2.setGivenName("Scott");
        patient2.setSurName("Tiger");
        patient2.setGender("M");
        patient2.setDateOfBirth("2014-12-01");

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazilaId("09");
        address.setCityCorporationId("20");
        address.setVillage("10");
        address.setRuralWardId("01");
        address.setCountryCode("050");

        patient2.setAddress(address);

        location = new LocationData();

        location.setGeoCode(GEO_CODE);
        location.setDivisionId("10");
        location.setDistrictId("04");
        location.setUpazilaId("09");
        location.setCityCorporationId("20");
        location.setUnionOrUrbanWardId("01");
        return patient2;
    }

    @Test
    public void shouldFindPendingApprovalsWithoutGivenTime() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        List<PendingApprovalListResponse> pendingApprovals = new ArrayList<>();
        pendingApprovals.add(buildPendingApprovalListResponse(1));
        pendingApprovals.add(buildPendingApprovalListResponse(2));
        pendingApprovals.add(buildPendingApprovalListResponse(3));

        when(patientService.findPendingApprovalList(catchment, null, null)).thenReturn(pendingApprovals);

        HttpHeaders headers = new HttpHeaders();
        headers.add(DIVISION_ID, "10");
        headers.add(DISTRICT_ID, "20");
        headers.add(UPAZILA_ID, "30");

        MvcResult mvcResult = mockMvc.perform(get(PENDING_APPROVALS_API).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].hid", is("hid-1")))
                .andExpect(jsonPath("$.results[0].given_name", is("Scott-1")))
                .andExpect(jsonPath("$.results[0].sur_name", is("Tiger-1")))
                .andExpect(jsonPath("$.results[0].last_updated", is(pendingApprovals.get(0).getLastUpdated().toString())))

                .andExpect(jsonPath("$.results[1].hid", is("hid-2")))
                .andExpect(jsonPath("$.results[1].given_name", is("Scott-2")))
                .andExpect(jsonPath("$.results[1].sur_name", is("Tiger-2")))
                .andExpect(jsonPath("$.results[1].last_updated", is(pendingApprovals.get(1).getLastUpdated().toString())))

                .andExpect(jsonPath("$.results[2].hid", is("hid-3")))
                .andExpect(jsonPath("$.results[2].given_name", is("Scott-3")))
                .andExpect(jsonPath("$.results[2].sur_name", is("Tiger-3")))
                .andExpect(jsonPath("$.results[2].last_updated", is(pendingApprovals.get(2).getLastUpdated().toString())));

        verify(patientService).findPendingApprovalList(catchment, null, null);
    }

    private PendingApprovalListResponse buildPendingApprovalListResponse(int suffix) {
        PendingApprovalListResponse pendingApproval = new PendingApprovalListResponse();
        pendingApproval.setHealthId("hid-" + suffix);
        pendingApproval.setGivenName("Scott-" + suffix);
        pendingApproval.setSurname("Tiger-" + suffix);
        pendingApproval.setLastUpdated(UUID.randomUUID());
        return pendingApproval;
    }

    @Test
    public void shouldFindPendingApprovalsAfterGivenTime() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        UUID after = UUIDs.timeBased();
        when(patientService.findPendingApprovalList(catchment, after, null)).thenReturn(new ArrayList<PendingApprovalListResponse>());

        HttpHeaders headers = new HttpHeaders();
        headers.add(DIVISION_ID, "10");
        headers.add(DISTRICT_ID, "20");
        headers.add(UPAZILA_ID, "30");

        MvcResult mvcResult = mockMvc.perform(
                get(PENDING_APPROVALS_API + "?" + AFTER + "=" + after).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        verify(patientService).findPendingApprovalList(catchment, after, null);
    }

    @Test
    public void shouldFindPendingApprovalsABeforeGivenTime() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        UUID before = UUIDs.timeBased();
        when(patientService.findPendingApprovalList(catchment, null, before)).thenReturn(new ArrayList<PendingApprovalListResponse>());

        HttpHeaders headers = new HttpHeaders();
        headers.add(DIVISION_ID, "10");
        headers.add(DISTRICT_ID, "20");
        headers.add(UPAZILA_ID, "30");

        MvcResult mvcResult = mockMvc.perform(
                get(PENDING_APPROVALS_API + "?" + BEFORE + "=" + before).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        verify(patientService).findPendingApprovalList(catchment, null, before);
    }

    @Test
    public void shouldFindPendingApprovalsABetweenGivenTimes() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        UUID after = UUIDs.timeBased();
        UUID before = UUIDs.timeBased();
        when(patientService.findPendingApprovalList(catchment, after, before)).thenReturn(new ArrayList<PendingApprovalListResponse>());

        HttpHeaders headers = new HttpHeaders();
        headers.add(DIVISION_ID, "10");
        headers.add(DISTRICT_ID, "20");
        headers.add(UPAZILA_ID, "30");

        MvcResult mvcResult = mockMvc.perform(
                get(PENDING_APPROVALS_API + "?" + AFTER + "=" + after + "&" + BEFORE + "=" + before).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        verify(patientService).findPendingApprovalList(catchment, after, before);
    }

    @Test
    public void shouldFindPendingApprovalDetailsForGivenHealthId() throws Exception {
        String healthId = "health-100";
        PendingApproval details = new PendingApproval();
        details.setName("x_y_z");
        details.setCurrentValue("curr val");

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        UUID timeuuid = UUIDs.timeBased();
        PendingApprovalFieldDetails approvalFieldDetails = new PendingApprovalFieldDetails();
        approvalFieldDetails.setFacilityId("facility-100");
        approvalFieldDetails.setValue("some value");
        approvalFieldDetails.setCreatedAt(unixTimestamp(timeuuid));
        fieldDetailsMap.put(timeuuid, approvalFieldDetails);
        details.setFieldDetails(fieldDetailsMap);

        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(details);
        when(patientService.findPendingApprovalDetails(healthId)).thenReturn(pendingApprovals);

        MvcResult mvcResult = mockMvc.perform(get(PENDING_APPROVALS_API + "/" + healthId))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].field_name", is("x_y_z")))
                .andExpect(jsonPath("$.results[0].current_value", is("curr val")))
                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".facility_id", is("facility-100")))
                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".value", is("some value")))
                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".created_at", is(toIsoFormat(unixTimestamp(timeuuid)))));

        verify(patientService).findPendingApprovalDetails(healthId);
    }

    @Test
    public void shouldAcceptPendingApprovalsForGivenHealthId() throws Exception {
        String healthId = "health-100";
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);

        HttpHeaders headers = new HttpHeaders();
        headers.add(DIVISION_ID, "10");
        headers.add(DISTRICT_ID, "20");
        headers.add(UPAZILA_ID, "30");
        Catchment catchment = new PatientController(patientService).buildCatchment(headers);

        when(patientService.processPendingApprovals(patient, catchment, true)).thenReturn(healthId);

        String content = writeValueAsString(patient);
        MvcResult mvcResult = mockMvc.perform(put(PENDING_APPROVALS_API + "/" + healthId).content(content)
                .contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted());
    }
}

