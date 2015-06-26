package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.security.TokenAuthentication;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMergeData;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PatientSummaryData;
import org.sharedhealth.mci.web.mapper.Requester;
import org.sharedhealth.mci.web.mapper.RequesterDetails;
import org.sharedhealth.mci.web.service.DuplicatePatientService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.asSet;
import static org.sharedhealth.mci.web.infrastructure.security.UserInfo.*;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;
import static org.sharedhealth.mci.web.utils.MCIConstants.DUPLICATION_ACTION_RETAIN_ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DuplicatePatientControllerTest {

    private static final String FACILITY_ID = "100067";
    private static final String PROVIDER_ID = "100068";
    private static final String ADMIN_ID = "102";

    @Mock
    private LocalValidatorFactoryBean validatorFactory;
    @Mock
    private DuplicatePatientService duplicatePatientService;

    private MockMvc mockMvc;

    @Before
    public void setup() throws ParseException {
        initMocks(this);
        DuplicatePatientController duplicatePatientController = new DuplicatePatientController(duplicatePatientService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(duplicatePatientController)
                .setValidator(validatorFactory)
                .build();
        getContext().setAuthentication(new TokenAuthentication(getUserInfo(), true));
    }

    private UserInfo getUserInfo() {
        UserProfile facilityProfile = new UserProfile("facility", FACILITY_ID, asList("1020"));
        UserProfile providerProfile = new UserProfile("provider", PROVIDER_ID, asList("102030"));
        UserProfile adminProfile = new UserProfile("mci-supervisor", ADMIN_ID, asList("10"));

        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(MCI_USER_GROUP, MCI_ADMIN, MCI_APPROVER, FACILITY_GROUP, PROVIDER_GROUP)),
                asList(facilityProfile, providerProfile, adminProfile));
    }

    @Test
    public void shouldFindDuplicatesByCatchment() throws Exception {
        when(duplicatePatientService.findAllByCatchment(new Catchment("102030"))).thenReturn(buildDuplicatePatientDataList());

        String url = "/patients/duplicates/catchments/102030";
        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.results[0]").exists())
                .andExpect(jsonPath("$.results[1]").exists())
                .andExpect(jsonPath("$.results[2]").exists())
                .andExpect(jsonPath("$.results[3]").exists())
                .andExpect(jsonPath("$.results[4]").exists())
                .andExpect(jsonPath("$.results[5]").doesNotExist())

                .andExpect(jsonPath("$.results[0].patient1.hid", is("99002")))
                .andExpect(jsonPath("$.results[0].patient1.given_name", is("A2")))
                .andExpect(jsonPath("$.results[0].patient1.sur_name", is("B2")))
                .andExpect(jsonPath("$.results[0].patient2.hid", is("99001")))
                .andExpect(jsonPath("$.results[0].patient2.given_name", is("A1")))
                .andExpect(jsonPath("$.results[0].patient2.sur_name", is("B1")))
                .andExpect(jsonPath("$.results[0].reasons", is(asList("PHONE", "NID"))))

                .andExpect(jsonPath("$.results[1].patient1.hid", is("99003")))
                .andExpect(jsonPath("$.results[1].patient1.given_name", is("A3")))
                .andExpect(jsonPath("$.results[1].patient1.sur_name", is("B3")))
                .andExpect(jsonPath("$.results[1].patient2.hid", is("99004")))
                .andExpect(jsonPath("$.results[1].patient2.given_name", is("A4")))
                .andExpect(jsonPath("$.results[1].patient2.sur_name", is("B4")))
                .andExpect(jsonPath("$.results[1].reasons", is(asList("NID"))))

                .andExpect(jsonPath("$.results[2].patient1.hid", is("99005")))
                .andExpect(jsonPath("$.results[2].patient1.given_name", is("A5")))
                .andExpect(jsonPath("$.results[2].patient1.sur_name", is("B5")))
                .andExpect(jsonPath("$.results[2].patient2.hid", is("99006")))
                .andExpect(jsonPath("$.results[2].patient2.given_name", is("A6")))
                .andExpect(jsonPath("$.results[2].patient2.sur_name", is("B6")))
                .andExpect(jsonPath("$.results[2].reasons", is(asList("NID"))))

                .andExpect(jsonPath("$.results[3].patient1.hid", is("99007")))
                .andExpect(jsonPath("$.results[3].patient1.given_name", is("A7")))
                .andExpect(jsonPath("$.results[3].patient1.sur_name", is("B7")))
                .andExpect(jsonPath("$.results[3].patient2.hid", is("99008")))
                .andExpect(jsonPath("$.results[3].patient2.given_name", is("A8")))
                .andExpect(jsonPath("$.results[3].patient2.sur_name", is("B8")))
                .andExpect(jsonPath("$.results[3].reasons", is(asList("PHONE"))))

                .andExpect(jsonPath("$.results[4].patient1.hid", is("99009")))
                .andExpect(jsonPath("$.results[4].patient1.given_name", is("A9")))
                .andExpect(jsonPath("$.results[4].patient1.sur_name", is("B9")))
                .andExpect(jsonPath("$.results[4].patient2.hid", is("99010")))
                .andExpect(jsonPath("$.results[4].patient2.given_name", is("A10")))
                .andExpect(jsonPath("$.results[4].patient2.sur_name", is("B10")))
                .andExpect(jsonPath("$.results[4].reasons", is(asList("PHONE", "NID"))));
    }

    private List<DuplicatePatientData> buildDuplicatePatientDataList() {
        Address address = new Address("10", "20", "30");
        PatientSummaryData patientSummaryData1 = buildPatientSummaryData("99001", "A1", "B1", address);
        PatientSummaryData patientSummaryData2 = buildPatientSummaryData("99002", "A2", "B2", address);
        PatientSummaryData patientSummaryData3 = buildPatientSummaryData("99003", "A3", "B3", address);
        PatientSummaryData patientSummaryData4 = buildPatientSummaryData("99004", "A4", "B4", address);
        PatientSummaryData patientSummaryData5 = buildPatientSummaryData("99005", "A5", "B5", address);
        PatientSummaryData patientSummaryData6 = buildPatientSummaryData("99006", "A6", "B6", address);
        PatientSummaryData patientSummaryData7 = buildPatientSummaryData("99007", "A7", "B7", address);
        PatientSummaryData patientSummaryData8 = buildPatientSummaryData("99008", "A8", "B8", address);
        PatientSummaryData patientSummaryData9 = buildPatientSummaryData("99009", "A9", "B9", address);
        PatientSummaryData patientSummaryData10 = buildPatientSummaryData("99010", "A10", "B10", address);

        return asList(new DuplicatePatientData(patientSummaryData1, patientSummaryData2, asSet("NID", "PHONE"), timeBased().toString()),
                new DuplicatePatientData(patientSummaryData2, patientSummaryData1, asSet("NID", "PHONE"), timeBased().toString()),
                new DuplicatePatientData(patientSummaryData3, patientSummaryData4, asSet("NID"), timeBased().toString()),
                new DuplicatePatientData(patientSummaryData5, patientSummaryData6, asSet("NID"), timeBased().toString()),
                new DuplicatePatientData(patientSummaryData7, patientSummaryData8, asSet("PHONE"), timeBased().toString()),
                new DuplicatePatientData(patientSummaryData9, patientSummaryData10, asSet("NID", "PHONE"), timeBased().toString()));
    }

    private PatientSummaryData buildPatientSummaryData(String hid, String givenName, String surname, Address address) {
        PatientSummaryData patientSummaryData = new PatientSummaryData();
        patientSummaryData.setHealthId(hid);
        patientSummaryData.setGivenName(givenName);
        patientSummaryData.setSurName(surname);
        patientSummaryData.setAddress(address);
        return patientSummaryData;
    }

    @Test(expected = Exception.class)
    public void shouldNotFindDuplicatesIfCatchmentDoesNotMatch() throws Exception {

        String url = "/patients/duplicates/catchments/12345";
        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult));
    }

    @Test
    public void shouldMergeDuplicates() throws Exception {
        PatientData patient1 = new PatientData();
        PatientData patient2 = new PatientData();
        DuplicatePatientMergeData data = new DuplicatePatientMergeData(DUPLICATION_ACTION_RETAIN_ALL, patient1, patient2);

        String url = "/patients/duplicates";
        MvcResult mvcResult = mockMvc.perform(put(url).content(writeValueAsString(data)).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted());

        ArgumentCaptor<DuplicatePatientMergeData> argument = ArgumentCaptor.forClass(DuplicatePatientMergeData.class);
        verify(duplicatePatientService).processDuplicates(argument.capture());

        Requester requester = new Requester(FACILITY_ID, PROVIDER_ID);
        requester.setAdmin(new RequesterDetails(ADMIN_ID));
        DuplicatePatientMergeData argValue = argument.getValue();
        assertEquals(DUPLICATION_ACTION_RETAIN_ALL, argValue.getAction());
        assertEquals(requester, argValue.getPatient1().getRequester());
        assertEquals(requester, argValue.getPatient2().getRequester());
    }
}