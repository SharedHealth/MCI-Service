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
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.InstanceOf;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfig;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.handler.ErrorHandler;
import org.sharedhealth.mci.web.handler.MCIError;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.sharedhealth.mci.web.mapper.Relation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientRestApiTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cqlTemplate;

    private MockMvc mockMvc;
    private PatientMapper patientMapper;
    private ObjectMapper mapper = new ObjectMapper();
    public static final String API_END_POINT = "/api/v1/patients";
    public static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        patientMapper = new PatientMapper();
        patientMapper.setGivenName("Scott");
        patientMapper.setSurName("Tiger");
        patientMapper.setGender("M");
        patientMapper.setDateOfBirth("2014-12-01");
        patientMapper.setEducationLevel("01");
        patientMapper.setOccupation("02");
        patientMapper.setMaritalStatus("1");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("1716528608");
        phoneNumber.setCountryCode("880");
        phoneNumber.setExtension("02");
        phoneNumber.setAreaCode("01");

        patientMapper.setPhoneNumber(phoneNumber);
        patientMapper.setPrimaryContactNumber(phoneNumber);

        Address address = new Address();
        address.setAddressLine("house-12");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazillaId("09");
        address.setCityCorporationId("20");
        address.setVillage("10");
        address.setWardId("01");
        address.setCountryCode("050");

        patientMapper.setAddress(address);
    }

    @Test
    public void shouldCreatePatient() throws Exception {
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = createPatient(json);

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void shouldReturnBadRequestForInvalidRequestData() throws Exception {
        patientMapper.getAddress().setAddressLine("h");
        String json = mapper.writeValueAsString(patientMapper);
        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1002,\"field\":\"present_address.address_line\",\"message\":\"invalid present_address.address_line\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestWithErrorDetailsForMultipleInvalidRequestData() throws Exception {
        patientMapper.getAddress().setAddressLine("h");
        patientMapper.setGender("0");
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorHandler errorHandler = mapper.readValue(result.getResponse().getContentAsString(), ErrorHandler.class);

        List<MCIError> errorInfoErrors = errorHandler.getErrors();
        Collections.sort(errorInfoErrors);

        Assert.assertEquals(2, errorInfoErrors.size());
        Assert.assertEquals(1002, errorInfoErrors.get(0).getCode());
        Assert.assertEquals(1004, errorInfoErrors.get(1).getCode());
    }

    @Test
    public void shouldReturnBadRequestForInvalidJson() throws Exception {
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content("invalidate" + json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":2000,\"http_status\":400,\"message\":\"invalid.request\",\"errors\":[{\"code\":2001,\"message\":\"invalid.json\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestIfPresentAddressIsNull() throws Exception {
        patientMapper.setAddress(null);
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1001,\"field\":\"present_address\",\"message\":\"invalid present_address\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestForInvalidDataProperty() throws Exception {
        String json = mapper.writeValueAsString(new InvalidPatient());

        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":2000,\"http_status\":400,\"message\":\"invalid.request\",\"errors\":[{\"code\":2002,\"field\":\"invalid_property\",\"message\":\"Unrecognized field: 'invalid_property'\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void ShouldPassIFAddressIsValidTillUpazilaLevel() throws Exception {

        patientMapper.getAddress().setWardId(null);
        patientMapper.getAddress().setCityCorporationId(null);
        patientMapper.getAddress().setUnionId(null);

        String json = mapper.writeValueAsString(patientMapper);

        createPatient(json);

    }

    @Test
    public void shouldReturnNotFoundResponseWhenSearchBy_ID_IfPatientNotExist() throws Exception {

        MvcResult result = mockMvc.perform(get(API_END_POINT + "/random-1000"))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(new InstanceOf(PatientNotFoundException.class)))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().string(asString("jsons/response/error_404.json")));
    }

    @Test
    public void shouldReturnNotFoundResponseIfPatientNotExistForUpdate() throws Exception {
        String json = mapper.writeValueAsString(patientMapper);

        mockMvc.perform(put(API_END_POINT + "/health-1000").accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(asString("jsons/response/error_404.json")))
                .andReturn();
    }

    @Test
    public void shouldReturnErrorResponseIfHealthIdGivenWhileCreateApiCall() throws Exception {
        String json = asString("jsons/patient/payload_with_hid.json");

        mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(asString("jsons/response/error_hid.json")))
                .andReturn();
    }

    @Test
    public void shouldReturnNotFoundResponseIfHIDNotMatchWithUrlHid() throws Exception {
        patientMapper.setHealthId("health-100");
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = mockMvc.perform(put(API_END_POINT + "/health-1000").accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1004,\"field\":\"hid\",\"message\":\"invalid hid\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestIfOnlySurNameGiven() throws Exception {
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = mockMvc.perform(get(API_END_POINT + "?sur_name=Tiger").accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1006,\"message\":\"Invalid search parameter\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestIfOnlyGivenNameGiven() throws Exception {
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = mockMvc.perform(get(API_END_POINT + "?given_name=Tiger").accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1006,\"message\":\"Invalid search parameter\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnOkResponseIfPatientNotExistWithSurNameAndAddress() throws Exception {
        patientMapper.setHealthId("health-100");
        String json = mapper.writeValueAsString(patientMapper);
        String present_address = patientMapper.getAddress().getDivisionId() +
                patientMapper.getAddress().getDistrictId() + patientMapper.getAddress().getUpazillaId();
        String surName = "Mazumder";
        MvcResult result = mockMvc.perform(get(API_END_POINT + "?sur_name="+surName + "&present_address=" + present_address).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertEquals("[]", body.getResults().toString());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnOkResponseIfPatientNotExistWithGivenNameAndAddress() throws Exception {
        patientMapper.setHealthId("health-100");
        String json = mapper.writeValueAsString(patientMapper);
        String present_address = patientMapper.getAddress().getDivisionId() +
                patientMapper.getAddress().getDistrictId() + patientMapper.getAddress().getUpazillaId();
        String surName = "Raju";
        MvcResult result = mockMvc.perform(get(API_END_POINT + "?given_name="+surName + "&present_address=" + present_address).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertEquals("[]", body.getResults().toString());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnAllTheCreatedPatientFieldAfterGetAPICall() throws Exception {
        String json = asString("jsons/patient/full_payload.json");

        PatientMapper original = getPatientObjectFromString(json);

        MvcResult result = createPatient(json);

        final MCIResponse body = getMciResponse(result);
        String healthId = body.getId();

        MvcResult getResult = mockMvc.perform(get(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final ResponseEntity asyncResult = (ResponseEntity<PatientMapper>) getResult.getAsyncResult();

        PatientMapper patient = getPatientObjectFromResponse(asyncResult);

        assertPatientEquals(original, patient);
    }

    @Test
    public void shouldGoToUpdateFlowIfCreatePatientRequestWithSimilarPatientData() throws Exception {
        String json = asString("jsons/patient/full_payload.json");

        PatientMapper original = getPatientObjectFromString(json);

        MvcResult firstTimeResponse = createPatient(json);

        final MCIResponse body1 = getMciResponse(firstTimeResponse);
        String healthId = body1.getId();

        original.setGivenName("Updated Full Name");

        MvcResult result = createPatient(mapper.writeValueAsString(original));

        final MCIResponse body = getMciResponse(result);

        Assert.assertEquals(202, body.getHttpStatus());

        MvcResult getResult = mockMvc.perform(get(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final ResponseEntity asyncResult = (ResponseEntity<PatientMapper>) getResult.getAsyncResult();

        PatientMapper patient = getPatientObjectFromResponse(asyncResult);

        Assert.assertEquals("Updated Full Name", patient.getGivenName());
    }

    private PatientMapper getPatientObjectFromResponse(ResponseEntity asyncResult) throws Exception {
        return getPatientObjectFromString(mapper.writeValueAsString((PatientMapper) asyncResult.getBody()));
    }

    @Test
    public void shouldCreateRelationsData() throws Exception {
        String json = asString("jsons/patient/payload_with_multiple_relations.json");

        PatientMapper original = getPatientObjectFromString(json);

        MvcResult result = createPatient(json);

        final MCIResponse body = getMciResponse(result);
        String healthId = body.getId();

        MvcResult getResult = mockMvc.perform(get(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        final ResponseEntity asyncResult = (ResponseEntity<PatientMapper>) getResult.getAsyncResult();

        PatientMapper patient = getPatientObjectFromResponse(asyncResult);
        synchronizeRelationsId(original, patient);
        Assert.assertTrue(isRelationsEqual(original.getRelations(), patient.getRelations()));
    }

    @Test
    public void shouldReturnBadRequestIfOnlyExtensionOrCountryCodeOrAreaCodeGiven() throws Exception {
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = mockMvc.perform(get(API_END_POINT +
                "?country_code=880&area_code=02&extension=122").accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1006,\"message\":\"Invalid search parameter\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestIfPresentAddressNotGivenWithPhoneNumber() throws Exception {
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = mockMvc.perform(get(API_END_POINT +
                "?phone_number=1716528608").accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1006,\"message\":\"Invalid search parameter\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestIfOnlyCountryCodeGiven() throws Exception {
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = mockMvc.perform(get(API_END_POINT +
                "?country_code=880").accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1006,\"message\":\"Invalid search parameter\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnOkResponseIfPatientNotExistWithPhoneNumber() throws Exception {
        patientMapper.setHealthId("health-100");
        String json = mapper.writeValueAsString(patientMapper);
        String present_address = patientMapper.getAddress().getDivisionId() +
                patientMapper.getAddress().getDistrictId() + patientMapper.getAddress().getUpazillaId();
        MvcResult result = mockMvc.perform(get(API_END_POINT +
                "?phone_no=123456&country_code=880&present_address="+present_address).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        final MCIMultiResponse body = getMciMultiResponse(result);
        Assert.assertEquals("[]", body.getResults().toString());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    @Test
    public void shouldReturnAllTheCreatedPatientIfPhoneNumberMatchBySearch() throws Exception {
        String json = mapper.writeValueAsString(patientMapper);
        String present_address = patientMapper.getAddress().getDivisionId() +
                patientMapper.getAddress().getDistrictId() + patientMapper.getAddress().getUpazillaId();
        createPatient(json);
        createPatient(json);
        createPatient(json);

        MvcResult result = mockMvc.perform(get(API_END_POINT +
                "?phone_no=1716528608&country_code=880&present_address="+present_address).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final MCIMultiResponse body = getMciMultiResponse(result);
        PatientMapper patientMapper1 = (PatientMapper)body.getResults().get(0);
        Assert.assertEquals("1716528608", patientMapper1.getPhoneNumber().getNumber());
        Assert.assertEquals(200, body.getHttpStatus());
    }

    private MvcResult createPatient(String json) throws Exception {
        return mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    private MCIMultiResponse getMciMultiResponse(MvcResult result) {
        final ResponseEntity asyncResult = (ResponseEntity< MCIMultiResponse>) result.getAsyncResult();

        return (MCIMultiResponse)asyncResult.getBody();
    }

    private MCIResponse getMciResponse(MvcResult result) {
        final ResponseEntity asyncResult = (ResponseEntity< MCIResponse>) result.getAsyncResult();

        return (MCIResponse)asyncResult.getBody();
    }

    private  PatientMapper getPatientObjectFromString(String json) throws Exception  {
        return mapper.readValue(json, PatientMapper.class);
    }

    private boolean isRelationsEqual(List<Relation> original, List<Relation> patient) {
        return original.containsAll(patient) && patient.containsAll(original);
    }

    private void assertPatientEquals(PatientMapper original, PatientMapper patient) {
        synchronizeAutoGeneratedFields(original, patient);
        Assert.assertEquals(original, patient);
    }

    private void synchronizeAutoGeneratedFields(PatientMapper original, PatientMapper patient) {
        original.setHealthId(patient.getHealthId());
        original.setCreatedAt(patient.getCreatedAt());
        original.setUpdatedAt(patient.getUpdatedAt());
        synchronizeRelationsId(original, patient);
    }

    private void synchronizeRelationsId(PatientMapper original, PatientMapper patient) {
        int y = original.getRelations().size();

        for(int x = 0; x < y; x = x+1) {
            original.getRelations().get(x).setId(patient.getRelationOfType(original.getRelations().get(x).getType()).getId());
        }
    }

    @After
    public void teardown() {
        cqlTemplate.execute("truncate patient");
    }

    private class InvalidPatient {

        @JsonProperty("nid")
        public String nationalId = "1234567890123";

        @JsonProperty("invalid_property")
        public String birthRegistrationNumber = "some thing";
    }
}
