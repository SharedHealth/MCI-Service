package org.sharedhealth.mci.web.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
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
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientControllerIT extends BaseControllerTest{
    @Before
    public void setup() throws ParseException {
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

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
        assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1002,\"field\":\"present_address.address_line\",\"message\":\"invalid present_address.address_line\"}]}", result.getResponse().getContentAsString());
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

        assertEquals(2, errorInfoErrors.size());
        assertEquals(1002, errorInfoErrors.get(0).getCode());
        assertEquals(1004, errorInfoErrors.get(1).getCode());
    }

    @Test
    public void shouldReturnBadRequestForInvalidJson() throws Exception {
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content("invalidate" + json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals("{\"error_code\":2000,\"http_status\":400,\"message\":\"invalid.request\",\"errors\":[{\"code\":2001,\"message\":\"invalid.json\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestIfPresentAddressIsNull() throws Exception {
        patientMapper.setAddress(null);
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1001,\"field\":\"present_address\",\"message\":\"invalid present_address\"}]}", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnBadRequestForInvalidDataProperty() throws Exception {
        String json = mapper.writeValueAsString(new InvalidPatient());

        MvcResult result = mockMvc.perform(post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals("{\"error_code\":2000,\"http_status\":400,\"message\":\"invalid.request\",\"errors\":[{\"code\":2002,\"field\":\"invalid_property\",\"message\":\"Unrecognized field: 'invalid_property'\"}]}", result.getResponse().getContentAsString());
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

        MvcResult result = mockMvc.perform(put(API_END_POINT + "/health-1001").accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1004,\"field\":\"hid\",\"message\":\"invalid hid\"}]}", result.getResponse().getContentAsString());
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
