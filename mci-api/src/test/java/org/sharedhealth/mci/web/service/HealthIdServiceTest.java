package org.sharedhealth.mci.web.service;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.sharedhealth.mci.utils.LuhnChecksumGenerator;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.infrastructure.security.UserProfile;
import org.sharedhealth.mci.web.model.GeneratedHIDBlock;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.sharedhealth.mci.web.model.OrgHealthId;

import java.io.File;
import java.util.*;
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
public class HealthIdServiceTest {
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
        mciProperties.setOtherOrgInvalidHidPattern("^[^9]|^.[^1-7]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        mciProperties.setMciStartHid("9800000000");
        mciProperties.setMciEndHid("9999999999");
        mciProperties.setHealthIdBlockSize("10");
        mciProperties.setHealthIdBlockSizeThreshold("1");
        initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        File file = new File("test-hid");
        if (file.exists()) {
            FileUtils.cleanDirectory(file);
            file.delete();
        }
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
        testProperties.setHidStoragePath("test-hid");
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        GeneratedHIDBlock hidBlock = healthIdService.generateAll(getUserInfo());
        assertEquals(80, hidBlock.getTotalHIDs().longValue());
    }

    @Test
    public void shouldExecuteCorrectMCIHIDRegex() {
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciStartHid("9800005790");
        testProperties.setMciEndHid("9800005792");
        testProperties.setHidStoragePath("test-hid");
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setMciInvalidHidPattern("^[^9]|^.[^89]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        GeneratedHIDBlock hidBlock = healthIdService.generateAll(getUserInfo());
        assertEquals(0, hidBlock.getTotalHIDs().longValue());
    }

    @Test
    public void shouldSaveValidHids() {
        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciStartHid("1000");
        testProperties.setMciEndHid("1099");
        testProperties.setHidStoragePath("test-hid");
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        GeneratedHIDBlock hidBlock = healthIdService.generateAll(getUserInfo());
        assertEquals(80, hidBlock.getTotalHIDs().longValue());

        ArgumentCaptor<MciHealthId> healthIdArgumentCaptor = ArgumentCaptor.forClass(MciHealthId.class);
        verify(healthIdRepository, times(80)).saveMciHealthId(healthIdArgumentCaptor.capture());
        verify(checksumGenerator, times(80)).generate(any(String.class));
        assertTrue(String.valueOf(healthIdArgumentCaptor.getValue().getHid()).endsWith("1"));
    }

    @Test
    public void shouldSaveTheGeneratedBlock() throws Exception {
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setMciStartHid("1000");
        testProperties.setMciEndHid("1099");
        testProperties.setHidStoragePath("test-hid");

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
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setMciStartHid("1040");
        testProperties.setMciEndHid("1050");
        testProperties.setHidStoragePath("test-hid");

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
        testProperties.setHidStoragePath("test-hid");
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        GeneratedHIDBlock hidBlock = healthIdService.generateAll(getUserInfo());
        assertEquals(0, hidBlock.getTotalHIDs().longValue());
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
        testProperties.setOtherOrgInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setHidStoragePath("test-hid");

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
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setHidStoragePath("test-hid");

        when(generatedHidBlockService.getPreGeneratedHidBlocks(1000L)).thenReturn(new ArrayList<GeneratedHIDBlock>());
        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidBlockService.saveGeneratedHidBlock(any(GeneratedHIDBlock.class))).thenReturn(null);
        when(healthIdRepository.findOrgHealthId(anyString())).thenReturn(null);

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
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setHidStoragePath("test-hid");
        GeneratedHIDBlock generatedHIDBlock = new GeneratedHIDBlock(1000L, MCI_ORG_CODE, 1000L, 1069L, 20L, null, TimeUuidUtil.uuidForDate(new Date()));

        when(generatedHidBlockService.getPreGeneratedHidBlocks(1000L)).thenReturn(asList(generatedHIDBlock));
        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidBlockService.saveGeneratedHidBlock(any(GeneratedHIDBlock.class))).thenReturn(null);
        when(healthIdRepository.findOrgHealthId(anyString())).thenReturn(null);

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
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setHidStoragePath("test-hid");

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
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setHidStoragePath("test-hid");

