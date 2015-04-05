package org.sharedhealth.mci.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.sharedhealth.mci.web.exception.Forbidden;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.TestUtil;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PatientSummaryData;
import org.sharedhealth.mci.web.mapper.Relation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.List;

import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.FROM_KEY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


public class BaseControllerTest {
    protected static final String FACILITY_ID = "1111111";
    protected static final String PROVIDER_ID = "222222";
    protected static final String ADMIN_ID = "333333";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    @Qualifier("MCICassandraTemplate")
    protected CassandraOperations cassandraOps;

    @Autowired
    private Filter springSecurityFilterChain;

    protected String validClientId;
    protected String validEmail;
    protected String validAccessToken;
    protected MockMvc mockMvc;
    protected PatientData patientData;
    protected ObjectMapper mapper = new ObjectMapper();
    public static final String API_END_POINT_FOR_PATIENT = "/api/v1/patients";
    public static final String API_END_POINT_FOR_LOCATION = "/api/v1/locations";
    public static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";


    protected void setUpMockMvcBuilder() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    protected MvcResult postPatient(String json) throws Exception {
        return mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    protected MCIResponse createPatient(PatientData patient) throws Exception {
        return patientRepository.create(patient);
    }
    protected MCIResponse createPatient(String json) throws Exception {
        PatientData data = getPatientObjectFromString(json);
        data.setRequester(FACILITY_ID, null);
        return createPatient(data);
    }

    protected MCIResponse updatePatient(String json, String healthId) throws Exception {
        PatientData data = getPatientObjectFromString(json);
        data.setHealthId(healthId);
        data.setRequester(FACILITY_ID, null);
        return patientRepository.update(data, data.getHealthId());
    }

    protected MCIMultiResponse getMciMultiResponse(MvcResult result) {
        final ResponseEntity asyncResult = (ResponseEntity<MCIMultiResponse>) result.getAsyncResult();

        return (MCIMultiResponse) asyncResult.getBody();
    }

    protected MCIResponse getMciResponse(MvcResult result) {
        final ResponseEntity asyncResult = (ResponseEntity<MCIResponse>) result.getAsyncResult();

        return (MCIResponse) asyncResult.getBody();
    }

    protected PatientData getPatientObjectFromString(String json) throws Exception {
        return mapper.readValue(json, PatientData.class);
    }

    protected PatientSummaryData getPatientSummaryObjectFromString(String json) throws Exception {
        return mapper.readValue(json, PatientSummaryData.class);
    }

    protected boolean isRelationsEqual(List<Relation> original, List<Relation> patient) {
        return original.containsAll(patient) && patient.containsAll(original);
    }

    protected void assertPatientEquals(PatientData original, PatientData patient) {
        synchronizeAutoGeneratedFields(original, patient);
        Assert.assertEquals(original, patient);
    }

    protected void synchronizeAutoGeneratedFields(PatientData original, PatientData patient) {
        original.setHealthId(patient.getHealthId());
        original.setCreatedAt(patient.getCreatedAt());
        original.setUpdatedAt(patient.getUpdatedAt());
    }

    protected PatientData getPatientObjectFromResponse(ResponseEntity asyncResult) throws Exception {
        return getPatientObjectFromString(mapper.writeValueAsString((PatientData) asyncResult.getBody()));
    }

    @After
    public void teardown() {
        TestUtil.truncateAllColumnFamilies(cassandraOps);
    }

    protected PatientData getPatientMapperObjectByHealthId(String healthId) throws Exception {
        MvcResult getResult = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        final ResponseEntity asyncResult = (ResponseEntity<PatientData>) getResult.getAsyncResult();
        return getPatientObjectFromResponse(asyncResult);
    }

    protected PatientData getPatientData(String healthId) throws Exception {
        return patientRepository.findByHealthId(healthId);
    }

    protected BaseMatcher<Object> isForbidden() {
        return new BaseMatcher<Object>() {

            @Override
            public boolean matches(Object item) {
                return item instanceof Forbidden;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue("Forbidden");
            }
        };
    }
}
