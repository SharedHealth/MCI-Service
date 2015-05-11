package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.text.ParseException;

import static org.hamcrest.Matchers.is;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DiagnosticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    @Before
    public void setup() throws ParseException {
        initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DiagnosticsController())
                .setValidator(localValidatorFactoryBean)
                .build();
    }

    @Test
    public void shouldCheckHealth() throws Exception {
        mockMvc.perform(get("/diagnostics/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }
}