package org.sharedhealth.mci.web.controller;

import com.datastax.driver.core.utils.UUIDs;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.service.PatientService;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.ParseException;
import java.util.*;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.utils.DateUtil.fromIsoFormat;
import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

public class CatchmentControllerTest {

    private static final String API_END_POINT = "api/v1/catchments";

    @Mock
    private PatientService patientService;
    @Mock
    private LocalValidatorFactoryBean validatorFactory;
    private MockMvc mockMvc;

    @Before
    public void setup() throws ParseException {
        initMocks(this);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CatchmentController(patientService))
                .setValidator(validatorFactory)
                .build();
    }

    @Test
    public void shouldFindPendingApprovalsWithoutGivenTime() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        List<PendingApprovalListResponse> pendingApprovals = new ArrayList<>();
        pendingApprovals.add(buildPendingApprovalListResponse(1));
        pendingApprovals.add(buildPendingApprovalListResponse(2));
        pendingApprovals.add(buildPendingApprovalListResponse(3));

        when(patientService.findPendingApprovalList(catchment, null, null)).thenReturn(pendingApprovals);

        String url = buildPendingApprovalUrl("102030");
        MvcResult mvcResult = mockMvc.perform(get(url))
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

        String url = buildPendingApprovalUrl("102030");
        MvcResult mvcResult = mockMvc.perform(get(url + "?" + AFTER + "=" + after))
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

        String url = buildPendingApprovalUrl("102030");
        MvcResult mvcResult = mockMvc.perform(get(url + "?" + BEFORE + "=" + before))
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

        String url = buildPendingApprovalUrl("102030");
        MvcResult mvcResult = mockMvc.perform(get(url + "?" + AFTER + "=" + after + "&" + BEFORE + "=" + before))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        verify(patientService).findPendingApprovalList(catchment, after, before);
    }

    @Test
    public void shouldFindPendingApprovalDetailsForGivenHealthId() throws Exception {
        String healthId = "health-100";
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(OCCUPATION);
        pendingApproval.setCurrentValue("curr val");

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        UUID timeuuid = UUIDs.timeBased();
        PendingApprovalFieldDetails approvalFieldDetails = new PendingApprovalFieldDetails();
        approvalFieldDetails.setFacilityId("facility-100");
        approvalFieldDetails.setValue("some value");
        approvalFieldDetails.setCreatedAt(unixTimestamp(timeuuid));
        fieldDetailsMap.put(timeuuid, approvalFieldDetails);
        pendingApproval.setFieldDetails(fieldDetailsMap);

        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(pendingApproval);

        String catchmentId = "102030";
        Catchment catchment = new Catchment(catchmentId);
        when(patientService.findPendingApprovalDetails(healthId, catchment)).thenReturn(pendingApprovals);

        String url = buildPendingApprovalUrl(catchmentId, healthId);
        MvcResult mvcResult = mockMvc.perform(get(url))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].field_name", is(OCCUPATION)))
                .andExpect(jsonPath("$.results[0].current_value", is("curr val")))
                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".facility_id", is("facility-100")))
                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".value", is("some value")))
                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".created_at", is(toIsoFormat(unixTimestamp(timeuuid)))));

        verify(patientService).findPendingApprovalDetails(healthId, catchment);
    }

    @Test
    public void shouldAcceptPendingApprovalsForGivenHealthId() throws Exception {
        String healthId = "health-100";
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);

        String catchmentId = "102030";
        Catchment catchment = new Catchment(catchmentId);
        when(patientService.processPendingApprovals(patient, catchment, true)).thenReturn(healthId);

        String url = buildPendingApprovalUrl(catchmentId, healthId);
        String content = writeValueAsString(patient);
        MvcResult mvcResult = mockMvc.perform(put(url).content(content).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(healthId)));

        verify(patientService).processPendingApprovals(patient, catchment, true);
    }

    @Test
    public void shouldRejectPendingApprovalsForGivenHealthId() throws Exception {
        String healthId = "health-100";
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);

        String catchmentId = "102030";
        Catchment catchment = new Catchment(catchmentId);

        when(patientService.processPendingApprovals(patient, catchment, false)).thenReturn(healthId);

        String content = writeValueAsString(patient);
        String url = buildPendingApprovalUrl(catchmentId, healthId);
        MvcResult mvcResult = mockMvc.perform(delete(url).content(content).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(healthId)));

        verify(patientService).processPendingApprovals(patient, catchment, false);
    }

    private String buildPendingApprovalUrl(String catchmentId) {
        return format("/%s/%s/approvals", API_END_POINT, catchmentId);
    }

    private String buildPendingApprovalUrl(String catchmentId, String healthId) {
        return format("/%s/%s/approvals/%s", API_END_POINT, catchmentId, healthId);
    }

    @Test
    public void shouldFindPatientByCatchment() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);

        List<PatientData> patients = asList(buildPatient("h100"), buildPatient("h200"), buildPatient("h300"));
        when(patientService.findAllByCatchment(catchment, null, null, facilityId)).thenReturn(patients);

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String url = format("http://localhost/%s/%s/patients", API_END_POINT, catchmentId);

        String nextUrl = fromUriString(url)
                .queryParam(SINCE, encode(patients.get(2).getUpdatedAtAsString(), "UTF-8"))
                .queryParam(LAST_MARKER, encode(patients.get(2).getUpdatedAt().toString(), "UTF-8")).build().toString();


        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(url)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nextUrl)))

                .andExpect(jsonPath("$.entries.[0].id", is(patients.get(0).getUpdatedAt().toString())))
                .andExpect(jsonPath("$.entries.[0].publishedDate", is(patients.get(0).getUpdatedAtAsString())))
                .andExpect(jsonPath("$.entries.[0].title", is("Patient in Catchment: h100")))
                .andExpect(jsonPath("$.entries.[0].link", is("http://localhost:80/api/v1/patients/h100")))
                .andExpect(jsonPath("$.entries.[0].categories[0]", is("patient")))
                .andExpect(jsonPath("$.entries.[0].content.hid", is("h100")))

                .andExpect(jsonPath("$.entries.[1].id", is(patients.get(1).getUpdatedAt().toString())))
                .andExpect(jsonPath("$.entries.[2].id", is(patients.get(2).getUpdatedAt().toString())));

        verify(patientService).findAllByCatchment(catchment, null, null, facilityId);
    }

    @Test
    public void shouldReturnEmptyListIfNoPatientFound() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);

        when(patientService.findAllByCatchment(catchment, null, null, facilityId)).thenReturn(null);

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String url = format("http://localhost/%s/%s/patients", API_END_POINT, catchmentId);

        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(url)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nullValue())))
                .andExpect(jsonPath("$.entries", is(emptyList())));

        verify(patientService).findAllByCatchment(catchment, null, null, facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentWithSinceQueryParam() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);
        String since = "2000-01-01T10:20:30Z";

        List<PatientData> patients = asList(buildPatient("h100"), buildPatient("h200"), buildPatient("h300"));
        when(patientService.findAllByCatchment(catchment, fromIsoFormat(since), null, facilityId)).thenReturn(patients);

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = format("http://localhost/%s/%s/patients", API_END_POINT, catchmentId);

        String url = fromUriString(requestUrl)
                .queryParam(SINCE, since)
                .build().toString();

        String nextUrl = fromUriString(requestUrl)
                .queryParam(SINCE, encode(patients.get(2).getUpdatedAtAsString(), "UTF-8"))
                .queryParam(LAST_MARKER, encode(patients.get(2).getUpdatedAt().toString(), "UTF-8")).build().toString();

        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(url)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nextUrl)))

                .andExpect(jsonPath("$.entries.[0].id", is(patients.get(0).getUpdatedAt().toString())))
                .andExpect(jsonPath("$.entries.[1].id", is(patients.get(1).getUpdatedAt().toString())))
                .andExpect(jsonPath("$.entries.[2].id", is(patients.get(2).getUpdatedAt().toString())));

        verify(patientService).findAllByCatchment(catchment, fromIsoFormat(since), null, facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentWithSinceAndLastMarkerQueryParams() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);
        String since = "2010-01-01T10:20:30Z";
        UUID lastMarker = timeBased();

        List<PatientData> patients = asList(buildPatient("h100"), buildPatient("h200"), buildPatient("h300"));
        when(patientService.findAllByCatchment(catchment, fromIsoFormat(since), lastMarker, facilityId)).thenReturn(patients);

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = format("http://localhost/%s/%s/patients", API_END_POINT, catchmentId);

        String url = fromUriString(requestUrl)
                .queryParam(SINCE, since)
                .queryParam(LAST_MARKER, lastMarker)
                .build().toString();

        String nextUrl = fromUriString(requestUrl)
                .queryParam(SINCE, encode(patients.get(2).getUpdatedAtAsString(), "UTF-8"))
                .queryParam(LAST_MARKER, encode(patients.get(2).getUpdatedAt().toString(), "UTF-8")).build().toString();

        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(url)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nextUrl)))

                .andExpect(jsonPath("$.entries.[0].id", is(patients.get(0).getUpdatedAt().toString())))
                .andExpect(jsonPath("$.entries.[1].id", is(patients.get(1).getUpdatedAt().toString())))
                .andExpect(jsonPath("$.entries.[2].id", is(patients.get(2).getUpdatedAt().toString())));

        verify(patientService).findAllByCatchment(catchment, fromIsoFormat(since), lastMarker, facilityId);
    }

    @Test
    public void shouldBuildFeedResponse() throws Exception {
        List<PatientData> patients = asList(buildPatient("h100"), buildPatient("h200"), buildPatient("h300"));
        MockHttpServletRequest request = buildHttpRequest(null, null);

        String title = "Patients";
        Feed feed = new CatchmentController(null).buildFeedResponse(patients, request);

        assertEquals("MCI", feed.getAuthor());
        assertEquals(title, feed.getTitle());

        String requestUrl = request.getRequestURL().toString();
        assertEquals(requestUrl, feed.getFeedUrl());

        assertNull(feed.getPrevUrl());

        String nextUrl = feed.getNextUrl();
        assertNotNull(nextUrl);
        assertTrue(nextUrl.startsWith(requestUrl));

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(nextUrl), "UTF-8");
        assertEquals(2, params.size());
        assertEquals(SINCE, params.get(0).getName());
        assertEquals(patients.get(2).getUpdatedAtAsString(), params.get(0).getValue());
        assertEquals(LAST_MARKER, params.get(1).getName());
        assertEquals(patients.get(2).getUpdatedAt().toString(), params.get(1).getValue());

        List<FeedEntry> entries = feed.getEntries();
        assertNotNull(entries);
        assertEquals(patients.size(), entries.size());

        assertFeedEntry(entries.get(0), patients.get(0));
        assertFeedEntry(entries.get(1), patients.get(1));
        assertFeedEntry(entries.get(2), patients.get(2));
    }

    @Test
    public void shouldBuildFeedResponseWithQueryParam() throws Exception {
        List<PatientData> patients = asList(buildPatient("h100"), buildPatient("h200"), buildPatient("h300"));
        MockHttpServletRequest request = buildHttpRequest("2010-01-01T10:20:30Z", "h000");

        String title = "Patients";
        Feed feed = new CatchmentController(null).buildFeedResponse(patients, request);

        assertEquals("MCI", feed.getAuthor());
        assertEquals(title, feed.getTitle());

        String requestUrl = request.getRequestURL().toString();
        String feedUrl = fromUriString(requestUrl)
                .queryParam(SINCE, encode("2010-01-01T10:20:30Z", "UTF-8"))
                .queryParam(LAST_MARKER, encode("h000", "UTF-8")).build().toString();
        assertEquals(feedUrl, feed.getFeedUrl());

        assertNull(feed.getPrevUrl());

        String nextUrl = feed.getNextUrl();
        assertNotNull(nextUrl);
        assertTrue(nextUrl.startsWith(requestUrl));

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(nextUrl), "UTF-8");
        assertEquals(2, params.size());
        assertEquals(SINCE, params.get(0).getName());
        assertEquals(patients.get(2).getUpdatedAtAsString(), params.get(0).getValue());
        assertEquals(LAST_MARKER, params.get(1).getName());
        assertEquals(patients.get(2).getUpdatedAt().toString(), params.get(1).getValue());

        List<FeedEntry> entries = feed.getEntries();
        assertNotNull(entries);
        assertEquals(patients.size(), entries.size());

        assertFeedEntry(entries.get(0), patients.get(0));
        assertFeedEntry(entries.get(1), patients.get(1));
        assertFeedEntry(entries.get(2), patients.get(2));
    }

    private PatientData buildPatient(String healthId) throws InterruptedException {
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);
        patient.setUpdatedAt(timeBased());
        Thread.sleep(1, 10);
        return patient;
    }

    private MockHttpServletRequest buildHttpRequest(String since, String lastMarker) throws UnsupportedEncodingException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("www.mci.com");
        request.setServerPort(8081);
        request.setMethod("GET");
        request.setRequestURI("/api/v1/catchments/102030/patients");

        StringBuilder queryString = new StringBuilder();
        if (isNotEmpty(since)) {
            queryString.append(SINCE).append("=").append(encode(since, "UTF-8"));
        }
        if (isNotEmpty(lastMarker)) {
            queryString.append("&").append(LAST_MARKER).append("=").append(encode(lastMarker, "UTF-8"));
        }
        request.setQueryString(queryString.toString());

        return request;
    }

    private void assertFeedEntry(FeedEntry entry, PatientData patient) {
        assertNotNull(entry);
        String healthId = patient.getHealthId();
        assertEquals(patient.getUpdatedAt(), entry.getId());
        assertEquals(patient.getUpdatedAtAsString(), entry.getPublishedDate());
        assertEquals("Patient in Catchment: " + healthId, entry.getTitle());
        assertEquals("http://www.mci.com:8081/api/v1/patients/" + healthId, entry.getLink());
        assertNotNull(entry.getCategories());
        assertEquals(1, entry.getCategories().length);
        assertEquals("patient", entry.getCategories()[0]);
        assertEquals(patient, entry.getContent());
    }
}