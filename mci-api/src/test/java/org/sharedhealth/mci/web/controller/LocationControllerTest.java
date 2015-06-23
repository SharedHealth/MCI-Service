package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.web.infrastructure.security.TokenAuthentication;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.mapper.LocationCriteria;
import org.sharedhealth.mci.web.mapper.LocationData;
import org.sharedhealth.mci.web.service.LocationService;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.security.UserInfo.MCI_USER_GROUP;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class LocationControllerTest {

    private static final String USER_INFO_FACILITY = "100067";
    private static final String API_END_POINT = "/locations";

    @Mock
    private LocationService locationService;
    @Mock
    private LocalValidatorFactoryBean localValidatorFactoryBean;
    @Mock
    private SecurityContext securityContext;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new LocationController(locationService))
                .setValidator(localValidatorFactoryBean)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(getUserInfo(), true));
    }

    private UserInfo getUserInfo() {
        UserProfile userProfile = new UserProfile("facility", USER_INFO_FACILITY, null);
        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100", asList(MCI_USER_GROUP), asList(userProfile));
    }

    @Test
    public void shouldFindLocationByParent() throws Exception {
        List<LocationData> locationDataList = buildLocationData();

        when(locationService.findLocationsByParent(new LocationCriteria())).thenReturn(locationDataList);
        MvcResult result = mockMvc.perform(get(API_END_POINT))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.http_status", is(200)))

                .andExpect(jsonPath("$results[0].id", is(nullValue())))
                .andExpect(jsonPath("$results[0].type", is(nullValue())))
                .andExpect(jsonPath("$results[0].active", is("1")))
                .andExpect(jsonPath("$results[0].updatedAt", is(nullValue())))
                .andExpect(jsonPath("$results[0].code", is("10")))
                .andExpect(jsonPath("$results[0].name", is("Barisal")))

                .andExpect(jsonPath("$results[1].id", is(nullValue())))
                .andExpect(jsonPath("$results[1].type", is(nullValue())))
                .andExpect(jsonPath("$results[1].active", is("1")))
                .andExpect(jsonPath("$results[1].updatedAt", is(nullValue())))
                .andExpect(jsonPath("$results[1].code", is("20")))
                .andExpect(jsonPath("$results[1].name", is("Chittagong")))

                .andExpect(jsonPath("$results[2].id").doesNotExist());
    }

    private List<LocationData> buildLocationData() {
        List<LocationData> locationDataList = new ArrayList<>();
        LocationData locationData = new LocationData();
        locationData.setName("Barisal");
        locationData.setCode("10");
        locationData.setActive("1");
        locationDataList.add(locationData);

        locationData = new LocationData();
        locationData.setName("Chittagong");
        locationData.setCode("20");
        locationData.setActive("1");
        locationDataList.add(locationData);
        return locationDataList;
    }
}
