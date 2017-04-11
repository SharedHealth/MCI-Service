package org.sharedhealth.mci.web.controller;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.constant.JsonConstants;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.util.DateUtil;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.infrastructure.security.TokenAuthentication;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.mapper.Feed;
import org.sharedhealth.mci.web.mapper.FeedEntry;
import org.sharedhealth.mci.web.mapper.PendingApprovalListResponse;
import org.sharedhealth.mci.web.service.PatientService;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.ParseException;
import java.util.*;

import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;
import static org.sharedhealth.mci.domain.util.DateUtil.convertToDateStringIsoMillisFormat;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;
import static org.sharedhealth.mci.domain.util.JsonMapper.writeValueAsString;
import static org.sharedhealth.mci.web.infrastructure.security.UserInfo.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

public class CatchmentControllerTest {

    private static final String API_END_POINT = "catchments";
    private static final int MAX_PAGE_SIZE = 3;
    private static final String REQUEST_URL = "http://mci.dghs.com:8081";

    private static final String FACILITY_ID = "100067";
    private static final String PROVIDER_ID = "100068";

    @Mock
    private PatientService patientService;
    @Mock
    private MCIProperties properties;
    @Mock
    private LocalValidatorFactoryBean validatorFactory;

    private MockMvc mockMvc;
    private CatchmentController catchmentController;

    @Before
    public void setup() throws ParseException {
        initMocks(this);
        catchmentController = new CatchmentController(patientService, properties);
        mockMvc = MockMvcBuilders
                .standaloneSetup(catchmentController)
                .setValidator(validatorFactory)
                .build();

        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(getUserInfo(), true));

