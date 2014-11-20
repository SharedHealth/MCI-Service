package org.sharedhealth.mci.web.controller;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.sharedhealth.mci.web.mapper.Relation;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
public class PatientControllerIT extends BaseControllerTest {
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

        Address presentAddress = new Address();
        presentAddress.setAddressLine("house-12");
        presentAddress.setDivisionId("10");
        presentAddress.setDistrictId("04");
        presentAddress.setUpazillaId("09");
        presentAddress.setCityCorporationId("20");
        presentAddress.setVillage("10");
        presentAddress.setWardId("01");
        presentAddress.setCountryCode("050");

        patientMapper.setAddress(presentAddress);

        Address permanentAddress = new Address();
        permanentAddress.setAddressLine("house-12");
        permanentAddress.setDivisionId("10");
        permanentAddress.setDistrictId("04");
        permanentAddress.setUpazillaId("09");
        permanentAddress.setCityCorporationId("20");
        permanentAddress.setVillage("10");
        permanentAddress.setWardId("01");
        permanentAddress.setCountryCode("050");

        patientMapper.setPermanentAddress(permanentAddress);
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
    public void shouldCreatePatientForAnyPostCodeWithPermanentAddressWhenCountryCodeIsNotBangladesh() throws Exception {
        patientMapper.getPermanentAddress().setCountryCode("051");
        patientMapper.getPermanentAddress().setPostCode("12345");
        String json = mapper.writeValueAsString(patientMapper);

        MvcResult result = createPatient(json);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void shouldReturnBadRequestForInvalidPostCodeWithPermanentAddressWhenCountryCodeIsBangladesh() throws Exception {
        patientMapper.getPermanentAddress().setCountryCode("050");
        patientMapper.getPermanentAddress().setPostCode("12345");
        String json = mapper.writeValueAsString(patientMapper);

        mockMvc.perform(MockMvcRequestBuilders.post(API_END_POINT).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

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
                .andExpect(status().isNotFound())
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

        MvcResult getResult = mockMvc.perform(get(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
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

        MvcResult getResult = mockMvc.perform(get(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).contentType(APPLICATION_JSON))
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

        PatientMapper patient = getPatientMapperObjectByHealthId(healthId);
        Assert.assertTrue(isRelationsEqual(original.getRelations(), patient.getRelations()));
    }

    @Test
    public void shouldUpdatePatientSuccessfullyForValidData() throws Exception {
        String json = asString("jsons/patient/full_payload.json");

        PatientMapper original = getPatientObjectFromString(json);

        MvcResult createdResult = createPatient(json);

        final MCIResponse createdResponse = getMciResponse(createdResult);
        String healthId = createdResponse.getId();

        mockMvc.perform(put(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).content(json).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientMapper patient = getPatientMapperObjectByHealthId(healthId);

        assertPatientEquals(original, patient);

    }

    @Test
    public void shouldPatientUpdatePartiallyForValidPartialData() throws Exception {

        String fullPayloadJson = asString("jsons/patient/full_payload.json");
        String nid = "9934677890120";

        PatientMapper original = getPatientObjectFromString(fullPayloadJson);

        MvcResult createdResult = createPatient(fullPayloadJson);

        final MCIResponse createdResponse = getMciResponse(createdResult);
        String healthId = createdResponse.getId();
        String nidJson = "{\"nid\": \"" + nid + "\"}";

        MvcResult updatedResult = mockMvc.perform(put(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).content(nidJson).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientMapper patient = getPatientMapperObjectByHealthId(healthId);
        original.setNationalId(nid);
        assertPatientEquals(original, patient);

    }

    @Test
    public void shouldRemoveAddressBlockOptionalFieldsIfNotGiven() throws Exception {

        String fullPayloadJson = asString("jsons/patient/full_payload.json");

        MvcResult createdResult = createPatient(fullPayloadJson);

        final MCIResponse createdResponse = getMciResponse(createdResult);
        String healthId = createdResponse.getId();
        String addressJson = asString("jsons/patient/payload_with_address.json");
        PatientMapper patientMapper1 = getPatientObjectFromString(addressJson);

        MvcResult updatedResult = mockMvc.perform(put(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).content(addressJson).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientMapper patient = getPatientMapperObjectByHealthId(healthId);

        assertEquals(patientMapper1.getAddress(), patient.getAddress());

    }

    @Test
    public void shouldUpdateSingleRelationBlock() throws Exception {

        String json = asString("jsons/patient/payload_with_multiple_relations.json");

        MvcResult createdResult = createPatient(json);

        final MCIResponse createdResponse = getMciResponse(createdResult);
        String healthId = createdResponse.getId();
        PatientMapper original = getPatientMapperObjectByHealthId(healthId);

        String relationJson = asString("jsons/patient/payload_relation_with_id.json");

        Relation fth = original.getRelationOfType("FTH");
        relationJson = relationJson.replace("__RELATION_ID__", fth.getId());

        PatientMapper updateRequestPatientMapper = getPatientObjectFromString(relationJson);
        original.getRelations().set(original.getRelations().indexOf(fth), updateRequestPatientMapper.getRelations().get(0));

        mockMvc.perform(put(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).content(relationJson).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientMapper updatedPatient = getPatientMapperObjectByHealthId(healthId);

        assertTrue(isRelationsEqual(original.getRelations(), updatedPatient.getRelations()));
    }

    @Test
    public void shouldRemoveRelationBlockWithEmptyData() throws Exception {

        String json = asString("jsons/patient/payload_with_multiple_relations.json");

        MvcResult createdResult = createPatient(json);

        final MCIResponse createdResponse = getMciResponse(createdResult);
        String healthId = createdResponse.getId();
        PatientMapper original = getPatientMapperObjectByHealthId(healthId);

        String relationJson = asString("jsons/patient/payload_with_empty_relation.json");

        Relation fth = original.getRelationOfType("FTH");
        relationJson = relationJson.replace("__RELATION_ID__", fth.getId());

        original.getRelations().remove(fth);

        mockMvc.perform(put(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).content(relationJson).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientMapper updatedPatient = getPatientMapperObjectByHealthId(healthId);

        assertTrue(isRelationsEqual(original.getRelations(), updatedPatient.getRelations()));
    }

    @Test
    public void shouldAddNewRelationBlockOnUpdate() throws Exception {

        String json = asString("jsons/patient/payload_with_multiple_relations.json");

        MvcResult createdResult = createPatient(json);

        final MCIResponse createdResponse = getMciResponse(createdResult);
        String healthId = createdResponse.getId();
        PatientMapper original = getPatientMapperObjectByHealthId(healthId);

        String relationJson = asString("jsons/patient/payload_with_new_relation.json");

        PatientMapper newPatientMapper = getPatientObjectFromString(relationJson);

        original.getRelations().add(newPatientMapper.getRelations().get(0));

        mockMvc.perform(put(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).content(relationJson).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientMapper updatedPatient = getPatientMapperObjectByHealthId(healthId);

        assertTrue(isRelationsEqual(original.getRelations(), updatedPatient.getRelations()));
    }

    @Test
    public void shouldNotSaveDuplicateRelationBlock() throws Exception {

        String json = asString("jsons/patient/payload_with_duplicate_relations.json");

        MvcResult createdResult = createPatient(json);

        final MCIResponse createdResponse = getMciResponse(createdResult);
        String healthId = createdResponse.getId();
        PatientMapper original = getPatientMapperObjectByHealthId(healthId);

        assertEquals(1, original.getRelations().size());

    }

    @Test
    public void shouldReturnErrorResponseOnUpdateRequestWithWrongRelationId() throws Exception {

        String json = asString("jsons/patient/payload_with_multiple_relations.json");

        MvcResult createdResult = createPatient(json);

        final MCIResponse createdResponse = getMciResponse(createdResult);
        String healthId = createdResponse.getId();
        PatientMapper original = getPatientMapperObjectByHealthId(healthId);

        String relationJson = asString("jsons/patient/payload_with_empty_relation.json");

        Relation fth = original.getRelationOfType("FTH");
        relationJson = relationJson.replace("__RELATION_ID__", "random-id");

        original.getRelations().remove(fth);

        MvcResult result = mockMvc.perform(put(API_END_POINT + "/" + healthId).accept(APPLICATION_JSON).content(relationJson).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\",\"errors\":[{\"code\":1004,\"field\":\"relations\",\"message\":\"invalid relations\"}]}", result.getResponse().getContentAsString());

    }
}