        when(generatedHidBlockService.getPreGeneratedHidBlocks(1000L)).thenReturn(new ArrayList<GeneratedHIDBlock>());
        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidBlockService.saveGeneratedHidBlock(any(GeneratedHIDBlock.class))).thenReturn(null);
        when(healthIdRepository.findOrgHealthId(anyString())).thenReturn(null);

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
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setHidStoragePath("test-hid");

        GeneratedHIDBlock generatedHIDBlock = new GeneratedHIDBlock(1000L, MCI_ORG_CODE, 1000L, 1089L, 80L, null, TimeUuidUtil.uuidForDate(new Date()));

        when(generatedHidBlockService.getPreGeneratedHidBlocks(1000L)).thenReturn(asList(generatedHIDBlock));
        when(healthIdRepository.saveMciHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidBlockService.saveGeneratedHidBlock(any(GeneratedHIDBlock.class))).thenReturn(null);
        when(healthIdRepository.findOrgHealthId(anyString())).thenReturn(null);

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
        final Date date = new DateTime().toDate();
        org.joda.time.DateTimeUtils.setCurrentMillisFixed(date.getTime());

        long start = 10000;
        long totalHIDs = 100;
        String orgCode = "OTHER-ORG";
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setHidStoragePath("test-hid");

        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateBlockForOrg(start, totalHIDs, orgCode, getUserInfo());

        verify(healthIdRepository, times(100)).saveOrgHealthId(any(OrgHealthId.class));
        verify(checksumGenerator, times(100)).generate(anyString());

        verify(healthIdRepository, times(1)).saveOrgHealthId(argThat(orgHID("100001", orgCode)));
        verify(healthIdRepository, never()).saveOrgHealthId(argThat(orgHID("100401", orgCode)));
        verify(healthIdRepository, never()).saveOrgHealthId(argThat(orgHID("100501", orgCode)));
        verify(healthIdRepository, times(1)).saveOrgHealthId(argThat(orgHID("101191", orgCode)));
    }

    Matcher<OrgHealthId> orgHID(final String healthId, final String orgCode) {
        return new TypeSafeMatcher<OrgHealthId>() {
            public boolean matchesSafely(OrgHealthId orgHealthId) {
                return healthId.equals(orgHealthId.getHealthId()) && orgCode.equals(orgHealthId.getAllocatedFor());
            }
            public void describeTo(Description description) {
                description.appendText("a OrgHealthID with HID" + healthId);
            }
        };
    }

    @Test
    public void shouldSaveTheGeneratedBlockForGivenOrganization() throws Exception {
        long start = 10000;
        long totalHIDs = 100;
        String orgCode = "OTHER-ORG";
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setHidStoragePath("test-hid");

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
    public void shouldNotGenerateHIDIfAlreadyExist() throws Exception {
        long start = 10000;
        long totalHIDs = 100;
        String orgCode = "OTHER-ORG";
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setOtherOrgInvalidHidPattern("^(1005|1004)\\d*$");
        testProperties.setHidStoragePath("test-hid");

        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(healthIdRepository.findOrgHealthId(anyString())).thenReturn(null, new OrgHealthId("100011", "XYZ", any(UUID.class), null), null);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.generateBlockForOrg(start, totalHIDs, orgCode, getUserInfo());

        verify(healthIdRepository, times(101)).findOrgHealthId(anyString());
        verify(checksumGenerator, times(101)).generate(anyString());
        verify(healthIdRepository, times(100)).saveOrgHealthId(any(OrgHealthId.class));

    }

    @Test
    public void shouldMarkMCIHidUsed() {
        String hid = "898998";
        MciHealthId MciHealthId = new MciHealthId(hid);
        doNothing().when(healthIdRepository).removedUsedHid(any(MciHealthId.class));
        HealthIdService healthIdService = new HealthIdService(mciProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);
        healthIdService.markMCIHealthIdUsed(MciHealthId);
        verify(healthIdRepository).removedUsedHid(MciHealthId);

        ArgumentCaptor<OrgHealthId> captor = ArgumentCaptor.forClass(OrgHealthId.class);
        verify(healthIdRepository, times(1)).saveOrgHealthId(captor.capture());

        OrgHealthId captorValue = captor.getValue();
        assertEquals(hid, captorValue.getHealthId());
        assertEquals(MCI_ORG_CODE, captorValue.getAllocatedFor());
        assertTrue(captorValue.isUsed());
    }

    @Test
    public void shouldMarkOrgHIDAsUsed() throws Exception {
        OrgHealthId orgHealthId = new OrgHealthId("1234", "OTHER", TimeUuidUtil.uuidForDate(new Date()), null);
        HealthIdService healthIdService = new HealthIdService(mciProperties, healthIdRepository, checksumGenerator, generatedHidBlockService);

        assertFalse(orgHealthId.isUsed());
        assertNull(orgHealthId.getUsedAt());
        healthIdService.markOrgHealthIdUsed(orgHealthId);

        ArgumentCaptor<OrgHealthId> captor = ArgumentCaptor.forClass(OrgHealthId.class);
        verify(healthIdRepository, times(1)).saveOrgHealthId(captor.capture());

        assertTrue(orgHealthId.isUsed());
        assertNotNull(orgHealthId.getUsedAt());
    }

    private UserInfo getUserInfo() {
        UserProfile adminProfile = new UserProfile("mci-supervisor", "102", asList("10"));

        return new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(MCI_USER_GROUP, MCI_ADMIN, MCI_APPROVER, FACILITY_GROUP, PROVIDER_GROUP)),
                asList(adminProfile));
    }
}