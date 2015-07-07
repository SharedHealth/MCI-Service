package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.MarkerRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientFeedRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMergeData;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.PatientSummaryData;
import org.sharedhealth.mci.web.model.DuplicatePatient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.asSet;
import static org.sharedhealth.mci.web.utils.MCIConstants.DUPLICATION_ACTION_MERGE;
import static org.sharedhealth.mci.web.utils.MCIConstants.DUPLICATION_ACTION_RETAIN_ALL;

public class DuplicatePatientServiceTest {

    @Rule
    public ExpectedException expectedEx = none();

    @Mock
    private DuplicatePatientRepository duplicatePatientRepository;
    @Mock
    private PatientFeedRepository feedRepository;
    @Mock
    private MarkerRepository markerRepository;
    @Mock
    private PatientRepository patientRepository;

    private DuplicatePatientService duplicatePatientService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        DuplicatePatientMapper duplicatePatientMapper = new DuplicatePatientMapper(patientRepository, new PatientMapper());
        duplicatePatientService = new DuplicatePatientService(duplicatePatientRepository, duplicatePatientMapper);
    }

    @Test
    public void shouldFindAllByCatchment() {
        Catchment catchment = new Catchment("102030");
        Address address = new Address("10", "20", "30");
        when(duplicatePatientRepository.findByCatchment(catchment, null, null, 25)).thenReturn(buildDuplicatePatients());
        when(patientRepository.findByHealthId("99001")).thenReturn(buildPatientData("99001", "A1", "B1", address));
        when(patientRepository.findByHealthId("99002")).thenReturn(buildPatientData("99002", "A2", "B2", address));
        when(patientRepository.findByHealthId("99003")).thenReturn(buildPatientData("99003", "A3", "B3", address));
        when(patientRepository.findByHealthId("99004")).thenReturn(buildPatientData("99004", "A4", "B4", address));
        when(patientRepository.findByHealthId("99005")).thenReturn(buildPatientData("99005", "A5", "B5", address));
        when(patientRepository.findByHealthId("99006")).thenReturn(buildPatientData("99006", "A6", "B6", address));
        List<DuplicatePatientData> duplicatePatientDataList = duplicatePatientService.findAllByCatchment(catchment, null, null, 25);

        assertTrue(isNotEmpty(duplicatePatientDataList));
        assertEquals(3, duplicatePatientDataList.size());

        assertDuplicateData(duplicatePatientDataList.get(0), buildPatientData("99001", "A1", "B1", address),
                buildPatientData("99002", "A2", "B2", address), asSet("nid", "phoneNo"));
        assertDuplicateData(duplicatePatientDataList.get(1), buildPatientData("99003", "A3", "B3", address),
                buildPatientData("99004", "A4", "B4", address), asSet("phoneNo"));
        assertDuplicateData(duplicatePatientDataList.get(2), buildPatientData("99005", "A5", "B5", address),
                buildPatientData("99006", "A6", "B6", address), asSet("nid"));
    }

    private PatientData buildPatientData(String healthId, String givenName, String surname, Address address) {
        PatientData patientData = new PatientData();
        patientData.setHealthId(healthId);
        patientData.setGivenName(givenName);
        patientData.setSurName(surname);
        patientData.setAddress(address);
        return patientData;
    }

    private List<DuplicatePatient> buildDuplicatePatients() {
        List<DuplicatePatient> duplicatePatients = new ArrayList<>();
        duplicatePatients.add(new DuplicatePatient("A102030", "99001", "99002", asSet("nid", "phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient("A102030", "99003", "99004", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient("A102030", "99005", "99006", asSet("nid"), timeBased()));
        return duplicatePatients;
    }

    private void assertDuplicateData(DuplicatePatientData duplicatePatient, PatientData patientData1, PatientData patientData2,
                                     Set<String> reasons) {
        assertPatient(duplicatePatient.getPatient1(), patientData1);
        assertPatient(duplicatePatient.getPatient2(), patientData2);
        assertEquals(reasons, duplicatePatient.getReasons());
    }

    private void assertPatient(PatientSummaryData expected, PatientData actual) {
        assertNotNull(actual);
        assertEquals(expected.getHealthId(), actual.getHealthId());
        assertEquals(expected.getGivenName(), actual.getGivenName());
        assertEquals(expected.getSurName(), actual.getSurName());
        assertEquals(expected.getAddress(), actual.getAddress());
    }

    @Test
    public void shouldIgnoreDuplicates() {
        PatientData patient1 = new PatientData();
        patient1.setHealthId("100");
        PatientData patient2 = new PatientData();
        patient2.setHealthId("200");

        DuplicatePatientMergeData data = new DuplicatePatientMergeData();
        data.setAction(DUPLICATION_ACTION_RETAIN_ALL);
        data.setPatient1(patient1);
        data.setPatient2(patient2);
        duplicatePatientService.processDuplicates(data);

        ArgumentCaptor<PatientData> argument1 = ArgumentCaptor.forClass(PatientData.class);
        ArgumentCaptor<PatientData> argument2 = ArgumentCaptor.forClass(PatientData.class);
        ArgumentCaptor<Boolean> argument3 = ArgumentCaptor.forClass(Boolean.class);
        verify(duplicatePatientRepository).processDuplicates(argument1.capture(), argument2.capture(), argument3.capture());
        assertEquals(patient1, argument1.getValue());
        assertEquals(patient2, argument2.getValue());
        assertEquals(false, argument3.getValue());
    }

    @Test
    public void shouldMergePatients() {
        String healthId1 = "100";
        String healthId2 = "200";

        PatientData patient1 = new PatientData();
        patient1.setHealthId(healthId1);
        patient1.setMergedWith(healthId2);
        patient1.setAddress(new Address("10", "20", "30"));
        patient1.setActive(false);

        PatientData patient2 = new PatientData();
        patient2.setHealthId(healthId2);
        patient2.setAddress(new Address("10", "20", "30"));
        patient2.setActive(true);

        when(duplicatePatientRepository.findByCatchmentAndHealthIds(patient1.getCatchment(), healthId1, healthId2))
                .thenReturn(asList(new DuplicatePatient()));

        duplicatePatientService.processDuplicates(buildDuplicatePatientMergeData(patient1, patient2));

        ArgumentCaptor<PatientData> argument1 = ArgumentCaptor.forClass(PatientData.class);
        ArgumentCaptor<PatientData> argument2 = ArgumentCaptor.forClass(PatientData.class);
        ArgumentCaptor<Boolean> argument3 = ArgumentCaptor.forClass(Boolean.class);
        verify(duplicatePatientRepository).processDuplicates(argument1.capture(), argument2.capture(), argument3.capture());
        assertEquals(patient1, argument1.getValue());
        assertEquals(patient2, argument2.getValue());
        assertEquals(true, argument3.getValue());
    }

    @Test
    public void shouldNotMergePatientsIfPatient1IsActive() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Patient 1 [hid: 100] is not retired. Cannot merge.");

        PatientData patient1 = new PatientData();
        patient1.setHealthId("100");
        patient1.setActive(true);
        PatientData patient2 = new PatientData();
        duplicatePatientService.processDuplicates(buildDuplicatePatientMergeData(patient1, patient2));
    }

    @Test
    public void shouldNotMergePatientsIfPatient1IsNotMergedWithPatient2() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("'merge_with' field of Patient 1 [hid: 100] is not set properly. Cannot merge.");

        PatientData patient1 = new PatientData();
        patient1.setHealthId("100");
        patient1.setActive(false);
        PatientData patient2 = new PatientData();
        duplicatePatientService.processDuplicates(buildDuplicatePatientMergeData(patient1, patient2));
    }

    @Test
    public void shouldNotMergePatientsIfPatient2IsRetired() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Patient 2 [hid: 200] is retired. Cannot merge.");

        PatientData patient1 = new PatientData();
        patient1.setHealthId("100");
        patient1.setActive(false);
        patient1.setMergedWith("200");
        PatientData patient2 = new PatientData();
        patient2.setHealthId("200");
        patient2.setActive(false);
        duplicatePatientService.processDuplicates(buildDuplicatePatientMergeData(patient1, patient2));
    }

    private DuplicatePatientMergeData buildDuplicatePatientMergeData(PatientData patient1, PatientData patient2) {
        DuplicatePatientMergeData data = new DuplicatePatientMergeData();
        data.setAction(DUPLICATION_ACTION_MERGE);
        data.setPatient1(patient1);
        data.setPatient2(patient2);
        return data;
    }

    @Test
    public void shouldRetainPatients() {
        String healthId1 = "100";
        String healthId2 = "200";

        PatientData patient1 = new PatientData();
        patient1.setHealthId(healthId1);
        patient1.setAddress(new Address("10", "20", "30"));
        patient1.setActive(true);

        PatientData patient2 = new PatientData();
        patient2.setHealthId(healthId2);
        patient2.setAddress(new Address("10", "20", "30"));
        patient2.setActive(true);

        when(duplicatePatientRepository.findByCatchmentAndHealthIds(patient1.getCatchment(), healthId1, healthId2))
                .thenReturn(asList(new DuplicatePatient()));

        duplicatePatientService.processDuplicates(buildDuplicatePatientRetainData(patient1, patient2));

        ArgumentCaptor<PatientData> argument1 = ArgumentCaptor.forClass(PatientData.class);
        ArgumentCaptor<PatientData> argument2 = ArgumentCaptor.forClass(PatientData.class);
        ArgumentCaptor<Boolean> argument3 = ArgumentCaptor.forClass(Boolean.class);
        verify(duplicatePatientRepository).processDuplicates(argument1.capture(), argument2.capture(), argument3.capture());
        assertEquals(patient1, argument1.getValue());
        assertEquals(patient2, argument2.getValue());
        assertEquals(false, argument3.getValue());
    }

    @Test
    public void shouldNotRetainPatientsIfPatient1IsRetired() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Patient 1 [hid: 100] and/or patient 2 [hid: 200] are/is retired. Cannot retain.");

        PatientData patient1 = new PatientData();
        patient1.setHealthId("100");
        patient1.setActive(false);
        PatientData patient2 = new PatientData();
        patient2.setHealthId("200");
        patient2.setActive(true);
        duplicatePatientService.processDuplicates(buildDuplicatePatientRetainData(patient1, patient2));
    }

    @Test
    public void shouldNotRetainPatientsIfPatient2IsRetired() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Patient 1 [hid: 100] and/or patient 2 [hid: 200] are/is retired. Cannot retain.");

        PatientData patient1 = new PatientData();
        patient1.setHealthId("100");
        patient1.setActive(true);
        PatientData patient2 = new PatientData();
        patient2.setHealthId("200");
        patient2.setActive(false);
        duplicatePatientService.processDuplicates(buildDuplicatePatientRetainData(patient1, patient2));
    }

    private DuplicatePatientMergeData buildDuplicatePatientRetainData(PatientData patient1, PatientData patient2) {
        DuplicatePatientMergeData data = new DuplicatePatientMergeData();
        data.setAction(DUPLICATION_ACTION_RETAIN_ALL);
        data.setPatient1(patient1);
        data.setPatient2(patient2);
        return data;
    }
}