package org.mci.web.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mci.web.model.Address;
import org.mci.web.model.Patient;
import org.mci.web.service.PatientService;
import org.mci.web.utils.concurrent.PreResolvedListenableFuture;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PatientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PatientService patientService;

    private PatientController patientController;

    @Before
    public void setup() {
        initMocks(this);
        patientController = new PatientController(patientService);
        mockMvc = MockMvcBuilders.standaloneSetup(patientController).build();
    }

    @Test
    public void shouldRespondWithOkWhenCreatingEncounter() throws Exception {
        Patient patient = new Patient();
        patient.setFullName("fullname");
        patient.setGender("1");
        Address address = new Address();
        address.setDivisionId("10");
        address.setDistrictId("1020");
        address.setUpazillaId("102030");
        address.setUnionId("10203040");
        patient.setAddress(address);

        String json = new ObjectMapper().writeValueAsString(patient);
        String healthId = "healthId-100";

        when(patientService.create(patient)).thenReturn(new PreResolvedListenableFuture<String>(healthId));
        mockMvc.perform
                (
                        post("/patient").content(json).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(healthId));
        verify(patientService).create(patient);
    }
}

