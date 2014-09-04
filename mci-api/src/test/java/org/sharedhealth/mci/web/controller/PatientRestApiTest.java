package org.sharedhealth.mci.web.controller;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfig;
import org.sharedhealth.mci.web.exception.GlobalExceptionHandler.ErrorInfo;
import org.sharedhealth.mci.web.model.Address;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientRestApiTest {
    private static final Logger logger = LoggerFactory.getLogger(Patient.class);
    private static final String LOCATION_INSERT_QUERY = "INSERT INTO locations (geo_code, division_id, district_id, upazilla_id, pourashava_id, union_id)" +
            " values ('%s', '%s', '%s', '%s', '%s', '%s')";
    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Mock
    private LocationService locationService;

    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cqlTemplate;

    private MockMvc mockMvc;
    private Patient patient;
    public static final String API_END_POINT = "/api/v1/patients";
    public static final String GEO_CODE = "1004092001";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        patient = new Patient();
        patient.setFirstName("Scott");
        patient.setLastName("Tiger");
        patient.setGender("1");
        patient.setDateOfBirth("2014-12-01");
        patient.setEducationLevel("01");
        patient.setOccupation("02");


        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazillaId("09");
        address.setCityCorporation("20");
        address.setVillage("10");
        address.setWard("01");
        address.setCountry("103");

        patient.setAddress(address);

        createLocation();
    }

    @Test
    public void shouldCreatePatient() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patient);

        MvcResult result = mockMvc.perform(post(API_END_POINT).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void shouldReturnBadRequestForInvalidRequestData() throws Exception {
        patient.getAddress().setAddressLine("h");
        String json = new ObjectMapper().writeValueAsString(patient);
        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"code\":400,\"message\":\"invalid.request\",\"errors\":[{\"code\":2002,\"message\":\"Invalid address.addressLine\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestWithErrorDetailsForMultipleInvalidRequestData() throws Exception {
        patient.getAddress().setAddressLine("h");
        patient.setGender("0");
        String json = new ObjectMapper().writeValueAsString(patient);

        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorInfo errorInfo = new ObjectMapper().readValue(result.getResponse().getContentAsString(), ErrorInfo.class);

        List<ErrorInfo> errorInfoErrors = errorInfo.getErrors();
        Collections.sort(errorInfoErrors);

        Assert.assertEquals(2, errorInfoErrors.size());
        Assert.assertEquals(1010, errorInfoErrors.get(0).getCode());
        Assert.assertEquals(2002, errorInfoErrors.get(1).getCode());
    }

    @Test
    public void shouldReturnBadRequestForInvalidJson() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patient);

        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content("invalidate" + json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"code\":125,\"message\":\"invalid.json\"}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestForInvalidDataProperty() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new InvalidPatient());

        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"code\":126,\"message\":\"Unrecognized field: \\\"invalid_property\\\"\"}", result.getResponse().getContentAsString());
    }

    @After
    public void teardown() {
        cqlTemplate.execute("truncate locations");
        cqlTemplate.execute("truncate patient");
    }

    public void createLocation()
    {
        cqlTemplate.execute(String.format(LOCATION_INSERT_QUERY, GEO_CODE, "10", "04", "09", "20", "01"));
    }

    private class InvalidPatient {

        @JsonProperty("nid")
        public String nationalId = "1234567890123";

        @JsonProperty("invalid_property")
        public String birthRegistrationNumber = "some thing";
    }
}
