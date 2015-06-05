package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.utils.LuhnChecksumGenerator;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.web.model.HealthId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class HealthIdServiceTest {

    MCIProperties mciProperties;

    @Mock
    HealthIdRepository healthIdRepository;

    @Mock
    LuhnChecksumGenerator checksumGenerator;

    @Before
    public void setUp() throws Exception {
        mciProperties = new MCIProperties();
        mciProperties.setInvalidHidPattern("^[^9]|^.[^89]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        mciProperties.setMciStartHid("9800000000");
        mciProperties.setMciEndHid("9999999999");
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
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setInvalidHidPattern("^[45]\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator);
        assertEquals(78, healthIdService.generate(0, 99));
    }

    @Test
    public void shouldExecuteCorrectMCIHIDRegex() {
        long start = 9800005790L, end = 9800005792L;
        MCIProperties testProperties = new MCIProperties();
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setInvalidHidPattern("^[^9]|^.[^89]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator);
        assertEquals(0, healthIdService.generate(9800005790L, 9800005792L));
    }

    @Test
    public void shouldSaveValidHids() {
        when(healthIdRepository.saveHealthId(any(HealthId.class))).thenReturn(HealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        MCIProperties testProperties = new MCIProperties();
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setInvalidHidPattern("^[45]\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator);
        assertEquals(78, healthIdService.generate(0, 99));

        ArgumentCaptor<HealthId> healthIdArgumentCaptor = ArgumentCaptor.forClass(HealthId.class);
        verify(healthIdRepository, times(78)).saveHealthId(healthIdArgumentCaptor.capture());
        verify(checksumGenerator, times(78)).generate(any(String.class));
        assertTrue(String.valueOf(healthIdArgumentCaptor.getValue().getHid()).endsWith("1"));
    }

    @Test
    public void shouldNotGenerateAnyHidsIfStartHidIsGreaterThanEndHid() {
        MCIProperties testProperties = new MCIProperties();
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setInvalidHidPattern("^[45]\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator);
        assertEquals(0, healthIdService.generate(100, 99));
    }

    @Test
    public void should10kBlockIdsForMCIService() {
        ArrayList<HealthId> result = new ArrayList<>();
        result.add(new HealthId("898998"));
        result.add(new HealthId("898999"));
        when(healthIdRepository.getNextBlock()).thenReturn(result);

        HealthIdService healthIdService = new HealthIdService(mciProperties, healthIdRepository, checksumGenerator);
        List<HealthId> nextBlock = healthIdService.getNextBlock();
        assertEquals(2, nextBlock.size());
    }
}