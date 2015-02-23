package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.mapper.ChangeSet;
import org.sharedhealth.mci.web.mapper.PatientAuditLogData;
import org.sharedhealth.mci.web.service.PatientAuditService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PatientAuditControllerTest {

    private static final String API_END_POINT = "/api/v1/audit/patients/{healthId}";

    @Mock
    private PatientAuditService auditService;
    @Mock
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    private MockMvc mockMvc;

    @Before
    public void setup() throws ParseException {
        initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PatientAuditController(auditService))
                .setValidator(localValidatorFactoryBean)
                .build();
    }

    @Test
    public void shouldFindByHealthId() throws Exception {
        String healthId = "h100";
        String eventTime = "2015-01-02T10:20:30Z";
        when(auditService.findByHealthId(healthId)).thenReturn(buildAuditLogs(eventTime));

        MvcResult mvcResult = mockMvc.perform(get(API_END_POINT, healthId).contentType(APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].event_time", is(eventTime)))

                .andExpect(jsonPath("$.[0].change_set[0].field_name", is(GIVEN_NAME)))
                .andExpect(jsonPath("$.[0].change_set[0].old_value", is("Harry")))
                .andExpect(jsonPath("$.[0].change_set[0].new_value", is("Potter")))
                .andExpect(jsonPath("$.[0].change_set[0].proposed_by", is("x")))
                .andExpect(jsonPath("$.[0].change_set[0].approved_by", is("y")))

                .andExpect(jsonPath("$.[0].change_set[1].field_name", is(OCCUPATION)))
                .andExpect(jsonPath("$.[0].change_set[1].old_value", is("Wizard")))
                .andExpect(jsonPath("$.[0].change_set[1].new_value", is("Jobless")))
                .andExpect(jsonPath("$.[0].change_set[1].proposed_by", is("admin")))
                .andExpect(jsonPath("$.[0].change_set[1].approved_by", is(nullValue())))

                .andExpect(jsonPath("$.[0].change_set[2].field_name", is(EDU_LEVEL)))
                .andExpect(jsonPath("$.[0].change_set[2].old_value", is("Std 12")))
                .andExpect(jsonPath("$.[0].change_set[2].new_value", is("Std 10")))
                .andExpect(jsonPath("$.[0].change_set[2].proposed_by", is("p")))
                .andExpect(jsonPath("$.[0].change_set[2].approved_by", is("q")));


        verify(auditService).findByHealthId(healthId);
    }

    private List<PatientAuditLogData> buildAuditLogs(String eventTime) {
        List<PatientAuditLogData> logs = new ArrayList<>();

        PatientAuditLogData log = new PatientAuditLogData();
        log.setEventTime(eventTime);
        log.setChangeSet(buildChangeSet());
        logs.add(log);

        return logs;
    }

    private List<ChangeSet> buildChangeSet() {
        List<ChangeSet> changeSets = new ArrayList<>();

        ChangeSet changeSet1 = new ChangeSet();
        changeSet1.setFieldName(GIVEN_NAME);
        changeSet1.setOldValue("Harry");
        changeSet1.setNewValue("Potter");
        changeSet1.setProposedBy("x");
        changeSet1.setApprovedBy("y");
        changeSets.add(changeSet1);

        ChangeSet changeSet2 = new ChangeSet();
        changeSet2.setFieldName(OCCUPATION);
        changeSet2.setOldValue("Wizard");
        changeSet2.setNewValue("Jobless");
        changeSet2.setProposedBy("admin");
        changeSet2.setApprovedBy(null);
        changeSets.add(changeSet2);

        ChangeSet changeSet3 = new ChangeSet();
        changeSet3.setFieldName(EDU_LEVEL);
        changeSet3.setOldValue("Std 12");
        changeSet3.setNewValue("Std 10");
        changeSet3.setProposedBy("p");
        changeSet3.setApprovedBy("q");
        changeSets.add(changeSet3);

        return changeSets;
    }
}