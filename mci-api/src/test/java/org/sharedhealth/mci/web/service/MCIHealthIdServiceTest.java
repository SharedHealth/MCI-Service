package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.utils.LuhnChecksumGenerator;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.model.GeneratedHIDBlock;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.sharedhealth.mci.web.model.OrgHealthId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.util.JsonMapper.readValue;
import static org.sharedhealth.mci.web.infrastructure.security.UserInfo.*;
import static org.sharedhealth.mci.web.service.HealthIdService.MCI_ORG_CODE;

@RunWith(MockitoJUnitRunner.class)
public class MCIHealthIdServiceTest {
    private MCIProperties mciProperties;
    @Mock
    private HealthIdRepository healthIdRepository;
    @Mock
    private LuhnChecksumGenerator checksumGenerator;
    @Mock
    private GeneratedHidBlockService generatedHidBlockService;

    @Before
    public void setUp() throws Exception {
        mciProperties = new MCIProperties();
        mciProperties.setMciInvalidHidPattern("^[^9]|^.[^89]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        mciProperties.setOrgInvalidHidPattern("^[^9]|^.[^1-7]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        mciProperties.setMciStartHid("9800000000");
        mciProperties.setMciEndHid("9999999999");
        mciProperties.setHealthIdBlockSize("10");
        mciProperties.setHealthIdBlockSizeThreshold("1");
        initMocks(this);
    }

    @Test
    public void validIdsStartWith9() {
        Pattern invalidPattern = Pattern.compile(mciProperties.getMciInvalidHidPattern());
        assertTrue(invalidPattern.matcher("8801543886").find());
        assertFalse(invalidPattern.matcher("9801543886").find());
    }

    @Test
    public void validIdsStartWith98Or99() {
        Pattern invalidPattern = Pattern.compile(mciProperties.getMciInvalidHidPattern());
        assertTrue(invalidPattern.matcher("9101543886").find());
        assertTrue(invalidPattern.matcher("98000034730").find());
        assertFalse(invalidPattern.matcher("9801543886").find());
        assertFalse(invalidPattern.matcher("9901543886").find());
    }

    @Test
    public void validIdsCannotHave4RepeatedChars() {
        Pattern invalidPattern = Pattern.compile(mciProperties.getMciInvalidHidPattern());
        assertTrue(invalidPattern.matcher("98015888861").find());
        assertFalse(invalidPattern.matcher("9901548886").find());
    }

    @Test
    public void validIdsCannotHave2OrMoreInstancesOf3RepeatedChars() {
        Pattern invalidPattern = Pattern.compile(mciProperties.getMciInvalidHidPattern());
        assertFalse(invalidPattern.matcher("9801588861").find());
        assertTrue(invalidPattern.matcher("9991548886").find());
        assertTrue(invalidPattern.matcher("9991118126").find());
        assertTrue(invalidPattern.matcher("9811115255").find());
    }

    @Test
    public void validIdsCannotHaveMoreThan10Chars() {
        Pattern invalidPattern = Pattern.compile(mciProperties.getMciInvalidHidPattern());
        assertFalse(invalidPattern.matcher("9801588861").find());
        assertTrue(invalidPattern.matcher("999154128886").find());
        assertTrue(invalidPattern.matcher("9926").find());
    }

    @Test
    public void shouldExecuteConfiguredInvalidRegex() {
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciStartHid("1000");
        testProperties.setMciEndHid("1099");
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        assertEquals(80, healthIdService.generateAll(getUserInfo()));
    }

    @Test
    public void shouldExecuteCorrectMCIHIDRegex() {
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciStartHid("9800005790");
        testProperties.setMciEndHid("9800005792");
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setMciInvalidHidPattern("^[^9]|^.[^89]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        assertEquals(0, healthIdService.generateAll(getUserInfo()));
    }

    @Test
    public void shouldSaveValidHids() {
        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciStartHid("1000");
        testProperties.setMciEndHid("1099");
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        assertEquals(80, healthIdService.generateAll(getUserInfo()));

        ArgumentCaptor<MciHealthId> healthIdArgumentCaptor = ArgumentCaptor.forClass(MciHealthId.class);
        verify(healthIdRepository, times(80)).saveMciHealthId(healthIdArgumentCaptor.capture());
        verify(checksumGenerator, times(80)).generate(any(String.class));
        assertTrue(String.valueOf(healthIdArgumentCaptor.getValue().getHid()).endsWith("1"));
    }

    @Test
    public void shouldSaveTheGeneratedBlock() throws Exception {
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setMciStartHid("1000");
        testProperties.setMciEndHid("1099");

        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidBlockService.saveGeneratedHidBlock(any(GeneratedHIDBlock.class))).thenReturn(null);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateAll(getUserInfo());

        ArgumentCaptor<GeneratedHIDBlock> argument = ArgumentCaptor.forClass(GeneratedHIDBlock.class);
        verify(generatedHidBlockService, times(1)).saveGeneratedHidBlock(argument.capture());
        GeneratedHIDBlock passedHidBlock = argument.getValue();
        assertEquals(1000, passedHidBlock.getSeriesNo().longValue());
        assertEquals(1000, passedHidBlock.getBeginsAt().longValue());
        assertEquals("MCI", passedHidBlock.getGeneratedFor());
        assertEquals(1099, passedHidBlock.getEndsAt().longValue());
        assertEquals(80, passedHidBlock.getTotalHIDs().longValue());
        assertRequestedBy(passedHidBlock);
    }

    @Test
    public void shouldNotSaveBlockIfNoHIDsAreGenerated() throws Exception {
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setMciStartHid("1040");
        testProperties.setMciEndHid("1050");

        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateAll(getUserInfo());

        verify(generatedHidBlockService, never()).saveGeneratedHidBlock(any(GeneratedHIDBlock.class));
    }

    @Test
    public void shouldNotGenerateAnyHidsIfStartHidIsGreaterThanEndHid() {
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciStartHid("1000");
        testProperties.setMciEndHid("999");
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        assertEquals(0, healthIdService.generateAll(getUserInfo()));
    }

    @Test
    public void shouldFetchBlockIdsForMCIService() {
        ArrayList<MciHealthId> result = new ArrayList<>();
        result.add(new MciHealthId("898998"));
        result.add(new MciHealthId("898999"));
        when(healthIdRepository.getNextBlock(mciProperties.getHealthIdBlockSize())).thenReturn(result);

        HealthIdService healthIdService = new HealthIdService(mciProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        List<MciHealthId> nextBlock = healthIdService.getNextBlock();
        verify(healthIdRepository).getNextBlock(mciProperties.getHealthIdBlockSize());
        assertEquals(2, nextBlock.size());
    }

    @Test
    public void shouldGenerateValidHealthIdsForGivenTotalHIDs() throws Exception {
        long start = 10000;
        long totalHIDs = 100;
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(105|104)\\d*$");

        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateBlock(start, totalHIDs, getUserInfo());

        verify(healthIdRepository, times(100)).saveMciHealthId(any(MciHealthId.class));
        verify(checksumGenerator, times(100)).generate(any(String.class));

        verify(healthIdRepository, times(1)).saveMciHealthId(new MciHealthId("100001"));
        verify(healthIdRepository, never()).saveMciHealthId(new MciHealthId("100401"));
        verify(healthIdRepository, never()).saveMciHealthId(new MciHealthId("100501"));
        verify(healthIdRepository, times(1)).saveMciHealthId(new MciHealthId("101191"));
    }

    @Test
    public void shouldCalculateBlockEndAt() throws Exception {
        long start = 1000;
        long totalHIDs = 50;
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");

        when(generatedHidBlockService.getPreGeneratedHidBlocks(1000L)).thenReturn(new ArrayList<GeneratedHIDBlock>());
        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidBlockService.saveGeneratedHidBlock(any(GeneratedHIDBlock.class))).thenReturn(null);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateBlock(start, totalHIDs, getUserInfo());

        ArgumentCaptor<GeneratedHIDBlock> argument = ArgumentCaptor.forClass(GeneratedHIDBlock.class);
        verify(generatedHidBlockService, times(1)).saveGeneratedHidBlock(argument.capture());
        GeneratedHIDBlock passedHidBlock = argument.getValue();
        assertEquals(1000, passedHidBlock.getSeriesNo().longValue());
        assertEquals(start, passedHidBlock.getBeginsAt().longValue());
        assertEquals(1069, passedHidBlock.getEndsAt().longValue());
        assertEquals(MCI_ORG_CODE, passedHidBlock.getGeneratedFor());
        assertEquals(50, passedHidBlock.getTotalHIDs().longValue());
        assertRequestedBy(passedHidBlock);
    }

    @Test
    public void shouldIdentifyStartOfBlockFromPreGeneratedBlock() throws Exception {
        long start = 1000;
        long totalHIDs = 20;
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");
        GeneratedHIDBlock generatedHIDBlock = new GeneratedHIDBlock(1000L, MCI_ORG_CODE, 1000L, 1069L, 20L, null);

        when(generatedHidBlockService.getPreGeneratedHidBlocks(1000L)).thenReturn(asList(generatedHIDBlock));
        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidBlockService.saveGeneratedHidBlock(any(GeneratedHIDBlock.class))).thenReturn(null);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateBlock(start, totalHIDs, getUserInfo());

        verify(generatedHidBlockService, times(1)).getPreGeneratedHidBlocks(1000L);
        ArgumentCaptor<GeneratedHIDBlock> argument = ArgumentCaptor.forClass(GeneratedHIDBlock.class);
        verify(generatedHidBlockService, times(1)).saveGeneratedHidBlock(argument.capture());
        GeneratedHIDBlock passedHidBlock = argument.getValue();

        assertEquals(1000, passedHidBlock.getSeriesNo().longValue());
        assertEquals(1070, passedHidBlock.getBeginsAt().longValue());
        assertEquals(1089, passedHidBlock.getEndsAt().longValue());
        assertEquals(MCI_ORG_CODE, passedHidBlock.getGeneratedFor());
        assertEquals(20, passedHidBlock.getTotalHIDs().longValue());
        assertRequestedBy(passedHidBlock);
    }

