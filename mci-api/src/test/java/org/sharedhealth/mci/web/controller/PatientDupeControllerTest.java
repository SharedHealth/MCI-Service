package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.security.TokenAuthentication;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientDupeData;
import org.sharedhealth.mci.web.service.PatientDupeService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.asSet;
import static org.sharedhealth.mci.web.infrastructure.security.UserInfo.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PatientDupeControllerTest {

    private static final String FACILITY_ID = "100067";
    private static final String PROVIDER_ID = "100068";

    @Mock
    private LocalValidatorFactoryBean validatorFactory;
    @Mock
    private PatientDupeService dupeService;

    private MockMvc mockMvc;
    private PatientDupeController dupeController;

    @Before
    public void setup() throws ParseException {
        initMocks(this);
        dupeController = new PatientDupeController(dupeService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(dupeController)
                .setValidator(validatorFactory)
                .build();
        getContext().setAuthentication(new TokenAuthentication(getUserInfo(), true));
    }

    private UserInfo getUserInfo() {
        UserProfile facilityProfile = new UserProfile("facility", FACILITY_ID, asList("1020"));
        UserProfile providerProfile = new UserProfile("provider", PROVIDER_ID, asList("102030"));
        UserProfile adminProfile = new UserProfile("mci-supervisor", "102", asList("10"));

        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(MCI_USER_GROUP, MCI_ADMIN, MCI_APPROVER, FACILITY_GROUP, PROVIDER_GROUP)),
                asList(facilityProfile, providerProfile, adminProfile));
    }

    @Test
    public void shouldFindDupesByCatchment() throws Exception {
        when(dupeService.findAllByCatchment(new Catchment("102030"))).thenReturn(buildDupeDataList());

        String url = "/patients/dupes/102030";
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

                .andExpect(jsonPath("$.results[0].hid1", is("99001")))
                .andExpect(jsonPath("$.results[0].hid2", is("99002")))
                .andExpect(jsonPath("$.results[0].reasons", is(asList("PHONE", "NID"))));
    }

    private List<PatientDupeData> buildDupeDataList() {
        return asList(new PatientDupeData("99001", "99002", asSet("NID", "PHONE"), timeBased().toString()),
                new PatientDupeData("99003", "99004", asSet("NID"), timeBased().toString()),
                new PatientDupeData("99005", "99006", asSet("NID"), timeBased().toString()),
                new PatientDupeData("99007", "99008", asSet("PHONE"), timeBased().toString()),
                new PatientDupeData("99009", "99010", asSet("NID", "PHONE"), timeBased().toString()));
    }

    @Test(expected = Exception.class)
    public void shouldNotFindDupesIfCatchmentDoesNotMatch() throws Exception {

        String url = "/patients/dupes/112233";
        MvcResult mvcResult = mockMvc.perform(get(url).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult));
    }
}