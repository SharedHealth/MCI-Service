package org.sharedhealth.mci.web.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.searchmapping.repository.PatientSearchMappingRepository;
import org.sharedhealth.mci.web.dummy.InvalidPatient;
import org.sharedhealth.mci.web.handler.ErrorHandler;
import org.sharedhealth.mci.web.handler.MCIError;
import org.sharedhealth.mci.web.model.OrgHealthId;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.domain.repository.TestUtil.setupApprovalsConfig;
import static org.sharedhealth.mci.domain.repository.TestUtil.setupLocation;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.utils.HttpUtil.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@RunWith(SpringJUnit4ClassRunner.class)
public class PatientControllerIT extends BaseControllerTest {
    @Autowired
    private PatientSearchMappingRepository searchMappingRepository;

    @Before
    public void setup() throws ParseException {
        MockitoAnnotations.initMocks(this);

        validClientId = "6";
        validEmail = "some@thoughtworks.com";
        validAccessToken = "2361e0a8-f352-4155-8415-32adfb8c2472";


        setUpMockMvcBuilder();
        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailsWithAllRoles.json"))));

        givenThat(get(urlEqualTo("/token/" + facilityAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForFacility.json"))));

        givenThat(WireMock.get(urlEqualTo("/token/" + mciApproverAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIApprover.json"))));

        createPatientData();
        setupApprovalsConfig(cassandraOps);
        setupLocation(cassandraOps);
    }

    @Test
    public void shouldCreatePatientForAnyPostCodeWithPermanentAddressWhenCountryCodeIsNotBangladesh() throws Exception {
        patientData.getPermanentAddress().setCountryCode("051");
        patientData.getPermanentAddress().setPostCode("12345");
        String json = mapper.writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void shouldReturnBadRequestForInvalidPostCodeWithPermanentAddressWhenCountryCodeIsBangladesh() throws
            Exception {
        patientData.getPermanentAddress().setCountryCode("050");
        patientData.getPermanentAddress().setPostCode("12345");
        String json = mapper.writeValueAsString(patientData);

        mockMvc.perform(MockMvcRequestBuilders.post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content(json).contentType
                        (APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void shouldReturnBadRequestForInvalidRequestData() throws Exception {
        patientData.setSurName(null);
        String json = mapper.writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        JSONAssert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\"," +
                "\"errors\":[{\"code\":1001,\"field\":\"sur_name\",\"message\":\"invalid " +
                "sur_name\"}]}", result.getResponse().getContentAsString(), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldReturnBadRequestWithErrorDetailsForMultipleInvalidRequestData() throws Exception {
        patientData.setSurName(null);
        patientData.setGender("0");
        String json = mapper.writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorHandler errorHandler = mapper.readValue(result.getResponse().getContentAsString(), ErrorHandler.class);

        List<MCIError> errorInfoErrors = errorHandler.getErrors();
        Collections.sort(errorInfoErrors);

        assertEquals(2, errorInfoErrors.size());
        assertEquals(1001, errorInfoErrors.get(0).getCode());
        assertEquals(1004, errorInfoErrors.get(1).getCode());
    }

    @Test
    public void shouldReturnBadRequestForInvalidJson() throws Exception {
        String json = mapper.writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content("invalidate" + json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        JSONAssert.assertEquals("{\"error_code\":2000,\"http_status\":400,\"message\":\"invalid.request\"," +
                "\"errors\":[{\"code\":2001,\"message\":\"invalid.json\"}]}", result.getResponse().getContentAsString
                (), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldReturnBadRequestIfPresentAddressIsNull() throws Exception {
        patientData.setAddress(null);
        String json = mapper.writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        JSONAssert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\"," +
                "\"errors\":[{\"code\":1001,\"field\":\"present_address\",\"message\":\"invalid " +
                "present_address\"}]}", result.getResponse().getContentAsString(), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldReturnBadRequestIfAddressGivenInvalidCityCorporationCode() throws Exception {
        patientData.getAddress().setDivisionId("10");
        patientData.getAddress().setDistrictId("09");
        patientData.getAddress().setUpazilaId("04");
        patientData.getAddress().setCityCorporationId("99");
        patientData.getAddress().setUnionOrUrbanWardId(null);
        patientData.getAddress().setRuralWardId(null);
        patientData.getAddress().setVillage(null);

        String json = mapper.writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content(json).contentType
                        (APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        JSONAssert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\"," +
                "\"errors\":[{\"code\":1004,\"field\":\"present_address\",\"message\":\"invalid " +
                "present_address\"}]}", result.getResponse().getContentAsString(), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldNotReturnBadRequestForInvalidDataProperty() throws Exception {
        String json = mapper.writeValueAsString(this.patientData);
        final InvalidPatient patientData = mapper.readValue(json, InvalidPatient.class);
        patientData.invalidProperty = "some value";

        MvcResult result = mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void shouldPassIFAddressIsValidTillUpazilaLevel() throws Exception {
        patientData.getAddress().setRuralWardId(null);
        patientData.getAddress().setCityCorporationId(null);
        patientData.getAddress().setUnionOrUrbanWardId(null);

        String json = mapper.writeValueAsString(patientData);

        postPatient(json);

    }

    @Test
    public void shouldReturnNotFindResponseWhenSearchBy_ID_IfPatientNotExist() throws Exception {
        MvcResult result = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/random-1000")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId))
                .andExpect(status().isNotFound())
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(asyncDispatch(result))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8)).andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        content.contains("{\"message\":\"No patient found with health id: random-1000\",\"http_status\":404}");
    }

    @Test
    public void shouldReturnNotFoundResponseIfPatientNotExistForUpdate() throws Exception {
        String json = mapper.writeValueAsString(patientData);

        MvcResult mvcResult = mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/health-1000")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content
                        (json).contentType
                        (APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        content.contains("{\"message\":\"No patient found with health id: health-1000\",\"http_status\":404}");
    }

    @Test
    public void shouldCreateAPatientForGivenOrganization() throws Exception {
        String json = asString("jsons/patient/payload_with_hid.json");

        insertOrgHID("97000416912", "10000002");

        MvcResult mvcResult = mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content(json).contentType
                        (APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void shouldIdentifyFacilityOfProviderToCreatePatientForOrganization() throws Exception {
        String providerClientId = "18556";
        String providerID = "113068";
        String providerEmail = "provider@gmail.com";
        String providerAccessToken = "40214a6c-e27c-4223-981c-1f837be90f03";

        insertOrgHID("97000416912", "10019842");

        givenThat(get(urlEqualTo("/token/" + providerAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForProvider.json"))));

        givenThat(get(urlEqualTo("/api/1.0/providers/" + providerID + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/provider.json"))));

        String json = asString("jsons/patient/payload_with_hid.json");

        MvcResult mvcResult = mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, providerAccessToken)
                .header(FROM_KEY, providerEmail)
                .header(CLIENT_ID_KEY, providerClientId)
                .accept(APPLICATION_JSON).content(json).contentType
                        (APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    private void insertOrgHID(String healthId, String clientId) {
        cassandraOps.insert(new OrgHealthId(healthId, clientId, timeBased(), null));
    }

    @Test
    public void shouldReturnNotFoundResponseIfHIDNotMatchWithUrlHid() throws Exception {
        patientData.setHealthId("health-100");
        String json = mapper.writeValueAsString(patientData);

        MvcResult result = mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/health-1001")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        JSONAssert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\"," +
                "\"errors\":[{\"code\":1004,\"field\":\"hid\",\"message\":\"invalid hid\"}]}", result.getResponse()
                .getContentAsString(), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldReturnAllTheCreatedPatientFieldAfterGetAPICall() throws Exception {
        String json = asString("jsons/patient/full_payload.json");

        PatientData original = getPatientObjectFromString(json);

        final MCIResponse body = createPatient(json);
        String healthId = body.getId();

        MvcResult getResult = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final ResponseEntity asyncResult = (ResponseEntity<PatientData>) getResult.getAsyncResult();

        PatientData patient = getPatientObjectFromResponse(asyncResult);

        assertPatientEquals(original, patient);
    }

    @Test
    public void shouldGoToUpdateFlowIfCreatePatientRequestWithSimilarPatientData() throws Exception {
        String json = asString("jsons/patient/full_payload.json");
        MvcResult firstTimeResponse = postPatient(json);

        final MCIResponse body1 = getMciResponse(firstTimeResponse);
        String healthId = body1.getId();

        PatientData patientData = getPatientObjectFromString(json);
        patientData.setHealthId(healthId);
        patientData.setUpdatedAt(timeBased());
        searchMappingRepository.saveMappings(patientData);

        PatientData original = getPatientObjectFromString(json);
        original.setGivenName("Updated Given Name");

        MvcResult result = postPatient(mapper.writeValueAsString(original));

        final MCIResponse body = getMciResponse(result);

        Assert.assertEquals(202, body.getHttpStatus());

        MvcResult getResult = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final ResponseEntity asyncResult = (ResponseEntity<PatientData>) getResult.getAsyncResult();

        PatientData patient = getPatientObjectFromResponse(asyncResult);

        Assert.assertEquals("Updated Given Name", patient.getGivenName());
    }

    @Test
    public void shouldCreateRelationsData() throws Exception {
        String json = asString("jsons/patient/payload_with_multiple_relations.json");

        PatientData original = getPatientObjectFromString(json);

        MvcResult result = postPatient(json);

        final MCIResponse body = getMciResponse(result);
        String healthId = body.getId();

        PatientData patient = getPatientData(healthId);
        assertTrue(isRelationsEqual(original.getRelations(), patient.getRelations()));
    }

    @Test
    public void shouldUpdatePatientSuccessfullyForValidData() throws Exception {
        String json = asString("jsons/patient/full_payload.json");
        String updateJson = asString("jsons/patient/full_payload_for_update.json");

        PatientData original = getPatientObjectFromString(json);

        final MCIResponse createdResponse = createPatient(json);
        String healthId = createdResponse.getId();

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content(updateJson).contentType
                        (APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientData patient = getPatientData(healthId);

        assertPatientEquals(original, patient);
    }

    @Test
    public void shouldUpdatePatientPartiallyForValidPartialData() throws Exception {

        String fullPayloadJson = asString("jsons/patient/full_payload.json");
        String nid = "9934677890120";

        PatientData original = getPatientObjectFromString(fullPayloadJson);

        MvcResult createdResult = postPatient(fullPayloadJson);

        final MCIResponse createdResponse = getMciResponse(createdResult);
        String healthId = createdResponse.getId();
        String nidJson = "{\"nid\": \"" + nid + "\"}";

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)

                .accept(APPLICATION_JSON)
                .content(nidJson).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientData patient = getPatientData(healthId);
        original.setNationalId(nid);
        assertPatientEquals(original, patient);
    }

    @Test
    public void shouldRemoveAddressBlockOptionalFieldsIfNotGiven() throws Exception {
        String createJson = asString("jsons/patient/full_payload.json");
        String healthId = createPatient(createJson).getId();

        String updateJson = asString("jsons/patient/payload_with_address.json");

        MvcResult mvcResult = mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId)
                .accept(APPLICATION_JSON)
                .content(updateJson)
                .contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isAccepted());

        String url = "/catchments/302618/approvals/" + healthId;
        String content = asString("jsons/patient/pending_approval_address_accept.json");
        mvcResult = mockMvc.perform(put(url).accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId)
                .content(content).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isAccepted());

        PatientData patientInDb = getPatientMapperObjectByHealthId(healthId);
        PatientData updateRequest = getPatientObjectFromString(updateJson);

        assertEquals(updateRequest.getAddress(), patientInDb.getAddress());
    }

    @Test
    public void shouldRemovePermanentAddressBlock() throws Exception {

        String createJson = asString("jsons/patient/full_payload.json");
        String healthId = createPatient(createJson).getId();
        String updateJson = "{\"permanent_address\":{}}";

        MvcResult mvcResult = mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(updateJson).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isAccepted());

        PatientData patientInDb = getPatientData(healthId);

        assertNull(patientInDb.getPermanentAddress());
    }

    @Test
    public void shouldRemovePhoneNumberBlocks() throws Exception {

        String createJson = asString("jsons/patient/full_payload.json");
        String healthId = createPatient(createJson).getId();

        String updateJson = "{\"primary_contact_number\":{}}";

        MvcResult mvcResult = mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(updateJson).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isAccepted());

        PatientData patientInDb = getPatientData(healthId);

        assertTrue(patientInDb.getPrimaryContactNumber().isEmpty());
    }

    @Test
    public void shouldRemovePhoneBlockOptionalFieldsIfNotGiven() throws Exception {
        String fullPayloadJson = asString("jsons/patient/full_payload.json");

        final MCIResponse createdResponse = createPatient(fullPayloadJson);
        String healthId = createdResponse.getId();
        String phoneJson = "{\"primary_contact_number\":{\"number\": \"9678909\"}}";
        PatientData patientData1 = getPatientObjectFromString(phoneJson);

        MvcResult updatedResult = mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(phoneJson).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientData patient = getPatientData(healthId);

        assertEquals(patientData1.getPrimaryContactNumber(), patient.getPrimaryContactNumber());
    }

    @Test
    public void shouldUpdateSingleRelationBlock() throws Exception {

        String json = asString("jsons/patient/payload_with_multiple_relations.json");

        final MCIResponse createdResponse = createPatient(json);
        String healthId = createdResponse.getId();
        PatientData original = getPatientData(healthId);

        String relationJson = asString("jsons/patient/payload_relation_with_id.json");

        Relation fth = original.getRelationOfType("FTH");
        relationJson = relationJson.replace("__RELATION_ID__", fth.getId());

        PatientData updateRequestPatientData = getPatientObjectFromString(relationJson);
        original.getRelations().set(original.getRelations().indexOf(fth), updateRequestPatientData.getRelations().get
                (0));

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content(relationJson)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientData updatedPatient = getPatientData(healthId);

        assertTrue(isRelationsEqual(original.getRelations(), updatedPatient.getRelations()));
    }

    @Test
    public void shouldRemoveRelationBlockWithEmptyData() throws Exception {

        String json = asString("jsons/patient/payload_with_multiple_relations.json");

        final MCIResponse createdResponse = createPatient(json);
        String healthId = createdResponse.getId();
        PatientData original = getPatientData(healthId);

        String relationJson = asString("jsons/patient/payload_with_empty_relation.json");

        Relation fth = original.getRelationOfType("FTH");
        relationJson = relationJson.replace("__RELATION_ID__", fth.getId());

        original.getRelations().remove(fth);

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content(relationJson)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientData updatedPatient = getPatientData(healthId);

        assertTrue(isRelationsEqual(original.getRelations(), updatedPatient.getRelations()));
    }

    @Test
    public void shouldAddNewRelationBlockOnUpdate() throws Exception {
        String json = asString("jsons/patient/payload_with_multiple_relations.json");

        MCIResponse createdResponse = createPatient(json);
        String healthId = createdResponse.getId();
        PatientData original = getPatientData(healthId);

        String relationJson = asString("jsons/patient/payload_with_new_relation.json");

        PatientData newPatientData = getPatientObjectFromString(relationJson);

        original.getRelations().add(newPatientData.getRelations().get(0));

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content(relationJson)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PatientData updatedPatient = getPatientData(healthId);

        assertTrue(isRelationsEqual(original.getRelations(), updatedPatient.getRelations()));
    }

    @Test
    public void shouldNotSaveDuplicateRelationBlock() throws Exception {

        String json = asString("jsons/patient/payload_with_duplicate_relations.json");

        MvcResult createdResult = postPatient(json);

        final MCIResponse createdResponse = getMciResponse(createdResult);
        String healthId = createdResponse.getId();
        PatientData original = getPatientData(healthId);

        assertEquals(1, original.getRelations().size());
    }

    @Test
    @Ignore
    //    Removing this check. This is a temporary fix so that client can send the relation id while create.
    //    Please DO NOT REMOVE THIS TEST
    public void shouldReturnErrorResponseOnUpdateRequestWithWrongRelationId() throws Exception {
        String json = asString("jsons/patient/payload_with_multiple_relations.json");

        final MCIResponse createdResponse = createPatient(json);
        String healthId = createdResponse.getId();
        PatientData original = getPatientMapperObjectByHealthId(healthId);
        String relationJson = asString("jsons/patient/payload_with_empty_relation.json");

        Relation fth = original.getRelationOfType("FTH");
        relationJson = relationJson.replace("__RELATION_ID__", "random-id");

        original.getRelations().remove(fth);

        MvcResult result = mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content
                        (relationJson).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        JSONAssert.assertEquals("{\"error_code\":1000,\"http_status\":400,\"message\":\"validation error\"," +
                "\"errors\":[{\"code\":1004,\"field\":\"relations\",\"message\":\"invalid relations\"}]}", result
                .getResponse().getContentAsString(), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldUpdateAllUpdatablePatientDataSuccessfullyForValidData() throws Exception {
        String createJson = asString("jsons/patient/full_payload.json");
        String healthId = createPatient(createJson).getId();

        String updateJson = asString("jsons/patient/full_payload_with_updated_data.json");

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId)
                .accept(APPLICATION_JSON).content(updateJson).contentType
                        (APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, facilityAccessToken)
                .header(FROM_KEY, facilityEmail)
                .header(CLIENT_ID_KEY, facilityClientId)
                .accept(APPLICATION_JSON)
                .content(updateJson).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isAccepted());

        String url = "/catchments/3026/approvals/" + healthId;
        String content = asString("jsons/patient/pending_approvals_accept.json");

        mvcResult = mockMvc.perform(put(url).accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, mciApproverAccessToken)
                .header(FROM_KEY, mciApproverEmail)
                .header(CLIENT_ID_KEY, mciApproverClientId)
                .content(content).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isAccepted());

        PatientData updatedPatient = getPatientObjectFromString(updateJson);
        updatedPatient.setActive(true);

        PatientData patient = getPatientMapperObjectByHealthId(healthId);
        patient.setHealthId(patient.getHealthId());
        patient.setDateOfBirth(updatedPatient.getDateOfBirth());
        patient.setGender(updatedPatient.getGender());
        patient.setPhoneNumber(updatedPatient.getPhoneNumber());

        updatedPatient.setOccupation(patient.getOccupation());
        updatedPatient.setRelations(patient.getRelations());

        assertPatientEquals(updatedPatient, patient);
    }

    @Test
    public void shouldCreateActivePatient() throws Exception {
        patientData.setActive(true);
        String json = mapper.writeValueAsString(patientData);
        patientData.setActive(null);

        MvcResult result = mockMvc.perform(post(API_END_POINT_FOR_PATIENT)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void shouldNotUpdateActiveFieldUsingUpdateApi() throws Exception {
        String healthId = createPatient(patientData).getId();

        PatientData patientDataWithActiveInfo = new PatientData();
        patientDataWithActiveInfo.setActive(false);

        String json = mapper.writeValueAsString(patientDataWithActiveInfo);

        MvcResult mvcResult = mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content
                        (json).contentType
                        (APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        JSONAssert.assertEquals(asString("jsons/response/error_cannot_update_400.json"), content, JSONCompareMode.STRICT);
    }

    @Test
    public void shouldNotUpdateMergedWithFieldUsingUpdateApi() throws Exception {
        String healthId = createPatient(patientData).getId();
        String targetHealthId = createPatient(patientData).getId();

        PatientData patientDataWithActiveInfo = new PatientData();
        patientDataWithActiveInfo.setMergedWith(targetHealthId);

        String json = mapper.writeValueAsString(patientDataWithActiveInfo);

        MvcResult mvcResult = mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON).content
                        (json).contentType
                        (APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        JSONAssert.assertEquals(asString("jsons/response/error_cannot_update_400.json"), content, JSONCompareMode.STRICT);
    }

    @Test
    public void shouldNotActivateInactivePatient() throws Exception {
        String healthId = createPatient(patientData).getId();
        String targetHealthId = createPatient(patientData).getId();

        PatientData patientDataWithActiveInfo = new PatientData();
        patientDataWithActiveInfo.setActive(false);
        patientDataWithActiveInfo.setMergedWith(targetHealthId);

        String json = mapper.writeValueAsString(patientDataWithActiveInfo);

        String inactiveHid = updatePatient(json, healthId).getId();

        assertEquals(healthId, inactiveHid);

        patientDataWithActiveInfo.setActive(true);
        patientDataWithActiveInfo.setMergedWith(null);

        json = mapper.writeValueAsString(patientDataWithActiveInfo);

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void shouldNotUpdateInactivePatient() throws Exception {
        String healthId = createPatient(patientData).getId();

        PatientData patientDataWithActiveInfo = new PatientData();
        patientDataWithActiveInfo.setActive(false);

        String json = mapper.writeValueAsString(patientDataWithActiveInfo);

        updatePatient(json, healthId);

        PatientData updatePatientData = new PatientData();
        updatePatientData.setGivenName("newName");

        json = mapper.writeValueAsString(updatePatientData);

        mockMvc.perform(put(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void shouldGetSelectiveFieldsForInactivePatient() throws Exception {
        String healthId = createPatient(patientData).getId();
        String targetHealthId = createPatient(patientData).getId();

        PatientData patientDataWithActiveInfo = new PatientData();
        patientDataWithActiveInfo.setActive(false);
        patientDataWithActiveInfo.setMergedWith(targetHealthId);

        String json = mapper.writeValueAsString(patientDataWithActiveInfo);

        String inactiveHid = updatePatient(json, healthId).getId();

        assertEquals(healthId, inactiveHid);


        MvcResult getResult = mockMvc.perform(get(API_END_POINT_FOR_PATIENT + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final ResponseEntity asyncResult = (ResponseEntity<PatientData>) getResult.getAsyncResult();

        PatientData response = getPatientObjectFromResponse(asyncResult);
        PatientData expected = new PatientData();
        expected.setHealthId(healthId);
        expected.setActive(false);
        expected.setMergedWith(targetHealthId);

        assertPatientEquals(response, expected);
    }

    private void createPatientData() {
        patientData = new PatientData();
        patientData.setGivenName("Scott");
        patientData.setSurName("Tiger");
        patientData.setGender("M");
        patientData.setDateOfBirth(parseDate("2014-12-01"));
        patientData.setEducationLevel("01");
        patientData.setOccupation("02");
        patientData.setMaritalStatus("1");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("1716528608");
        phoneNumber.setCountryCode("880");
        phoneNumber.setExtension("02");
        phoneNumber.setAreaCode("01");

        patientData.setPhoneNumber(phoneNumber);
        patientData.setPrimaryContactNumber(phoneNumber);

        Address presentAddress = new Address();
        presentAddress.setAddressLine("house-12");
        presentAddress.setDivisionId("10");
        presentAddress.setDistrictId("04");
        presentAddress.setUpazilaId("09");
        presentAddress.setCityCorporationId("20");
        presentAddress.setUnionOrUrbanWardId("01");
        presentAddress.setRuralWardId(null);
        presentAddress.setVillage("10");
        presentAddress.setCountryCode("050");

        patientData.setAddress(presentAddress);

        Address permanentAddress = new Address();
        permanentAddress.setAddressLine("house-12");
        permanentAddress.setDivisionId("10");
        permanentAddress.setDistrictId("04");
        permanentAddress.setUpazilaId("09");
        permanentAddress.setCityCorporationId("20");
        permanentAddress.setUnionOrUrbanWardId("06");
        permanentAddress.setRuralWardId(null);
        permanentAddress.setVillage("10");
        permanentAddress.setCountryCode("050");

        patientData.setPermanentAddress(permanentAddress);
    }
}
