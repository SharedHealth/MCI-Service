package org.sharedhealth.mci.web.controller;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.Feed;
import org.sharedhealth.mci.web.mapper.FeedEntry;
import org.sharedhealth.mci.web.mapper.PatientData;
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
import java.util.List;

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
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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
    public void shouldFindPatientByCatchment() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);

        when(patientService.findAllByCatchment(catchment, null, facilityId)).thenReturn(
                asList(buildPatient("h100", "2010-01-01T10:20:30Z"),
                        buildPatient("h200", "2015-01-01T10:20:30Z"),
                        buildPatient("h300", "2020-01-01T10:20:30Z")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String url = format("http://localhost/%s/%s/patients", API_END_POINT, catchmentId);

        String nextUrl = fromUriString(url)
                .queryParam(AFTER, encode("2020-01-01T10:20:30Z", "UTF-8"))
                .queryParam(LAST_MARKER, encode("h300", "UTF-8")).build().toString();


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

                .andExpect(jsonPath("$.entries.[0].id", is("h100")))
                .andExpect(jsonPath("$.entries.[0].publishedDate", is("2010-01-01T10:20:30Z")))
                .andExpect(jsonPath("$.entries.[0].title", is("Patient in Catchment: h100")))
                .andExpect(jsonPath("$.entries.[0].link", is("http://localhost:80/api/v1/patients/h100")))
                .andExpect(jsonPath("$.entries.[0].categories[0]", is("patient")))
                .andExpect(jsonPath("$.entries.[0].content.hid", is("h100")))

                .andExpect(jsonPath("$.entries.[1].id", is("h200")))
                .andExpect(jsonPath("$.entries.[2].id", is("h300")));

        verify(patientService).findAllByCatchment(catchment, null, facilityId);
    }

    @Test
    public void shouldReturnEmptyListIfNoPatientFound() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);

        when(patientService.findAllByCatchment(catchment, null, facilityId)).thenReturn(null);

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

        verify(patientService).findAllByCatchment(catchment, null, facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentUpdatedAfterADate() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);
        String after = "2015-01-01T10:20:30Z";

        when(patientService.findAllByCatchment(catchment, fromIsoFormat(after), facilityId)).thenReturn(
                asList(buildPatient("h100", "2010-01-01T10:20:30Z"),
                        buildPatient("h200", "2015-01-01T10:20:30Z"),
                        buildPatient("h300", "2020-01-01T10:20:30Z")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = format("http://localhost/%s/%s/patients", API_END_POINT, catchmentId);

        String url = fromUriString(requestUrl)
                .queryParam(AFTER, after)
                .build().toString();

        String nextUrl = fromUriString(requestUrl)
                .queryParam(AFTER, encode("2020-01-01T10:20:30Z", "UTF-8"))
                .queryParam(LAST_MARKER, encode("h300", "UTF-8")).build().toString();

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

                .andExpect(jsonPath("$.entries.[0].id", is("h100")))
                .andExpect(jsonPath("$.entries.[1].id", is("h200")))
                .andExpect(jsonPath("$.entries.[2].id", is("h300")));

        verify(patientService).findAllByCatchment(catchment, fromIsoFormat(after), facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentWithDivisionAndDistrictUpdatedAfterADate() throws Exception {
        String facilityId = "123456";
        String catchmentId = "1020";
        Catchment catchment = new Catchment(catchmentId);
        String after = "2015-01-01T10:20:30Z";

        when(patientService.findAllByCatchment(catchment, fromIsoFormat(after), facilityId)).thenReturn(
                asList(buildPatient("h100", "2010-01-01T10:20:30Z"),
                        buildPatient("h200", "2015-01-01T10:20:30Z"),
                        buildPatient("h300", "2020-01-01T10:20:30Z")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = format("http://localhost/%s/%s/patients", API_END_POINT, catchmentId);

        String url = fromUriString(requestUrl)
                .queryParam(AFTER, after)
                .build().toString();

        String nextUrl = fromUriString(requestUrl)
                .queryParam(AFTER, encode("2020-01-01T10:20:30Z", "UTF-8"))
                .queryParam(LAST_MARKER, encode("h300", "UTF-8")).build().toString();

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

                .andExpect(jsonPath("$.entries.[0].id", is("h100")))
                .andExpect(jsonPath("$.entries.[1].id", is("h200")))
                .andExpect(jsonPath("$.entries.[2].id", is("h300")));

        verify(patientService).findAllByCatchment(catchment, fromIsoFormat(after), facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentWithDivisionDistrictAndUpazilaUpdatedAfterADate() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030";
        Catchment catchment = new Catchment(catchmentId);
        String after = "2015-01-01T10:20:30Z";

        when(patientService.findAllByCatchment(catchment, fromIsoFormat(after), facilityId)).thenReturn(
                asList(buildPatient("h100", "2010-01-01T10:20:30Z"),
                        buildPatient("h200", "2015-01-01T10:20:30Z"),
                        buildPatient("h300", "2020-01-01T10:20:30Z")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = format("http://localhost/%s/%s/patients", API_END_POINT, catchmentId);

        String url = fromUriString(requestUrl)
                .queryParam(AFTER, after)
                .build().toString();

        String nextUrl = fromUriString(requestUrl)
                .queryParam(AFTER, encode("2020-01-01T10:20:30Z", "UTF-8"))
                .queryParam(LAST_MARKER, encode("h300", "UTF-8")).build().toString();

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

                .andExpect(jsonPath("$.entries.[0].id", is("h100")))
                .andExpect(jsonPath("$.entries.[1].id", is("h200")))
                .andExpect(jsonPath("$.entries.[2].id", is("h300")));

        verify(patientService).findAllByCatchment(catchment, fromIsoFormat(after), facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentWithDivisionDistrictUpazilaAndCityCorpUpdatedAfterADate() throws Exception {
        String facilityId = "123456";
        String catchmentId = "10203040";
        Catchment catchment = new Catchment(catchmentId);
        String after = "2015-01-01T10:20:30Z";

        when(patientService.findAllByCatchment(catchment, fromIsoFormat(after), facilityId)).thenReturn(
                asList(buildPatient("h100", "2010-01-01T10:20:30Z"),
                        buildPatient("h200", "2015-01-01T10:20:30Z"),
                        buildPatient("h300", "2020-01-01T10:20:30Z")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = format("http://localhost/%s/%s/patients", API_END_POINT, catchmentId);

        String url = fromUriString(requestUrl)
                .queryParam(AFTER, after)
                .build().toString();

        String nextUrl = fromUriString(requestUrl)
                .queryParam(AFTER, encode("2020-01-01T10:20:30Z", "UTF-8"))
                .queryParam(LAST_MARKER, encode("h300", "UTF-8")).build().toString();

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

                .andExpect(jsonPath("$.entries.[0].id", is("h100")))
                .andExpect(jsonPath("$.entries.[1].id", is("h200")))
                .andExpect(jsonPath("$.entries.[2].id", is("h300")));

        verify(patientService).findAllByCatchment(catchment, fromIsoFormat(after), facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentWithDivisionDistrictUpazilaCityCorpAndUnionUpdatedAfterADate() throws Exception {
        String facilityId = "123456";
        String catchmentId = "1020304050";
        Catchment catchment = new Catchment(catchmentId);
        String after = "2015-01-01T10:20:30Z";

        when(patientService.findAllByCatchment(catchment, fromIsoFormat(after), facilityId)).thenReturn(
                asList(buildPatient("h100", "2010-01-01T10:20:30Z"),
                        buildPatient("h200", "2015-01-01T10:20:30Z"),
                        buildPatient("h300", "2020-01-01T10:20:30Z")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);

        String requestUrl = format("http://localhost/%s/%s/patients", API_END_POINT, catchmentId);

        String url = fromUriString(requestUrl)
                .queryParam(AFTER, after)
                .build().toString();

        String nextUrl = fromUriString(requestUrl)
                .queryParam(AFTER, encode("2020-01-01T10:20:30Z", "UTF-8"))
                .queryParam(LAST_MARKER, encode("h300", "UTF-8")).build().toString();

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

                .andExpect(jsonPath("$.entries.[0].id", is("h100")))
                .andExpect(jsonPath("$.entries.[1].id", is("h200")))
                .andExpect(jsonPath("$.entries.[2].id", is("h300")));
    }

    @Test
    public void shouldBuildFeedResponse() throws Exception {
        List<PatientData> patients = asList(buildPatient("h100", "2010-01-01T10:20:30Z"),
                buildPatient("h200", "2015-01-01T10:20:30Z"),
                buildPatient("h300", "2020-01-01T10:20:30Z"));
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
        assertEquals(AFTER, params.get(0).getName());
        assertEquals("2020-01-01T10:20:30Z", params.get(0).getValue());
        assertEquals(LAST_MARKER, params.get(1).getName());
        assertEquals("h300", params.get(1).getValue());

        List<FeedEntry> entries = feed.getEntries();
        assertNotNull(entries);
        assertEquals(patients.size(), entries.size());

        assertFeedEntry(entries.get(0), patients.get(0));
        assertFeedEntry(entries.get(1), patients.get(1));
        assertFeedEntry(entries.get(2), patients.get(2));
    }

    @Test
    public void shouldBuildFeedResponseWithQueryParam() throws Exception {
        List<PatientData> patients = asList(buildPatient("h100", "2010-01-01T10:20:30Z"),
                buildPatient("h200", "2015-01-01T10:20:30Z"),
                buildPatient("h300", "2020-01-01T10:20:30Z"));
        MockHttpServletRequest request = buildHttpRequest("2010-01-01T10:20:30Z", "h000");

        String title = "Patients";
        Feed feed = new CatchmentController(null).buildFeedResponse(patients, request);

        assertEquals("MCI", feed.getAuthor());
        assertEquals(title, feed.getTitle());

        String requestUrl = request.getRequestURL().toString();
        String feedUrl = fromUriString(requestUrl)
                .queryParam(AFTER, encode("2010-01-01T10:20:30Z", "UTF-8"))
                .queryParam(LAST_MARKER, encode("h000", "UTF-8")).build().toString();
        assertEquals(feedUrl, feed.getFeedUrl());

        assertNull(feed.getPrevUrl());

        String nextUrl = feed.getNextUrl();
        assertNotNull(nextUrl);
        assertTrue(nextUrl.startsWith(requestUrl));

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(nextUrl), "UTF-8");
        assertEquals(2, params.size());
        assertEquals(AFTER, params.get(0).getName());
        assertEquals("2020-01-01T10:20:30Z", params.get(0).getValue());
        assertEquals(LAST_MARKER, params.get(1).getName());
        assertEquals("h300", params.get(1).getValue());

        List<FeedEntry> entries = feed.getEntries();
        assertNotNull(entries);
        assertEquals(patients.size(), entries.size());

        assertFeedEntry(entries.get(0), patients.get(0));
        assertFeedEntry(entries.get(1), patients.get(1));
        assertFeedEntry(entries.get(2), patients.get(2));
    }

    private PatientData buildPatient(String healthId, String updatedAt) {
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);
        patient.setUpdatedAtAsString(updatedAt);
        return patient;
    }

    private MockHttpServletRequest buildHttpRequest(String after, String lastMarker) throws UnsupportedEncodingException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("www.mci.com");
        request.setServerPort(8081);
        request.setMethod("GET");
        request.setRequestURI("/api/v1/catchments/102030/patients");

        StringBuilder queryString = new StringBuilder();
        if (isNotEmpty(after)) {
            queryString.append(AFTER).append("=").append(encode(after, "UTF-8"));
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
        assertEquals(healthId, entry.getId());
        assertEquals(patient.getUpdatedAtAsString(), entry.getPublishedDate());
        assertEquals("Patient in Catchment: " + healthId, entry.getTitle());
        assertEquals("http://www.mci.com:8081/api/v1/patients/" + healthId, entry.getLink());
        assertNotNull(entry.getCategories());
        assertEquals(1, entry.getCategories().length);
        assertEquals("patient", entry.getCategories()[0]);
        assertEquals(patient, entry.getContent());
    }
}