    private void assertRequestedBy(GeneratedHIDBlock passedHidBlock) {
        String requestedBy = passedHidBlock.getRequestedBy();
        Map requesterDetails = readValue(requestedBy, Map.class);
        assertEquals("102", requesterDetails.get("id"));
    }

    @Test
    public void shouldNotSaveBlockIfNoHIDsGeneratedInBlock() throws Exception {
        long start = 1040;
        long totalHIDs = 0;
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");

        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateBlock(start, totalHIDs, getUserInfo());

        verify(generatedHidBlockService, never()).saveGeneratedHidBlock(any(GeneratedHIDBlock.class));
    }

    @Test
    public void shouldAssignBlockFromStartOfSeriesNo() throws Exception {
        long start = 1040;
        long totalHIDs = 50;
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");

        when(generatedHidBlockService.getPreGeneratedHidBlocks(1000L)).thenReturn(new ArrayList<GeneratedHIDBlock>());
        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidBlockService.saveGeneratedHidBlock(any(GeneratedHIDBlock.class))).thenReturn(null);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateBlock(start, totalHIDs, getUserInfo());

        verify(generatedHidBlockService, times(1)).getPreGeneratedHidBlocks(1000L);
        ArgumentCaptor<GeneratedHIDBlock> argument = ArgumentCaptor.forClass(GeneratedHIDBlock.class);
        verify(generatedHidBlockService, times(1)).saveGeneratedHidBlock(argument.capture());
        GeneratedHIDBlock passedHidBlock = argument.getValue();

