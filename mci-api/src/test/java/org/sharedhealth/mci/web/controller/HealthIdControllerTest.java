package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.model.HealthId;
import org.sharedhealth.mci.web.service.HealthIdService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class HealthIdControllerTest {
    @Mock
    HealthIdService service;

    MCIProperties mciProperties;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(getUserInfo(), null));
        mciProperties = new MCIProperties();
        mciProperties.setInvalidHidPattern("^[^9]|^.[^89]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        mciProperties.setMciStartHid("9800000000");
        mciProperties.setMciEndHid("9999999999");

    }

    private UserInfo getUserInfo() {
        UserProfile userProfile = new UserProfile("facility", "100067", null);

        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(userProfile));
    }

    @Test
    public void testGenerate() {
        long start = mciProperties.getMciStartHid(), end = mciProperties.getMciEndHid();
        when(service.generate(start, end)).thenReturn(100L);
        HealthIdController healthIdController = new HealthIdController(service, mciProperties);
        assertEquals("GENERATED 100 Ids", healthIdController.generate().getResult());
        verify(service, times(1)).generate(start, end);
    }

    @Test
    public void testGetNextBlock() {
        when(service.getNextBlock()).thenReturn(getNextBlock());
        HealthIdController healthIdController = new HealthIdController(service, mciProperties);
        assertEquals(3, healthIdController.nextBlock().size());
        verify(service, times(1)).getNextBlock();
    }

    private ArrayList<HealthId> getNextBlock() {
        ArrayList<HealthId> healthIds = new ArrayList<>();
        healthIds.add(new HealthId("123","MCI",0));
        healthIds.add(new HealthId("124","MCI",0));
        healthIds.add(new HealthId("125","MCI",0));
        return healthIds;
    }

    @Test
    public void testGenerateRange() {
        long start = 9800100100L, end = 9800100200L;
        when(service.generate(start, end)).thenReturn(100L);
        HealthIdController healthIdController = new HealthIdController(service, mciProperties);
        assertEquals("GENERATED 100 Ids", healthIdController.generateRange(start, end).getResult());
        verify(service, times(1)).generate(start, end);
    }
}