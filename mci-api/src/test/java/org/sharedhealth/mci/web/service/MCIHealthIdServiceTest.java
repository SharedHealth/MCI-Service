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
import org.sharedhealth.mci.web.model.GeneratedHidRange;
import org.sharedhealth.mci.web.model.MciHealthId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.service.HealthIdService.MCI_ORG_CODE;

@RunWith(MockitoJUnitRunner.class)
public class MCIHealthIdServiceTest {
    private MCIProperties mciProperties;
    @Mock
    private HealthIdRepository healthIdRepository;
    @Mock
    private LuhnChecksumGenerator checksumGenerator;
    @Mock
    private GeneratedHidRangeService generatedHidRangeService;

    @Before
    public void setUp() throws Exception {
        mciProperties = new MCIProperties();
        mciProperties.setInvalidHidPattern("^[^9]|^.[^89]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        mciProperties.setMciStartHid("9800000000");
        mciProperties.setMciEndHid("9999999999");
        mciProperties.setHealthIdBlockSize("10");
        mciProperties.setHealthIdBlockSizeThreshold("1");
        initMocks(this);
    }

    @Test
    public void validIdsStartWith9() {
        Pattern invalidPattern = Pattern.compile(mciProperties.getInvalidHidPattern());
        assertTrue(invalidPattern.matcher("8801543886").find());
        assertFalse(invalidPattern.matcher("9801543886").find());
    }

    @Test
    public void validIdsStartWith98Or99() {
        Pattern invalidPattern = Pattern.compile(mciProperties.getInvalidHidPattern());
        assertTrue(invalidPattern.matcher("9101543886").find());
        assertTrue(invalidPattern.matcher("98000034730").find());
        assertFalse(invalidPattern.matcher("9801543886").find());
        assertFalse(invalidPattern.matcher("9901543886").find());
    }

    @Test
    public void validIdsCannotHave4RepeatedChars() {
        Pattern invalidPattern = Pattern.compile(mciProperties.getInvalidHidPattern());
        assertTrue(invalidPattern.matcher("98015888861").find());
        assertFalse(invalidPattern.matcher("9901548886").find());
    }

    @Test
    public void validIdsCannotHave2OrMoreInstancesOf3RepeatedChars() {
        Pattern invalidPattern = Pattern.compile(mciProperties.getInvalidHidPattern());
        assertFalse(invalidPattern.matcher("9801588861").find());
        assertTrue(invalidPattern.matcher("9991548886").find());
        assertTrue(invalidPattern.matcher("9991118126").find());
        assertTrue(invalidPattern.matcher("9811115255").find());
    }

    @Test
    public void validIdsCannotHaveMoreThan10Chars() {
        Pattern invalidPattern = Pattern.compile(mciProperties.getInvalidHidPattern());
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
        testProperties.setInvalidHidPattern("^(105|104)\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        assertEquals(80, healthIdService.generateAll());
    }

    @Test
    public void shouldExecuteCorrectMCIHIDRegex() {
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciStartHid("9800005790");
        testProperties.setMciEndHid("9800005792");
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setInvalidHidPattern("^[^9]|^.[^89]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        assertEquals(0, healthIdService.generateAll());
    }

    @Test
    public void shouldSaveValidHids() {
        when(healthIdRepository.saveHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciStartHid("1000");
        testProperties.setMciEndHid("1099");
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setInvalidHidPattern("^(105|104)\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        assertEquals(80, healthIdService.generateAll());

        ArgumentCaptor<MciHealthId> healthIdArgumentCaptor = ArgumentCaptor.forClass(MciHealthId.class);
        verify(healthIdRepository, times(80)).saveHealthId(healthIdArgumentCaptor.capture());
        verify(checksumGenerator, times(80)).generate(any(String.class));
        assertTrue(String.valueOf(healthIdArgumentCaptor.getValue().getHid()).endsWith("1"));
    }

    @Test
    public void shouldSaveTheGeneratedRangeFromGivenRange() throws Exception {
        MCIProperties testProperties = new MCIProperties();
        testProperties.setInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setMciStartHid("1000");
        testProperties.setMciEndHid("1099");

        when(healthIdRepository.saveHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidRangeService.saveGeneratedHidRange(any(GeneratedHidRange.class))).thenReturn(null);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        healthIdService.generateAll();

        ArgumentCaptor<GeneratedHidRange> argument = ArgumentCaptor.forClass(GeneratedHidRange.class);
        verify(generatedHidRangeService, times(1)).saveGeneratedHidRange(argument.capture());
        GeneratedHidRange passedHidRange = argument.getValue();
        assertEquals(10, passedHidRange.getBlockBeginsAt().longValue());
        assertEquals(1000, passedHidRange.getBeginsAt().longValue());
        assertEquals("MCI", passedHidRange.getAllocatedFor());
        assertEquals(1099, passedHidRange.getEndsAt().longValue());
        assertNull(passedHidRange.getRequestedBy());
    }

    @Test
    public void shouldNotSaveRangeIfNoHIDsAreGenerated() throws Exception {
        MCIProperties testProperties = new MCIProperties();
        testProperties.setInvalidHidPattern("^(105|104)\\d*$");
        testProperties.setMciStartHid("1040");
        testProperties.setMciEndHid("1050");

        when(healthIdRepository.saveHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        healthIdService.generateAll();

        verify(generatedHidRangeService, never()).saveGeneratedHidRange(any(GeneratedHidRange.class));
    }

    @Test
    public void shouldNotGenerateAnyHidsIfStartHidIsGreaterThanEndHid() {
        MCIProperties testProperties = new MCIProperties();
        testProperties.setMciStartHid("1000");
        testProperties.setMciEndHid("999");
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setInvalidHidPattern("^(105|104)\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        assertEquals(0, healthIdService.generateAll());
    }

    @Test
    public void shouldFetchBlockIdsForMCIService() {
        ArrayList<MciHealthId> result = new ArrayList<>();
        result.add(new MciHealthId("898998"));
        result.add(new MciHealthId("898999"));
        when(healthIdRepository.getNextBlock(mciProperties.getHealthIdBlockSize())).thenReturn(result);

        HealthIdService healthIdService = new HealthIdService(mciProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        List<MciHealthId> nextBlock = healthIdService.getNextBlock();
        verify(healthIdRepository).getNextBlock(mciProperties.getHealthIdBlockSize());
        assertEquals(2, nextBlock.size());
    }

    @Test
    public void shouldGenerateValidHealthIdsForGivenBlockSize() throws Exception {
        long start = 1000;
        long blockSize = 100;
        MCIProperties testProperties = new MCIProperties();
        testProperties.setInvalidHidPattern("^(105|104)\\d*$");

        when(healthIdRepository.saveHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        healthIdService.generateBlock(start, blockSize);

        verify(healthIdRepository, times(100)).saveHealthId(any(MciHealthId.class));
        verify(checksumGenerator, times(100)).generate(any(String.class));

        verify(healthIdRepository, times(1)).saveHealthId(new MciHealthId("10001"));
        verify(healthIdRepository, never()).saveHealthId(new MciHealthId("10401"));
        verify(healthIdRepository, never()).saveHealthId(new MciHealthId("10501"));
        verify(healthIdRepository, times(1)).saveHealthId(new MciHealthId("10191"));
    }

    @Test
    public void shouldCalculateRangeAtSaveForGivenBlock() throws Exception {
        long start = 1000;
        long blockSize = 50;
        MCIProperties testProperties = new MCIProperties();
        testProperties.setInvalidHidPattern("^(105|104)\\d*$");

        when(healthIdRepository.saveHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidRangeService.saveGeneratedHidRange(any(GeneratedHidRange.class))).thenReturn(null);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        healthIdService.generateBlock(start, blockSize);

        ArgumentCaptor<GeneratedHidRange> argument = ArgumentCaptor.forClass(GeneratedHidRange.class);
        verify(generatedHidRangeService, times(1)).saveGeneratedHidRange(argument.capture());
        GeneratedHidRange passedHidRange = argument.getValue();
        assertEquals(10, passedHidRange.getBlockBeginsAt().longValue());
        assertEquals(start, passedHidRange.getBeginsAt().longValue());
        assertEquals(1069, passedHidRange.getEndsAt().longValue());
        assertEquals(MCI_ORG_CODE, passedHidRange.getAllocatedFor());
        assertNull(passedHidRange.getRequestedBy());
    }

    @Test
    public void shouldIdentifyStartOfRangeFromPreGeneratedRange() throws Exception {
        long start = 1000;
        long blockSize = 20;
        MCIProperties testProperties = new MCIProperties();
        testProperties.setInvalidHidPattern("^(105|104)\\d*$");
        GeneratedHidRange generatedHidRange = new GeneratedHidRange(10L, 1000L, 1069L, MCI_ORG_CODE, null);
        
        when(generatedHidRangeService.getPreGeneratedHidRanges(10L)).thenReturn(asList(generatedHidRange));
        when(healthIdRepository.saveHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);
        when(generatedHidRangeService.saveGeneratedHidRange(any(GeneratedHidRange.class))).thenReturn(null);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        healthIdService.generateBlock(start, blockSize);

        verify(generatedHidRangeService,times(1)).getPreGeneratedHidRanges(10L);
        ArgumentCaptor<GeneratedHidRange> argument = ArgumentCaptor.forClass(GeneratedHidRange.class);
        verify(generatedHidRangeService, times(1)).saveGeneratedHidRange(argument.capture());
        GeneratedHidRange passedHidRange = argument.getValue();

        assertEquals(10, passedHidRange.getBlockBeginsAt().longValue());
        assertEquals(1070, passedHidRange.getBeginsAt().longValue());
        assertEquals(1089, passedHidRange.getEndsAt().longValue());
        assertEquals(MCI_ORG_CODE, passedHidRange.getAllocatedFor());
        assertNull(passedHidRange.getRequestedBy());
    }

    @Test
    public void shouldNotSaveRangeIfNoHIDsGeneratedInBlock() throws Exception {
        long start = 1040;
        long blockSize = 0;
        MCIProperties testProperties = new MCIProperties();
        testProperties.setInvalidHidPattern("^(105|104)\\d*$");

        when(healthIdRepository.saveHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        healthIdService.generateBlock(start, blockSize);

        verify(generatedHidRangeService, never()).saveGeneratedHidRange(any(GeneratedHidRange.class));
    }

    @Test
    public void shouldMarkHidUsed() {
        MciHealthId MciHealthId = new MciHealthId("898998");
        doNothing().when(healthIdRepository).removedUsedHid(any(MciHealthId.class));
        HealthIdService healthIdService = new HealthIdService(mciProperties, healthIdRepository, checksumGenerator, generatedHidRangeService);
        healthIdService.markUsed(MciHealthId);
        verify(healthIdRepository).removedUsedHid(MciHealthId);
    }

}