        when(patientService.getPerPageMaximumLimit()).thenReturn(MAX_PAGE_SIZE);
    }

    private UserInfo getUserInfo() {
        UserProfile facilityProfile = new UserProfile("facility", FACILITY_ID, asList("1020"));
        UserProfile providerProfile = new UserProfile("provider", PROVIDER_ID, asList("102030"));
        UserProfile adminProfile = new UserProfile("mci-supervisor", "102", asList("10"));

        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(HRM_MCI_USER_GROUP, HRM_MCI_ADMIN, HRM_MCI_APPROVER, HRM_FACILITY_ADMIN_GROUP, HRM_PROVIDER_GROUP)),
                asList(facilityProfile, providerProfile, adminProfile));
    }

    @Test
    public void shouldFindPendingApprovalsWithoutGivenTime() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        List<PendingApprovalListResponse> pendingApprovals = asList(buildPendingApprovalListResponse(1),
                buildPendingApprovalListResponse(2), buildPendingApprovalListResponse(3));
        when(patientService.findPendingApprovalList(catchment, null, null, MAX_PAGE_SIZE + 1)).thenReturn(pendingApprovals);
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

        verify(patientService).findPendingApprovalList(catchment, null, null, MAX_PAGE_SIZE + 1);
    }

    @Test
    public void shouldFindPendingApprovalsWithNextUrlSet() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        List<PendingApprovalListResponse> pendingApprovals = new ArrayList<>();
        for (int x = 1; x <= MAX_PAGE_SIZE + 1; x++) {
            pendingApprovals.add(buildPendingApprovalListResponse(x));
        }

        String nextUrl = fromUriString(format(REQUEST_URL + "/%s/%s/approvals", API_END_POINT, "102030"))
                .queryParam("after", pendingApprovals.get(2).getLastUpdated()).build().toString();

        when(patientService.findPendingApprovalList(catchment, null, null, MAX_PAGE_SIZE + 1)).thenReturn(pendingApprovals);
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
                .andExpect(jsonPath("$.results[2].last_updated", is(pendingApprovals.get(2).getLastUpdated().toString())))
                .andExpect(jsonPath("$.additional_info.next", is(nextUrl)));


        verify(patientService).findPendingApprovalList(catchment, null, null, MAX_PAGE_SIZE + 1);
    }

    @Test
    public void shouldFindPendingApprovalsAfterGivenTime() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        UUID after = TimeUuidUtil.uuidForDate(new Date());
        when(patientService.findPendingApprovalList(catchment, after, null, MAX_PAGE_SIZE + 1)).thenReturn(new ArrayList<PendingApprovalListResponse>());

        String url = buildPendingApprovalUrl("102030");
        MvcResult mvcResult = mockMvc.perform(get(url + "?" + AFTER + "=" + after))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        verify(patientService).findPendingApprovalList(catchment, after, null, MAX_PAGE_SIZE + 1);
    }

    @Test
    public void shouldFindPendingApprovalsAfterGivenTimeWithPreviousUrlSet() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        UUID after = TimeUuidUtil.uuidForDate(new Date());
        List<PendingApprovalListResponse> pendingApprovals = new ArrayList<>();
        for (int x = 1; x <= MAX_PAGE_SIZE + 1; x++) {
            pendingApprovals.add(buildPendingApprovalListResponse(x));
        }

        String previousUrl = fromUriString(format("%s/%s/%s/approvals", REQUEST_URL, API_END_POINT, "102030"))
                .queryParam("before", pendingApprovals.get(0).getLastUpdated()).build().toString();

        when(patientService.findPendingApprovalList(catchment, after, null, MAX_PAGE_SIZE + 1)).thenReturn(pendingApprovals);

        String url = buildPendingApprovalUrl("102030");
        MvcResult mvcResult = mockMvc.perform(get(url + "?" + AFTER + "=" + after))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.additional_info.previous", is(previousUrl)));

        verify(patientService).findPendingApprovalList(catchment, after, null, MAX_PAGE_SIZE + 1);
    }

    @Test
    public void shouldFindPendingApprovalsABeforeGivenTime() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        UUID before = TimeUuidUtil.uuidForDate(new Date());
        when(patientService.findPendingApprovalList(catchment, null, before, MAX_PAGE_SIZE + 1)).thenReturn(new ArrayList<PendingApprovalListResponse>());

        String url = buildPendingApprovalUrl("102030");
        MvcResult mvcResult = mockMvc.perform(get(url + "?" + BEFORE + "=" + before))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        verify(patientService).findPendingApprovalList(catchment, null, before, MAX_PAGE_SIZE + 1);
    }

    @Test
    public void shouldFindPendingApprovalsABeforeGivenTimeWithNextUrlSet() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        UUID before = TimeUuidUtil.uuidForDate(new Date());
        List<PendingApprovalListResponse> pendingApprovals = new ArrayList<>();
        for (int x = 1; x <= MAX_PAGE_SIZE + 1; x++) {
            pendingApprovals.add(buildPendingApprovalListResponse(x));
        }
        when(patientService.findPendingApprovalList(catchment, null, before, MAX_PAGE_SIZE + 1)).thenReturn(pendingApprovals);

        String nextUrl = fromUriString(format("%s/%s/%s/approvals", REQUEST_URL, API_END_POINT, "102030"))
                .queryParam("after", pendingApprovals.get(3).getLastUpdated()).build().toString();

        String url = buildPendingApprovalUrl("102030");
        MvcResult mvcResult = mockMvc.perform(get(url + "?" + BEFORE + "=" + before))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.additional_info.next", is(nextUrl)));

        verify(patientService).findPendingApprovalList(catchment, null, before, MAX_PAGE_SIZE + 1);
    }

    @Test
    public void shouldFindPendingApprovalsABetweenGivenTimes() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        UUID after = TimeUuidUtil.uuidForDate(new Date());
        UUID before = TimeUuidUtil.uuidForDate(new Date());
        when(patientService.findPendingApprovalList(catchment, after, before, MAX_PAGE_SIZE + 1)).thenReturn(null);

        String url = buildPendingApprovalUrl("102030");
        MvcResult mvcResult = mockMvc.perform(get(url + "?" + AFTER + "=" + after + "&" + BEFORE + "=" + before))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        verify(patientService).findPendingApprovalList(catchment, after, before, MAX_PAGE_SIZE + 1);
    }

    @Test
    public void shouldFindPendingApprovalDetailsForGivenHealthId() throws Exception {
        String healthId = "health-100";
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(OCCUPATION);
        pendingApproval.setCurrentValue("curr val");

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        UUID timeuuid = TimeUuidUtil.uuidForDate(new Date());
        PendingApprovalFieldDetails approvalFieldDetails = new PendingApprovalFieldDetails();
        approvalFieldDetails.setRequestedBy(new Requester("facility-100", "provider-100"));
        approvalFieldDetails.setValue("some value");
        approvalFieldDetails.setCreatedAt(TimeUuidUtil.getTimeFromUUID(timeuuid));
        fieldDetailsMap.put(timeuuid, approvalFieldDetails);
        pendingApproval.addFieldDetails(fieldDetailsMap);

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

                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".requested_by.facility").exists())
                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".requested_by.facility.id", is("facility-100")))
                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".requested_by.facility.name", is(nullValue())))

                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".requested_by.provider").exists())
                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".requested_by.provider.id", is("provider-100")))
                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".requested_by.provider.name", is(nullValue())))

                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".value", is("some value")))
                .andExpect(jsonPath("$.results[0].field_details." + timeuuid + ".created_at", is(DateUtil.toIsoMillisFormat(
                        TimeUuidUtil.getTimeFromUUID(timeuuid)))));

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

    @Test
    public void shouldReturnEmptyListForInvalidLastMarker() throws Exception {

        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);

        when(patientService.findAllByCatchment(catchment, null, null)).thenReturn(null);

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = format("%s/%s/%s/patients?" + LAST_MARKER + "=123", REQUEST_URL, API_END_POINT, catchmentId);

        String feedUrl = format("%s/%s/%s/patients?" + LAST_MARKER + "=123", REQUEST_URL, API_END_POINT, catchmentId);

        MvcResult mvcResult = mockMvc.perform(get(requestUrl).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(feedUrl)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nullValue())))
                .andExpect(jsonPath("$.entries", is(emptyList())));

        verify(patientService).findAllByCatchment(catchment, null, null);
    }

    private String buildPendingApprovalUrl(String catchmentId) {
        return format("%s/%s/%s/approvals", REQUEST_URL, API_END_POINT, catchmentId);
    }

    private String buildPendingApprovalUrl(String catchmentId, String healthId) {
        return format("%s/%s/%s/approvals/%s", REQUEST_URL, API_END_POINT, catchmentId, healthId);
    }

    @Test
    public void shouldFindPatientByCatchment() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);


        List<Map<String, Object>> catchmentEvents = asList(buildCatchmentEvent("h100"), buildCatchmentEvent("h200"), buildCatchmentEvent("h300"));
        when(patientService.findAllByCatchment(catchment, null, null)).thenReturn(catchmentEvents);

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = format("%s/%s/%s/patients", REQUEST_URL, API_END_POINT, catchmentId);
        String feedUrl = format("%s/%s/%s/patients", REQUEST_URL, API_END_POINT, catchmentId);

        UUID eventId2 = (UUID) catchmentEvents.get(2).get("eventId");
        String nextUrl = fromUriString(feedUrl)
                .queryParam(SINCE, encode(convertToDateStringIsoMillisFormat(eventId2), "UTF-8"))
                .queryParam(LAST_MARKER, encode(eventId2.toString(), "UTF-8")).build().toString();


        MvcResult mvcResult = mockMvc.perform(get(requestUrl).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(feedUrl)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nextUrl)))

                .andExpect(jsonPath("$.entries.[0].id", is(catchmentEvents.get(0).get("eventId").toString())))
                .andExpect(jsonPath("$.entries.[0].publishedDate", is(convertToDateStringIsoMillisFormat((UUID) catchmentEvents.get(0).get("eventId")))))
                .andExpect(jsonPath("$.entries.[0].title", is("Patient in Catchment: h100")))
                .andExpect(jsonPath("$.entries.[0].link", is(REQUEST_URL + "/patients/h100")))
                .andExpect(jsonPath("$.entries.[0].categories[0]", is("patient")))
                .andExpect(jsonPath("$.entries.[0].content.hid", is("h100")))

                .andExpect(jsonPath("$.entries.[1].id", is(catchmentEvents.get(1).get("eventId").toString())))
                .andExpect(jsonPath("$.entries.[1].publishedDate", is(convertToDateStringIsoMillisFormat((UUID) catchmentEvents.get(1).get("eventId")))))
                .andExpect(jsonPath("$.entries.[2].id", is(catchmentEvents.get(2).get("eventId").toString())))
                .andExpect(jsonPath("$.entries.[2].publishedDate", is(convertToDateStringIsoMillisFormat((UUID) catchmentEvents.get(2).get("eventId")))));

        verify(patientService).findAllByCatchment(catchment, null, null);
    }

    @Test
    public void shouldReturnEmptyListIfNoPatientFound() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);

        when(patientService.findAllByCatchment(catchment, null, null)).thenReturn(null);

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = format("%s/%s/%s/patients", REQUEST_URL, API_END_POINT, catchmentId);
        String feedUrl = format("%s/%s/%s/patients", REQUEST_URL, API_END_POINT, catchmentId);

        MvcResult mvcResult = mockMvc.perform(get(requestUrl).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(feedUrl)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nullValue())))
                .andExpect(jsonPath("$.entries", is(emptyList())));

        verify(patientService).findAllByCatchment(catchment, null, null);
    }

    @Test
    public void shouldFindPatientByCatchmentWithSinceQueryParam() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);
        String since = "2000-01-01T10:20:30Z";

        List<Map<String, Object>> catchmentEvents = asList(buildCatchmentEvent("h100"), buildCatchmentEvent("h200"), buildCatchmentEvent("h300"));
        when(patientService.findAllByCatchment(catchment, parseDate(since), null)).thenReturn(catchmentEvents);

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = fromUriString(format("%s/%s/%s/patients", REQUEST_URL, API_END_POINT, catchmentId))
                .queryParam(SINCE, since)
                .build().toString();

        String feedUrl = fromUriString(format("%s/%s/%s/patients", REQUEST_URL, API_END_POINT, catchmentId))
                .queryParam(SINCE, since)
                .build().toString();

        String nextUrl = fromUriString(format("%s/%s/%s/patients", REQUEST_URL, API_END_POINT, catchmentId))
                .queryParam(SINCE, encode(convertToDateStringIsoMillisFormat((UUID) catchmentEvents.get(2).get("eventId")), "UTF-8"))
                .queryParam(LAST_MARKER, encode(catchmentEvents.get(2).get("eventId").toString(), "UTF-8")).build().toString();

        MvcResult mvcResult = mockMvc.perform(get(requestUrl)
                .contentType(APPLICATION_JSON)
                .headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(feedUrl)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nextUrl)))

                .andExpect(jsonPath("$.entries.[0].id", is(catchmentEvents.get(0).get("eventId").toString())))
                .andExpect(jsonPath("$.entries.[1].id", is(catchmentEvents.get(1).get("eventId").toString())))
                .andExpect(jsonPath("$.entries.[2].id", is(catchmentEvents.get(2).get("eventId").toString())));

        verify(patientService).findAllByCatchment(catchment, parseDate(since), null);
    }

    @Test
    public void shouldFindPatientByCatchmentWithSinceAndLastMarkerQueryParams() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);
        String since = "2010-01-01T10:20:30Z";
        UUID lastMarker = TimeUuidUtil.uuidForDate(new Date());

        List<Map<String, Object>> catchmentEvents = asList(buildCatchmentEvent("h100"), buildCatchmentEvent("h200"), buildCatchmentEvent("h300"));
        when(patientService.findAllByCatchment(catchment, parseDate(since), lastMarker)).thenReturn(catchmentEvents);

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = fromUriString(format("%s/%s/%s/patients", REQUEST_URL, API_END_POINT, catchmentId))
                .queryParam(SINCE, since)
                .queryParam(LAST_MARKER, lastMarker)
                .build().toString();

        String feedUrl = fromUriString(format("%s/%s/%s/patients", REQUEST_URL, API_END_POINT, catchmentId))
                .queryParam(SINCE, since)
                .queryParam(LAST_MARKER, lastMarker)
                .build().toString();

        String nextUrl = fromUriString(format("%s/%s/%s/patients", REQUEST_URL, API_END_POINT, catchmentId))
                .queryParam(SINCE, encode(convertToDateStringIsoMillisFormat((UUID) catchmentEvents.get(2).get("eventId")), "UTF-8"))
                .queryParam(LAST_MARKER, encode(catchmentEvents.get(2).get("eventId").toString(), "UTF-8")).build().toString();

        MvcResult mvcResult = mockMvc.perform(get(requestUrl).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();
        

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(feedUrl)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nextUrl)))

                .andExpect(jsonPath("$.entries.[0].id", is(catchmentEvents.get(0).get("eventId").toString())))
                .andExpect(jsonPath("$.entries.[1].id", is(catchmentEvents.get(1).get("eventId").toString())))
                .andExpect(jsonPath("$.entries.[2].id", is(catchmentEvents.get(2).get("eventId").toString())));

        verify(patientService).findAllByCatchment(catchment, parseDate(since), lastMarker);
    }

    @Test
    public void shouldBuildFeedResponse() throws Exception {

        Map<String, Object> catchmentEvent1 = buildCatchmentEvent("h100");
        Map<String, Object> catchmentEvent2 = buildCatchmentEvent("h200");
        Map<String, Object> catchmentEvent3 = buildCatchmentEvent("h300");
        List<Map<String, Object>> catchmentEvents = asList(catchmentEvent1, catchmentEvent2, catchmentEvent3);
        MockHttpServletRequest request = buildCatchmentHttpRequest(null, null);

        String title = "Patients";
        Feed feed = catchmentController.buildFeedResponse(catchmentEvents, request);

        assertEquals("MCI", feed.getAuthor());
        assertEquals(title, feed.getTitle());

        String requestUrl = this.catchmentController.buildUrl(request);
        assertEquals(requestUrl, feed.getFeedUrl());

        assertNull(feed.getPrevUrl());

        String nextUrl = feed.getNextUrl();
        assertNotNull(nextUrl);
        assertTrue(nextUrl.startsWith(requestUrl));

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(nextUrl), "UTF-8");
        assertEquals(2, params.size());
        assertEquals(SINCE, params.get(0).getName());
        UUID eventId = (UUID) catchmentEvent3.get("eventId");
        assertEquals(convertToDateStringIsoMillisFormat(eventId), params.get(0).getValue());
        assertEquals(LAST_MARKER, params.get(1).getName());
        assertEquals(eventId.toString(), params.get(1).getValue());

        List<FeedEntry> entries = feed.getEntries();
        assertNotNull(entries);
        assertEquals(catchmentEvents.size(), entries.size());

        assertFeedEntry(entries.get(0), catchmentEvent1);
        assertFeedEntry(entries.get(1), catchmentEvent2);
        assertFeedEntry(entries.get(2), catchmentEvent3);
    }

    @Test
    public void shouldBuildFeedResponseWithQueryParam() throws Exception {
        Map<String, Object> catchmentEvent1 = buildCatchmentEvent("h100");
        Map<String, Object> catchmentEvent2 = buildCatchmentEvent("h200");
        Map<String, Object> catchmentEvent3 = buildCatchmentEvent("h300");
        List<Map<String, Object>> catchmentEvents = asList(catchmentEvent1, catchmentEvent2, catchmentEvent3);
        MockHttpServletRequest request = buildCatchmentHttpRequest("2010-01-01T10:20:30Z", "h000");

        String title = "Patients";
        Feed feed = catchmentController.buildFeedResponse(catchmentEvents, request);

        assertEquals("MCI", feed.getAuthor());
        assertEquals(title, feed.getTitle());

        String requestUrl = this.catchmentController.buildUrl(request);
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
        assertEquals(convertToDateStringIsoMillisFormat((UUID) catchmentEvent3.get("eventId")), params.get(0).getValue());
        assertEquals(LAST_MARKER, params.get(1).getName());
        assertEquals(catchmentEvent3.get("eventId").toString(), params.get(1).getValue());

        List<FeedEntry> entries = feed.getEntries();
        assertNotNull(entries);
        assertEquals(catchmentEvents.size(), entries.size());

        assertFeedEntry(entries.get(0), catchmentEvent1);
        assertFeedEntry(entries.get(1), catchmentEvent2);
        assertFeedEntry(entries.get(2), catchmentEvent3);
    }

    private PatientData buildPatient(String healthId) throws InterruptedException {
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);
        patient.setUpdatedAt(TimeUuidUtil.uuidForDate(new Date()));
        return patient;
    }

    private Map<String, Object> buildCatchmentEvent(String healthId) throws InterruptedException {
        Map<String, Object> catchmentEvent = new HashMap<>();
        PatientData patientData = buildPatient(healthId);
        catchmentEvent.put("eventId", patientData.getUpdatedAt());
        catchmentEvent.put("patientData", patientData);
        return catchmentEvent;
    }

    private MockHttpServletRequest buildCatchmentHttpRequest(String since, String lastMarker) throws UnsupportedEncodingException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("mci.dghs.com");
        request.setServerPort(8081);
        request.setMethod("GET");
        request.setRequestURI("/catchments/102030/patients");

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

    private void assertFeedEntry(FeedEntry entry, Map<String, Object> catchmentEntry) {
        assertNotNull(entry);
        PatientData patientData = (PatientData) catchmentEntry.get("patientData");
        String healthId = patientData.getHealthId();
        assertEquals(catchmentEntry.get("eventId"), entry.getId());
        assertEquals(convertToDateStringIsoMillisFormat((UUID) catchmentEntry.get("eventId")), entry.getPublishedDate());
        assertEquals("Patient in Catchment: " + healthId, entry.getTitle());
        assertEquals(REQUEST_URL + "/patients/" + healthId, entry.getLink());
        assertNotNull(entry.getCategories());
        assertEquals(1, entry.getCategories().length);
        assertEquals("patient", entry.getCategories()[0]);
        assertEquals(catchmentEntry.get("patientData"), entry.getContent());
    }

    @Test
    public void shouldBuildPendingApprovalNextUrl() throws Exception {
        MockHttpServletRequest httpRequest = buildPendingApprovalHttpRequest();
        List<PendingApprovalListResponse> approvalListResponse = asList(buildPendingApprovalListResponse(1),
                buildPendingApprovalListResponse(2), buildPendingApprovalListResponse(3), buildPendingApprovalListResponse(4));

        MCIMultiResponse response = catchmentController.buildPaginatedResponse(httpRequest, approvalListResponse, null, null, 3);

        assertEquals(response.getHttpStatus(), 200);
        HashMap additionalInfo = response.getAdditionalInfo();
        assertTrue(additionalInfo != null && additionalInfo.size() == 1);
        assertEquals(REQUEST_URL + "/catchments/201915/approvals?after=" + approvalListResponse.get(2).getLastUpdated(),
                additionalInfo.get(NEXT));
    }

    @Test
    public void shouldBuildPendingApprovalPreviousUrl() throws Exception {
        MockHttpServletRequest httpRequest = buildPendingApprovalHttpRequest();
        List<PendingApprovalListResponse> approvalListResponse = asList(buildPendingApprovalListResponse(1),
                buildPendingApprovalListResponse(2));

        MCIMultiResponse response = catchmentController.buildPaginatedResponse(httpRequest, approvalListResponse,
                approvalListResponse.get(1).getLastUpdated(), null, 3);

        assertEquals(response.getHttpStatus(), 200);
        HashMap additionalInfo = response.getAdditionalInfo();
        assertTrue(additionalInfo != null && additionalInfo.size() == 1);
        assertEquals(REQUEST_URL + "/catchments/201915/approvals?before=" + approvalListResponse.get(0).getLastUpdated(),
                additionalInfo.get(PREVIOUS));
    }

    @Test
    public void shouldBuildPendingApprovalNextAndPreviousUrl() throws Exception {
        MockHttpServletRequest httpRequest = buildPendingApprovalHttpRequest();
        List<PendingApprovalListResponse> approvalListResponse = asList(buildPendingApprovalListResponse(1),
                buildPendingApprovalListResponse(2), buildPendingApprovalListResponse(3), buildPendingApprovalListResponse(4));

        MCIMultiResponse response = catchmentController.buildPaginatedResponse(httpRequest, approvalListResponse,
                approvalListResponse.get(2).getLastUpdated(), null, 3);

        assertEquals(response.getHttpStatus(), 200);
        HashMap additionalInfo = response.getAdditionalInfo();
        assertTrue(additionalInfo != null && additionalInfo.size() == 2);
        assertEquals(REQUEST_URL + "/catchments/201915/approvals?before=" + approvalListResponse.get(0).getLastUpdated(),
                additionalInfo.get(PREVIOUS));
        assertEquals(REQUEST_URL + "/catchments/201915/approvals?after=" + approvalListResponse.get(2).getLastUpdated(),
                additionalInfo.get(NEXT));
    }

    @Test
    public void shouldNotBuildNextUrl() throws Exception {
        MockHttpServletRequest httpRequest = buildPendingApprovalHttpRequest();
        List<PendingApprovalListResponse> approvalListResponse = asList(buildPendingApprovalListResponse(1),
                buildPendingApprovalListResponse(2), buildPendingApprovalListResponse(3));

        MCIMultiResponse response = catchmentController.buildPaginatedResponse(httpRequest, approvalListResponse, null, null, 3);

        assertEquals(response.getHttpStatus(), 200);
        HashMap additionalInfo = response.getAdditionalInfo();
        assertTrue(additionalInfo == null || additionalInfo.size() == 0);
    }

    @Test
    public void shouldNotBuildPreviousUrl() throws Exception {
        MockHttpServletRequest httpRequest = buildPendingApprovalHttpRequest();
        List<PendingApprovalListResponse> approvalListResponse = asList(buildPendingApprovalListResponse(1),
                buildPendingApprovalListResponse(2), buildPendingApprovalListResponse(3));

        MCIMultiResponse response = catchmentController.buildPaginatedResponse(httpRequest, approvalListResponse, null, approvalListResponse.get(0).getLastUpdated(), 3);

        assertEquals(response.getHttpStatus(), 200);
        HashMap additionalInfo = response.getAdditionalInfo();
        assertTrue(additionalInfo != null && additionalInfo.size() == 1);
        assertEquals(REQUEST_URL + "/catchments/201915/approvals?after=" + approvalListResponse.get(2).getLastUpdated(),
                additionalInfo.get(NEXT));
    }

    @Test
    public void shouldNotBuildNextAndPreviousUrlIFAfterAndBeforeGiven() throws Exception {
        MockHttpServletRequest httpRequest = buildPendingApprovalHttpRequest();
        List<PendingApprovalListResponse> approvalListResponse = asList(buildPendingApprovalListResponse(1),
                buildPendingApprovalListResponse(2), buildPendingApprovalListResponse(3));

        MCIMultiResponse response = catchmentController.buildPaginatedResponse(httpRequest,
                approvalListResponse, approvalListResponse.get(0).getLastUpdated(), approvalListResponse.get(2).getLastUpdated(), 3);

        assertEquals(response.getHttpStatus(), 200);
        HashMap additionalInfo = response.getAdditionalInfo();
        assertTrue(additionalInfo == null || additionalInfo.size() == 0);
    }

    @Test
    public void shouldBuildPendingApprovalNextUrlIFBeforeGiven() throws Exception {
        MockHttpServletRequest httpRequest = buildPendingApprovalHttpRequest();
        List<PendingApprovalListResponse> approvalListResponse = asList(buildPendingApprovalListResponse(1),
                buildPendingApprovalListResponse(2), buildPendingApprovalListResponse(3));

        MCIMultiResponse response = catchmentController.buildPaginatedResponse(httpRequest, approvalListResponse, null, approvalListResponse.get(2).getLastUpdated(), 3);

        assertEquals(response.getHttpStatus(), 200);
        HashMap additionalInfo = response.getAdditionalInfo();
        assertTrue(additionalInfo != null && additionalInfo.size() == 1);
        assertEquals(REQUEST_URL + "/catchments/201915/approvals?after=" + approvalListResponse.get(2).getLastUpdated(),
                additionalInfo.get(NEXT));
    }

    @Test
    public void shouldAddRequestedByWhenPatientIsApproved() throws Exception {
        String healthId = "health-100";
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);

        String catchmentId = "102030";
        Catchment catchment = new Catchment(catchmentId);
        when(patientService.processPendingApprovals(patient, catchment, true)).thenReturn(healthId);

        String url = buildPendingApprovalUrl(catchmentId, healthId);
        Map<String, String> content = new HashMap<>();
        content.put(HID, healthId);
        content.put(JsonConstants.PROVIDER, "Dr. Monika");
        MvcResult mvcResult = mockMvc.perform(put(url).content(writeValueAsString(content)).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(healthId)));

        verify(patientService).processPendingApprovals(patient, catchment, true);

        ArgumentCaptor<PatientData> argument1 = ArgumentCaptor.forClass(PatientData.class);
        ArgumentCaptor<Catchment> argument2 = ArgumentCaptor.forClass(Catchment.class);
        ArgumentCaptor<Boolean> argument3 = ArgumentCaptor.forClass(Boolean.class);
        verify(patientService).processPendingApprovals(argument1.capture(), argument2.capture(), argument3.capture());

        assertRequester(argument1.getValue().getRequester(), FACILITY_ID, PROVIDER_ID);
    }

    @Test
    public void shouldAddRequestedByWhenPatientIsRejected() throws Exception {
        String healthId = "health-100";
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);

        String catchmentId = "102030";
        Catchment catchment = new Catchment(catchmentId);
        when(patientService.processPendingApprovals(patient, catchment, false)).thenReturn(healthId);

        String url = buildPendingApprovalUrl(catchmentId, healthId);
        Map<String, String> content = new HashMap<>();
        content.put(HID, healthId);
        content.put(JsonConstants.PROVIDER, "Dr. Monika");
        MvcResult mvcResult = mockMvc.perform(delete(url).content(writeValueAsString(content)).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(healthId)));

        verify(patientService).processPendingApprovals(patient, catchment, false);

        ArgumentCaptor<PatientData> argument1 = ArgumentCaptor.forClass(PatientData.class);
        ArgumentCaptor<Catchment> argument2 = ArgumentCaptor.forClass(Catchment.class);
        ArgumentCaptor<Boolean> argument3 = ArgumentCaptor.forClass(Boolean.class);
        verify(patientService).processPendingApprovals(argument1.capture(), argument2.capture(), argument3.capture());

        assertRequester(argument1.getValue().getRequester(), FACILITY_ID, PROVIDER_ID);
    }

    private PendingApprovalListResponse buildPendingApprovalListResponse(int suffix) throws InterruptedException {
        PendingApprovalListResponse pendingApproval = new PendingApprovalListResponse();
        pendingApproval.setHealthId("hid-" + suffix);
        pendingApproval.setGivenName("Scott-" + suffix);
        pendingApproval.setSurname("Tiger-" + suffix);
        pendingApproval.setLastUpdated(TimeUuidUtil.uuidForDate(new Date()));
        return pendingApproval;
    }

    private MockHttpServletRequest buildPendingApprovalHttpRequest() throws UnsupportedEncodingException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("mci.dghs.com");
        request.setServerPort(8081);
        request.setMethod("GET");
        request.setRequestURI("/catchments/201915/approvals");
        return request;
    }

    private void assertRequester(Requester requester, String facilityId, String providerId) {
        assertNotNull(requester);
        assertNotNull(requester.getFacility());
        assertEquals(facilityId, requester.getFacility().getId());
        assertNotNull(requester.getProvider());
        assertEquals(providerId, requester.getProvider().getId());
    }
}