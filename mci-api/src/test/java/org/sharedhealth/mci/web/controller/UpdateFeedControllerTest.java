package org.sharedhealth.mci.web.controller;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.utils.DateUtil;
import org.sharedhealth.mci.web.handler.FeedMessageConverter;
import org.sharedhealth.mci.web.mapper.Feed;
import org.sharedhealth.mci.web.mapper.FeedEntry;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.sharedhealth.mci.web.service.PatientService;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
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
import static org.sharedhealth.mci.utils.DateUtil.parseDate;
import static org.sharedhealth.mci.web.utils.JsonConstants.LAST_MARKER;
import static org.sharedhealth.mci.web.utils.JsonConstants.SINCE;
import static org.springframework.http.MediaType.APPLICATION_ATOM_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

public class UpdateFeedControllerTest {

    private static final String API_END_POINT = "api/v1/feed";
    @Mock
    private PatientService patientService;
    @Mock
    private LocalValidatorFactoryBean validatorFactory;
    private MockMvc mockMvc;

    @Before
    public void setup() throws ParseException {
        initMocks(this);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new UpdateFeedController(patientService))
                .setMessageConverters(new FeedMessageConverter(), new MappingJackson2HttpMessageConverter())

                .setValidator(validatorFactory)
                .build();
    }

    @Test
    public void shouldFindPatientUpdatedSince() throws Exception {
        UUID uuid1 = timeBased();
        UUID uuid2 = timeBased();
        UUID uuid3 = timeBased();

        Date startDate = parseDate("2010-01-01T10:20:30Z");

        when(patientService.findPatientsUpdatedSince(startDate, null)).thenReturn(
                asList(buildPatientLog("h100", uuid1),
                        buildPatientLog("h200", uuid2),
                        buildPatientLog("h300", uuid3)));


        String url = format("http://localhost/%s/patients", API_END_POINT);

        String nextUrl = fromUriString(url)
                .queryParam(LAST_MARKER, encode(uuid3.toString(), "UTF-8")).build().toString();

        url = url + "?since=2010-01-01T10:20:30Z";

        mockMvc.perform(get(url).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(url)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nextUrl)))

                .andExpect(jsonPath("$.entries.[0].id", is(uuid1.toString())))
                .andExpect(jsonPath("$.entries.[0].publishedDate", is(DateUtil.toIsoFormat(uuid1))))
                .andExpect(jsonPath("$.entries.[0].title", is("Patient updates: h100")))
                .andExpect(jsonPath("$.entries.[0].link", is("http://localhost:80/api/v1/patients/h100")))
                .andExpect(jsonPath("$.entries.[0].categories[0]", is("patient")))
                .andExpect(jsonPath("$.entries.[0].content.health_id", is("h100")))
                .andExpect(jsonPath("$.entries.[0].content.change_set.sur_name", is("updated")))
                .andExpect(jsonPath("$.entries.[1].id", is(uuid2.toString())))
                .andExpect(jsonPath("$.entries.[2].id", is(uuid3.toString())));

        verify(patientService).findPatientsUpdatedSince(startDate, null);
    }

    @Test
    public void shouldFindPatientUpdatedAfterLastMarker() throws Exception {
        UUID uuid1 = timeBased();
        UUID uuid2 = timeBased();
        UUID uuid3 = timeBased();

        when(patientService.findPatientsUpdatedSince(null, uuid1)).thenReturn(
                asList(buildPatientLog("h200", uuid2),
                        buildPatientLog("h300", uuid3)));


        String url = format("http://localhost/%s/patients", API_END_POINT);

        String nextUrl = fromUriString(url)
                .queryParam(LAST_MARKER, encode(uuid3.toString(), "UTF-8")).build().toString();

        url = url + "?" + LAST_MARKER + "=" + uuid1.toString();

        mockMvc.perform(get(url).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(url)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nextUrl)))

                .andExpect(jsonPath("$.entries.[0].id", is(uuid2.toString())))
                .andExpect(jsonPath("$.entries.[0].publishedDate", is(DateUtil.toIsoFormat(uuid2))))
                .andExpect(jsonPath("$.entries.[0].title", is("Patient updates: h200")))
                .andExpect(jsonPath("$.entries.[0].link", is("http://localhost:80/api/v1/patients/h200")))
                .andExpect(jsonPath("$.entries.[0].categories[0]", is("patient")))
                .andExpect(jsonPath("$.entries.[0].content.health_id", is("h200")))
                .andExpect(jsonPath("$.entries.[0].content.change_set.sur_name", is("updated")))
                .andExpect(jsonPath("$.entries.[1].id", is(uuid3.toString())));

        verify(patientService).findPatientsUpdatedSince(null, uuid1);
    }

    @Test
    public void shouldReturnEmptyListIfNoPatientFound() throws Exception {
        Date startDate = parseDate("2010-01-01T10:20:30Z");

        when(patientService.findPatientsUpdatedSince(startDate, null)).thenReturn(null);

        String url = format("http://localhost/%s/patients?since=2010-01-01T10:20:30Z", API_END_POINT);

        mockMvc.perform(get(url).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(url)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nullValue())))
                .andExpect(jsonPath("$.entries", is(emptyList())));

        verify(patientService).findPatientsUpdatedSince(startDate, null);
    }

    @Test
    public void shouldWorkForInvalidLastMarker() throws Exception {

        when(patientService.findPatientsUpdatedSince(null, null)).thenReturn(null);

        String url = format("http://localhost/%s/patients?" + LAST_MARKER + "=123", API_END_POINT);

        mockMvc.perform(get(url).accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(url)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))
                .andExpect(jsonPath("$.nextUrl", is(nullValue())))
                .andExpect(jsonPath("$.entries", is(emptyList())));

        verify(patientService).findPatientsUpdatedSince(null, null);
    }

    @Test
    public void shouldBuildFeedResponseWithQueryParam() throws Exception {
        UUID uuid1 = timeBased();
        UUID uuid2 = timeBased();
        UUID uuid3 = timeBased();

        final String dateString = "2010-01-01T10:20:30Z";

        List<PatientUpdateLog> patients = asList(buildPatientLog("h100", uuid1),
                buildPatientLog("h200", uuid2),
                buildPatientLog("h300", uuid3));

        MockHttpServletRequest request = buildHttpRequest(dateString, uuid1.toString());

        String title = "Patients";
        Feed feed = new UpdateFeedController(null).buildFeedResponse(patients, request);

        assertEquals("MCI", feed.getAuthor());
        assertEquals(title, feed.getTitle());

        String requestUrl = request.getRequestURL().toString();
        String feedUrl = fromUriString(requestUrl)
                .queryParam(SINCE, encode(dateString, "UTF-8"))
                .queryParam(LAST_MARKER, encode(uuid1.toString(), "UTF-8")).build().toString();
        assertEquals(feedUrl, feed.getFeedUrl());

        assertNull(feed.getPrevUrl());

        String nextUrl = feed.getNextUrl();
        assertNotNull(nextUrl);
        assertTrue(nextUrl.startsWith(requestUrl));

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(nextUrl), "UTF-8");
        assertEquals(1, params.size());
        assertEquals(LAST_MARKER, params.get(0).getName());
        assertEquals(uuid3.toString(), params.get(0).getValue());

        List<FeedEntry> entries = feed.getEntries();
        assertNotNull(entries);
        assertEquals(patients.size(), entries.size());

        assertFeedEntry(entries.get(0), patients.get(0));
        assertFeedEntry(entries.get(1), patients.get(1));
        assertFeedEntry(entries.get(2), patients.get(2));
    }

    @Test
    public void shouldCreateCategoryArrayWithOnlyPatientIfChangeSetIsNull() throws Exception {
        UUID uuid1 = timeBased();
        UUID uuid2 = timeBased();

        when(patientService.findPatientsUpdatedSince(null, null)).thenReturn(
                asList(buildPatientLog("h100", uuid1, null), buildPatientLog("h200", uuid2))
        );

        String url = format("http://localhost/%s/patients", API_END_POINT);

        mockMvc.perform(get(url).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("MCI")))
                .andExpect(jsonPath("$.title", is("Patients")))
                .andExpect(jsonPath("$.feedUrl", is(url)))
                .andExpect(jsonPath("$.prevUrl", is(nullValue())))

                .andExpect(jsonPath("$.entries.[0].id", is(uuid1.toString())))
                .andExpect(jsonPath("$.entries.[0].publishedDate", is(DateUtil.toIsoFormat(uuid1))))
                .andExpect(jsonPath("$.entries.[0].title", is("Patient updates: h100")))
                .andExpect(jsonPath("$.entries.[0].link", is("http://localhost:80/api/v1/patients/h100")))
                .andExpect(jsonPath("$.entries.[0].categories", is(asList("patient"))))
                .andExpect(jsonPath("$.entries.[0].content.health_id", is("h100")))
                .andExpect(jsonPath("$.entries.[0].content.change_set", is(nullValue())))
                .andExpect(jsonPath("$.entries.[1].categories", is(asList("patient", "update:sur_name"))))
        ;

        verify(patientService).findPatientsUpdatedSince(null, null);
    }

    private PatientUpdateLog buildPatientLog(String healthId, UUID eventId, String changeSet) {
        PatientUpdateLog patient = new PatientUpdateLog();
        patient.setHealthId(healthId);
        patient.setEventId(eventId);
        patient.setChangeSet(changeSet);
        return patient;
    }

    private PatientUpdateLog buildPatientLog(String healthId, UUID eventId) {
        return buildPatientLog(healthId, eventId, "{\"sur_name\":\"updated\"}");
    }

    private MockHttpServletRequest buildHttpRequest(String since, String lastMarker) throws
            UnsupportedEncodingException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("www.mci.com");
        request.setServerPort(8081);
        request.setMethod("GET");
        request.setRequestURI("/api/v1/feed/patients");

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

    private void assertFeedEntry(FeedEntry entry, PatientUpdateLog patient) {
        assertNotNull(entry);
        String healthId = patient.getHealthId();
        assertEquals(patient.getEventId(), entry.getId());
        assertEquals(patient.getEventTimeAsString(), entry.getPublishedDate());
        assertEquals("Patient updates: " + healthId, entry.getTitle());
        assertEquals("http://www.mci.com:8081/api/v1/patients/" + healthId, entry.getLink());
        assertNotNull(entry.getCategories());
        assertEquals(2, entry.getCategories().length);
        assertEquals("patient", entry.getCategories()[0]);
        assertEquals("update:sur_name", entry.getCategories()[1]);
        assertEquals(patient, entry.getContent());
    }

    @Test
    public void shouldGiveFeedInAtomXMLFormat() throws Exception {
        UUID uuid1 = timeBased();
        UUID uuid2 = timeBased();
        List<PatientUpdateLog> patients = asList(buildPatientLog("h100", uuid1),
                buildPatientLog("h200", uuid2));

        when(patientService.findPatientsUpdatedSince(null, null)).thenReturn(patients);

        String url = format("http://localhost/%s/patients", API_END_POINT);

        mockMvc.perform(get(url).accept(APPLICATION_ATOM_XML_VALUE))
                .andExpect(status().isOk())
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        System.out.println(result);
                    }
                });

        verify(patientService).findPatientsUpdatedSince(null, null);
    }

}