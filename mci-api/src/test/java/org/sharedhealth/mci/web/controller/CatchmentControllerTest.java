package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.service.PatientService;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.text.ParseException;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.utils.DateUtil.fromIsoFormat;
import static org.sharedhealth.mci.web.utils.JsonConstants.FACILITY_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CatchmentControllerTest {

    private static final String API_END_POINT = "/api/v1/catchments";
    @Mock
    private PatientService patientService;
    @Mock
    private LocalValidatorFactoryBean validatorFactory;
    private MockMvc mockMvc;

    @Before
    public void setup() throws ParseException {
        initMocks(this);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CatchmentController(patientService))
                .setValidator(validatorFactory)
                .build();
    }

    @Test
    public void shouldFindPatientByCatchment() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);

        when(patientService.findAllByCatchment(catchment, null, facilityId)).thenReturn(
                asList(buildPatient("h100"), buildPatient("h200"), buildPatient("h300")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);
        String url = format("%s/%s/patients", API_END_POINT, catchmentId);

        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].hid", is("h100")));

        verify(patientService).findAllByCatchment(catchment, null, facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentUpdatedAfterADate() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030405060";
        Catchment catchment = new Catchment(catchmentId);
        String after = "2015-01-01T10:20:30Z";

        when(patientService.findAllByCatchment(catchment, fromIsoFormat(after), facilityId)).thenReturn(
                asList(buildPatient("h100"), buildPatient("h200"), buildPatient("h300")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);
        String url = format("%s/%s/patients?after=%s", API_END_POINT, catchmentId, after);

        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].hid", is("h100")));

        verify(patientService).findAllByCatchment(catchment, fromIsoFormat(after), facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentWithDivisionAndDistrictUpdatedAfterADate() throws Exception {
        String facilityId = "123456";
        String catchmentId = "1020";
        Catchment catchment = new Catchment(catchmentId);
        String after = "2015-01-01T10:20:30Z";

        when(patientService.findAllByCatchment(catchment, fromIsoFormat(after), facilityId)).thenReturn(
                asList(buildPatient("h100"), buildPatient("h200"), buildPatient("h300")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);
        String url = format("%s/%s/patients?after=%s", API_END_POINT, catchmentId, after);

        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].hid", is("h100")));

        verify(patientService).findAllByCatchment(catchment, fromIsoFormat(after), facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentWithDivisionDistrictAndUpazilaUpdatedAfterADate() throws Exception {
        String facilityId = "123456";
        String catchmentId = "102030";
        Catchment catchment = new Catchment(catchmentId);
        String after = "2015-01-01T10:20:30Z";

        when(patientService.findAllByCatchment(catchment, fromIsoFormat(after), facilityId)).thenReturn(
                asList(buildPatient("h100"), buildPatient("h200"), buildPatient("h300")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);
        String url = format("%s/%s/patients?after=%s", API_END_POINT, catchmentId, after);

        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].hid", is("h100")));

        verify(patientService).findAllByCatchment(catchment, fromIsoFormat(after), facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentWithDivisionDistrictUpazilaAndCityCorpUpdatedAfterADate() throws Exception {
        String facilityId = "123456";
        String catchmentId = "10203040";
        Catchment catchment = new Catchment(catchmentId);
        String after = "2015-01-01T10:20:30Z";

        when(patientService.findAllByCatchment(catchment, fromIsoFormat(after), facilityId)).thenReturn(
                asList(buildPatient("h100"), buildPatient("h200"), buildPatient("h300")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);
        String url = format("%s/%s/patients?after=%s", API_END_POINT, catchmentId, after);

        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].hid", is("h100")));

        verify(patientService).findAllByCatchment(catchment, fromIsoFormat(after), facilityId);
    }

    @Test
    public void shouldFindPatientByCatchmentWithDivisionDistrictUpazilaCityCorpAndUnionUpdatedAfterADate() throws Exception {
        String facilityId = "123456";
        String catchmentId = "1020304050";
        Catchment catchment = new Catchment(catchmentId);
        String after = "2015-01-01T10:20:30Z";

        when(patientService.findAllByCatchment(catchment, fromIsoFormat(after), facilityId)).thenReturn(
                asList(buildPatient("h100"), buildPatient("h200"), buildPatient("h300")));

        HttpHeaders headers = new HttpHeaders();
        headers.add(FACILITY_ID, facilityId);
        String url = format("%s/%s/patients?after=%s", API_END_POINT, catchmentId, after);

        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON).headers(headers))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].hid", is("h100")));

        verify(patientService).findAllByCatchment(catchment, fromIsoFormat(after), facilityId);
    }

    private PatientData buildPatient(String healthId) {
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);
        return patient;
    }
}