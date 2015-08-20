package org.sharedhealth.mci.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.cassandraunit.spring.CassandraUnit;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.sharedhealth.mci.domain.exception.Forbidden;
import org.sharedhealth.mci.domain.model.MCIResponse;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.PatientSummaryData;
import org.sharedhealth.mci.domain.model.Relation;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.sharedhealth.mci.domain.config.EnvironmentMock;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.domain.repository.TestUtil;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.Date;
import java.util.List;

import static org.sharedhealth.mci.utils.HttpUtil.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
@TestPropertySource(properties = {"HEALTH_ID_REPLENISH_INITIAL_DELAY = 0",
        "HEALTH_ID_REPLENISH_DELAY = 1",
        "HEALTH_ID_BLOCK_SIZE = 1",
        "HEALTH_ID_BLOCK_SIZE_THRESHOLD=1"})
@CassandraUnit
public class BaseControllerTest {
    protected static final String FACILITY_ID = "1111111";
    protected static final String PROVIDER_ID = "222222";
    protected static final String ADMIN_ID = "333333";


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);
    @Autowired
    private HealthIdRepository healthIdRepository;
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
    public static final String API_END_POINT_FOR_PATIENT = "/patients";
    public static final String API_END_POINT_FOR_MERGE_REQUEST = "/mergerequest";
    public static final String API_END_POINT_FOR_LOCATION = "/locations";
    public static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";

    protected final String facilityClientId = "18548";
    protected final String facilityEmail = "facility@gmail.com";
    protected final String facilityAccessToken = "40214a6c-e27c-4223-981c-1f837be90f02";

    protected final String mciApproverClientId = "18555";
    protected final String mciApproverEmail = "mciapprover@gmail.com";
    protected final String mciApproverAccessToken = "40214a6c-e27c-4223-981c-1f837be90f06";


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
        if (null == patient.getHealthId())
            patient.setHealthId(String.valueOf(new Date().getTime()));
        return patientRepository.create(patient);
    }

    protected MCIResponse createPatient(String json) throws Exception {
        PatientData data = getPatientObjectFromString(json);
        data.setRequester(FACILITY_ID, null);
        if (null == data.getHealthId())
            data.setHealthId(String.valueOf(new Date().getTime()));
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
        original.setHealthId(patient.getHealthId());
        Assert.assertEquals(original, patient);
    }

    protected PatientData getPatientObjectFromResponse(ResponseEntity asyncResult) throws Exception {
        return getPatientObjectFromString(mapper.writeValueAsString(asyncResult.getBody()));
    }

    @Before
    public void setupBase() throws Exception {
        healthIdRepository.resetLastReservedHealthId();
        createHealthIds();
    }

    private void createHealthIds() {
        for (int i = 0; i < numberOfHealthIdsNeeded(); i++) {
            healthIdRepository.saveHealthIdSync(new MciHealthId(String.valueOf(new Date().getTime() + i)));
        }
    }

    protected int numberOfHealthIdsNeeded() {
        return 10;
    }

    @After
    public void teardownBase() {
        healthIdRepository.resetLastReservedHealthId();
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
