package org.sharedhealth.mci.web.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;
import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.FROM_KEY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class MergePatientControllerIT extends PatientControllerIT {

    @Test
    public void shouldUpdateOnlyMergedWithFieldForInactivePatient() throws Exception {
        String healthId = createPatient(patientData).getId();
        String targetHealthId = createPatient(patientData).getId();
        String newTargetHealthId = createPatient(patientData).getId();

        PatientData patientDataWithActiveInfo = new PatientData();
        patientDataWithActiveInfo.setActive(false);
        patientDataWithActiveInfo.setMergedWith(targetHealthId);

        String json = mapper.writeValueAsString(patientDataWithActiveInfo);

        String inactiveHid = updatePatient(json, healthId).getId();

        assertEquals(healthId, inactiveHid);

        patientDataWithActiveInfo.setMergedWith(newTargetHealthId);

        json = mapper.writeValueAsString(patientDataWithActiveInfo);

        mockMvc.perform(put(API_END_POINT_FOR_MERGE_REQUEST + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
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

        mockMvc.perform(put(API_END_POINT_FOR_MERGE_REQUEST + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
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

        mockMvc.perform(put(API_END_POINT_FOR_MERGE_REQUEST + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void shouldNotMergePatientWithItself() throws Exception {
        String healthId = createPatient(patientData).getId();

        PatientData patientDataWithActiveInfo = new PatientData();
        patientDataWithActiveInfo.setActive(false);
        patientDataWithActiveInfo.setMergedWith(healthId);

        String json = mapper.writeValueAsString(patientDataWithActiveInfo);

        mockMvc.perform(put(API_END_POINT_FOR_MERGE_REQUEST + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void shouldNotUpdatePatientUsingActiveUpdateApiIfPatientHidNotFound() throws Exception {
        String targetHealthId = createPatient(patientData).getId();

        PatientData patientDataWithActiveInfo = new PatientData();
        patientDataWithActiveInfo.setActive(false);
        patientDataWithActiveInfo.setMergedWith(targetHealthId);

        String json = mapper.writeValueAsString(patientDataWithActiveInfo);

        mockMvc.perform(put(API_END_POINT_FOR_MERGE_REQUEST + "/" + "some_non_existing_hid")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void shouldNotUpdatePatientUsingActiveUpdateApiIfTargetHidNotFound() throws Exception {
        String healthId = createPatient(patientData).getId();

        PatientData patientDataWithActiveInfo = new PatientData();
        patientDataWithActiveInfo.setActive(false);
        patientDataWithActiveInfo.setMergedWith("some_non_existing_hid");

        String json = mapper.writeValueAsString(patientDataWithActiveInfo);

        mockMvc.perform(put(API_END_POINT_FOR_MERGE_REQUEST + "/" + healthId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(APPLICATION_JSON)
                .content(json)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }
}