        assertEquals(1000, passedHidBlock.getSeriesNo().longValue());
        assertEquals(1000, passedHidBlock.getBeginsAt().longValue());
        assertEquals(1069, passedHidBlock.getEndsAt().longValue());
        assertEquals(MCI_ORG_CODE, passedHidBlock.getGeneratedFor());
        assertEquals(50, passedHidBlock.getTotalHIDs().longValue());
        assertRequestedBy(passedHidBlock);
    }

    @Test
    public void shouldGenerateHIDsOnlyInGivenSeries() throws Exception {
        long start = 1040;
        long totalHIDs = 20;
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");
        GeneratedHIDBlock generatedHIDBlock = new GeneratedHIDBlock(1000L, MCI_ORG_CODE, 1000L, 1089L, 80L, null);

        when(generatedHidBlockService.getPreGeneratedHidBlocks(1000L)).thenReturn(asList(generatedHIDBlock));
        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidBlockService.saveGeneratedHidBlock(any(GeneratedHIDBlock.class))).thenReturn(null);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateBlock(start, totalHIDs, getUserInfo());

        verify(generatedHidBlockService, times(1)).getPreGeneratedHidBlocks(1000L);
        ArgumentCaptor<GeneratedHIDBlock> argument = ArgumentCaptor.forClass(GeneratedHIDBlock.class);
        verify(generatedHidBlockService, times(1)).saveGeneratedHidBlock(argument.capture());
        GeneratedHIDBlock passedHidBlock = argument.getValue();

        assertEquals(1000, passedHidBlock.getSeriesNo().longValue());
        assertEquals(1090, passedHidBlock.getBeginsAt().longValue());
        assertEquals(1099, passedHidBlock.getEndsAt().longValue());
        assertEquals(MCI_ORG_CODE, passedHidBlock.getGeneratedFor());
        assertEquals(10, passedHidBlock.getTotalHIDs().longValue());
        assertRequestedBy(passedHidBlock);
    }

    @Test
    public void shouldGenerateHIDsForGivenOrganization() throws Exception {
        long start = 10000;
        long totalHIDs = 100;
        String orgCode = "OTHER-ORG";
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");

        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateBlockForOrg(start, totalHIDs, orgCode, getUserInfo());

        verify(healthIdRepository, times(100)).saveOrgHealthId(any(OrgHealthId.class));
        verify(checksumGenerator, times(100)).generate(anyString());

        verify(healthIdRepository, times(1)).saveOrgHealthId(new OrgHealthId("100001", orgCode, null));
        verify(healthIdRepository, never()).saveOrgHealthId(new OrgHealthId("100401", orgCode, null));
        verify(healthIdRepository, never()).saveOrgHealthId(new OrgHealthId("100501", orgCode, null));
        verify(healthIdRepository, times(1)).saveOrgHealthId(new OrgHealthId("101191", orgCode, null));
    }

    @Test
    public void shouldSaveTheGeneratedBlockForGivenOrganization() throws Exception {
        long start = 10000;
        long totalHIDs = 100;
        String orgCode = "OTHER-ORG";
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOrgInvalidHidPattern("^(1005|1004)\\d*$");

        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateBlockForOrg(start, totalHIDs, orgCode, getUserInfo());

        verify(healthIdRepository, times(100)).saveOrgHealthId(any(OrgHealthId.class));
        verify(checksumGenerator, times(100)).generate(anyString());
        ArgumentCaptor<GeneratedHIDBlock> argument = ArgumentCaptor.forClass(GeneratedHIDBlock.class);
        verify(generatedHidBlockService, times(1)).saveGeneratedHidBlock(argument.capture());

        GeneratedHIDBlock passedHidBlock = argument.getValue();
        assertEquals(start, passedHidBlock.getSeriesNo().longValue());
        assertEquals(start, passedHidBlock.getBeginsAt().longValue());
        assertEquals(10119, passedHidBlock.getEndsAt().longValue());
        assertEquals(100, passedHidBlock.getTotalHIDs().longValue());
        assertRequestedBy(passedHidBlock);
    }

    @Test
    public void shouldMarkHidUsed() {
        MciHealthId MciHealthId = new MciHealthId("898998");
        doNothing().when(healthIdRepository).removedUsedHid(any(MciHealthId.class));
        HealthIdService healthIdService = new HealthIdService(mciProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.markUsed(MciHealthId);
        verify(healthIdRepository).removedUsedHid(MciHealthId);
    }

    private UserInfo getUserInfo() {
        UserProfile adminProfile = new UserProfile("mci-supervisor", "102", asList("10"));

        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(MCI_USER_GROUP, MCI_ADMIN, MCI_APPROVER, FACILITY_GROUP, PROVIDER_GROUP)),
                asList(adminProfile));
    }
}