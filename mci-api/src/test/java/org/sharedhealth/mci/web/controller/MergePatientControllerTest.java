package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.domain.model.MCIResponse;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.web.infrastructure.security.TokenAuthentication;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.service.PatientService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import rx.Observable;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.util.JsonMapper.writeValueAsString;
import static org.sharedhealth.mci.web.controller.MergePatientController.ERROR_MSG_MERGE_WITH_ITSELF;
import static org.sharedhealth.mci.web.infrastructure.security.UserInfo.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class MergePatientControllerTest {

    private static final String API_END_POINT = "/mergerequest";
    private static final String USER_INFO_FACILITY = "100067";

    @Mock
    private PatientService patientService;

    @Mock
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    private MockMvc mockMvc;

    @Rule
    public ExpectedException expectedEx = none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MergePatientController(patientService))
                .setValidator(localValidatorFactoryBean)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(getUserInfo(), true));
    }

    private UserInfo getUserInfo() {
        UserProfile userProfile = new UserProfile("facility", USER_INFO_FACILITY, null);
        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(HRM_MCI_USER_GROUP, HRM_MCI_ADMIN, HRM_MCI_APPROVER, HRM_FACILITY_ADMIN_GROUP, HRM_PROVIDER_GROUP)),
                asList(userProfile));
    }

    @Test
    public void shouldMergePatient() throws Exception {
        PatientData patient = new PatientData();
        patient.setActive(false);
        patient.setMergedWith("HID2");
        MCIResponse mciResponse = new MCIResponse(OK);
        when(patientService.update(patient, "HID1")).thenReturn(Observable.just(mciResponse));

        String content = writeValueAsString(patient);

        MvcResult result = mockMvc.perform(put(API_END_POINT + "/HID1").contentType(APPLICATION_JSON).content(content))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotMergePatientWithItself() throws Exception {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage(ERROR_MSG_MERGE_WITH_ITSELF);

        PatientData patient = new PatientData();
        patient.setActive(false);
        patient.setMergedWith("HID1");
        String content = writeValueAsString(patient);

        mockMvc.perform(put(API_END_POINT + "/HID1").contentType(APPLICATION_JSON).content(content))
                .andExpect(request().asyncStarted())
                .andReturn();
    }
}