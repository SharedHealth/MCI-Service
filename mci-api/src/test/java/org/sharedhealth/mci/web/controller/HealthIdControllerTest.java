package org.sharedhealth.mci.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.model.GeneratedHIDBlock;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.sharedhealth.mci.web.service.HealthIdService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class HealthIdControllerTest {
    @Mock
    private HealthIdService healthIdService;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(getUserInfo(), null));
    }

    private UserInfo getUserInfo() {
        UserProfile userProfile = new UserProfile("facility", "100067", null);

        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(userProfile));
    }

    @Test
    public void testGenerate() {
        GeneratedHIDBlock hidBlock = new GeneratedHIDBlock(1000L, "MCI", 1000L, 1099L, 100L, "");
        when(healthIdService.generateAll(any(UserInfo.class))).thenReturn(hidBlock);
        HealthIdController healthIdController = new HealthIdController(healthIdService);
        assertEquals("Generated 100 HIDs.", healthIdController.generate().getResult());
        verify(healthIdService, times(1)).generateAll(any(UserInfo.class));
    }

    @Test
    public void testGetNextBlock() {
        when(healthIdService.getNextBlock()).thenReturn(getNextBlock());
        HealthIdController healthIdController = new HealthIdController(healthIdService);
        assertEquals(3, healthIdController.nextBlock().size());
        verify(healthIdService, times(1)).getNextBlock();
    }

    private ArrayList<MciHealthId> getNextBlock() {
        ArrayList<MciHealthId> MciHealthIds = new ArrayList<>();
        MciHealthIds.add(new MciHealthId("123"));
        MciHealthIds.add(new MciHealthId("124"));
        MciHealthIds.add(new MciHealthId("125"));
        return MciHealthIds;
    }

    @Test
    public void testGenerateRange() {
        long start = 1000L, total = 100L;
        GeneratedHIDBlock hidBlock = new GeneratedHIDBlock(1000L, "MCI", 1000L, 1099L, 100L, "");
        when(healthIdService.generateBlock(eq(start), eq(total), any(UserInfo.class))).thenReturn(hidBlock);
        HealthIdController healthIdController = new HealthIdController(healthIdService);
        assertEquals("Generated 100 HIDs.", healthIdController.generateBlock(start, total).getResult());
        verify(healthIdService, times(1)).generateBlock(eq(start), eq(total), any(UserInfo.class));
    }

    @Test
    public void testGenerateBlockForOrg() throws Exception {
        long start = 1000L, total = 100L;
        GeneratedHIDBlock hidBlock = new GeneratedHIDBlock(1000L, "other", 1000L, 1099L, 100L, "");
        when(healthIdService.generateBlockForOrg(eq(start), eq(total), eq("other"), any(UserInfo.class))).thenReturn(hidBlock);
        HealthIdController healthIdController = new HealthIdController(healthIdService);
        assertEquals("Generated 100 HIDs.", healthIdController.generateBlockForOrg("other", start, total).getResult());
        verify(healthIdService, times(1)).generateBlockForOrg(eq(start), eq(total), eq("other"), any(UserInfo.class));
    }

    @Test
    public void testGenerateBlockForOrgWhenSeriesIsExhausted() throws Exception {
        long start = 1000L, total = 150L;
        GeneratedHIDBlock hidBlock = new GeneratedHIDBlock(1000L, "other", 1000L, 1099L, 100L, "");
        when(healthIdService.generateBlockForOrg(eq(start), eq(total), eq("other"), any(UserInfo.class))).thenReturn(hidBlock);
        HealthIdController healthIdController = new HealthIdController(healthIdService);
        assertEquals("Can generate only 100 HIDs, because series exhausted. Use another series.", healthIdController.generateBlockForOrg("other", start, total).getResult());
        verify(healthIdService, times(1)).generateBlockForOrg(eq(start), eq(total), eq("other"), any(UserInfo.class));
    }
}