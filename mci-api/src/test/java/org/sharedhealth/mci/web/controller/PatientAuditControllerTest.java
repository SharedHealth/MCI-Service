package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.security.TokenAuthentication;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.mapper.PatientAuditLogData;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.Requester;
import org.sharedhealth.mci.web.mapper.RequesterDetails;
import org.sharedhealth.mci.web.service.PatientAuditService;
import org.sharedhealth.mci.web.service.PatientService;
import org.sharedhealth.mci.web.service.RequesterService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PatientAuditControllerTest {

    private static final String API_END_POINT = "/audit/patients/{healthId}";

    @Mock
    private PatientService patientService;
    @Mock
    private PatientAuditService auditService;
    @Mock
    private RequesterService requesterService;
    @Mock
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    private MockMvc mockMvc;

    @Before
    public void setup() throws ParseException {
        initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PatientAuditController(patientService, auditService, requesterService))
                .setValidator(localValidatorFactoryBean)
                .build();

        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(getUserInfo(), true));

    }

    private UserInfo getUserInfo() {
        UserProfile userProfile = new UserProfile("facility", "100067", null);

        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(userProfile));
    }

    @Test
    public void shouldFindByHealthId() throws Exception {
        String healthId = "h100";
        PatientData patient = new PatientData();
        patient.setRequester("Bahmni", "Dr. Monika");
        patient.setCreatedAt(timeBased());
        patient.setCreatedBy(new Requester(new RequesterDetails("r100", "CHW"), null, null));
        when(patientService.findByHealthId(healthId)).thenReturn(patient);

        String eventTime = "2015-01-02T10:20:30Z";
        when(auditService.findByHealthId(healthId)).thenReturn(buildAuditLogs(eventTime));

        MvcResult mvcResult = mockMvc.perform(get(API_END_POINT, healthId).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.created_by.facility.id", is("r100")))
                .andExpect(jsonPath("$.created_by.facility.name", is("CHW")))
                .andExpect(jsonPath("$.created_at", is(patient.getCreatedAtAsString())))

                .andExpect(jsonPath("$.updates.[0].event_time", is(eventTime)))

                .andExpect(jsonPath("$.updates.[0].requested_by.given_name.facility.id", is(asList("f100"))))
                .andExpect(jsonPath("$.updates.[0].requested_by.given_name.facility.name", is(asList("Bahmni1"))))
                .andExpect(jsonPath("$.updates.[0].requested_by.given_name.provider.id", is(asList("p100"))))
                .andExpect(jsonPath("$.updates.[0].requested_by.given_name.provider.name", is(asList("Doc1"))))

                .andExpect(jsonPath("$.updates.[0].requested_by.occupation.facility.id", is(asList("f200"))))
                .andExpect(jsonPath("$.updates.[0].requested_by.occupation.facility.name", is(asList("Bahmni2"))))
                .andExpect(jsonPath("$.updates.[0].requested_by.occupation.provider.id", is(asList("p200"))))
                .andExpect(jsonPath("$.updates.[0].requested_by.occupation.provider.name", is(asList("Doc2"))))

                .andExpect(jsonPath("$.updates.[0].requested_by.edu_level.facility.id", is(asList("f300"))))
                .andExpect(jsonPath("$.updates.[0].requested_by.edu_level.facility.name", is(asList("Bahmni3"))))
                .andExpect(jsonPath("$.updates.[0].requested_by.edu_level.provider.id", is(asList("p300"))))
                .andExpect(jsonPath("$.updates.[0].requested_by.edu_level.provider.name", is(asList("Doc3"))))

                .andExpect(jsonPath("$.updates.[0].approved_by.admin.id", is("a100")))
                .andExpect(jsonPath("$.updates.[0].approved_by.admin.name", is("Admin Monika")))

                .andExpect(jsonPath("$.updates.[0].change_set.given_name", is(notNullValue())))
                .andExpect(jsonPath("$.updates.[0].change_set.given_name.old_value", is("Harry")))
                .andExpect(jsonPath("$.updates.[0].change_set.given_name.new_value", is("Potter")))

                .andExpect(jsonPath("$.updates.[0].change_set.occupation", is(notNullValue())))
                .andExpect(jsonPath("$.updates.[0].change_set.occupation.old_value", is("Wizard")))
                .andExpect(jsonPath("$.updates.[0].change_set.occupation.new_value", is("Jobless")))

                .andExpect(jsonPath("$.updates.[0].change_set.edu_level", is(notNullValue())))
                .andExpect(jsonPath("$.updates.[0].change_set.edu_level.old_value", is("Std 12")))
                .andExpect(jsonPath("$.updates.[0].change_set.edu_level.new_value", is("Std 10")));

        verify(patientService).findByHealthId(healthId);
        verify(auditService).findByHealthId(healthId);
    }

    private List<PatientAuditLogData> buildAuditLogs(String eventTime) {
        List<PatientAuditLogData> logs = new ArrayList<>();
        PatientAuditLogData log = new PatientAuditLogData();
        log.setEventTime(eventTime);
        log.setChangeSet(buildChangeSets());
        log.setApprovedBy(new Requester(null, null, new RequesterDetails("a100", "Admin Monika")));
        log.addRequestedBy(GIVEN_NAME, buildRequesters("f100", "Bahmni1", "p100", "Doc1"));
        log.addRequestedBy(OCCUPATION, buildRequesters("f200", "Bahmni2", "p200", "Doc2"));
        log.addRequestedBy(EDU_LEVEL, buildRequesters("f300", "Bahmni3", "p300", "Doc3"));
        logs.add(log);
        return logs;
    }

    private Set<Requester> buildRequesters(String facilityId, String facilityName, String providerId, String providerName) {
        Set<Requester> requesters = new HashSet<>();
        Requester facility = new Requester(new RequesterDetails(facilityId, facilityName),
                new RequesterDetails(providerId, providerName), null);
        requesters.add(facility);
        return requesters;
    }

    private Map<String, Map<String, Object>> buildChangeSets() {
        Map<String, Map<String, Object>> changeSets = new HashMap<>();
        changeSets.put(GIVEN_NAME, buildChangeSet("Harry", "Potter"));
        changeSets.put(OCCUPATION, buildChangeSet("Wizard", "Jobless"));
        changeSets.put(EDU_LEVEL, buildChangeSet("Std 12", "Std 10"));
        return changeSets;
    }

    private Map<String, Object> buildChangeSet(Object oldValue, Object newValue) {
        Map<String, Object> changeSet = new HashMap<>();
        changeSet.put(OLD_VALUE, oldValue);
        changeSet.put(NEW_VALUE, newValue);
        return changeSet;
    }
}