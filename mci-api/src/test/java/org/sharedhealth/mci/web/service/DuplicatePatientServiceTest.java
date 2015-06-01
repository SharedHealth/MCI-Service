package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.model.DuplicatePatient;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.asSet;
import static org.sharedhealth.mci.web.utils.MCIConstants.DUPLICATION_ACTION_IGNORE;
import static org.sharedhealth.mci.web.utils.MCIConstants.DUPLICATION_ACTION_MERGE;

public class DuplicatePatientServiceTest {

    @Rule
    public ExpectedException expectedEx = none();

    @Mock
    private DuplicatePatientRepository duplicatePatientRepository;
    @Mock
    private PatientService patientService;

    private DuplicatePatientService duplicatePatientService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        duplicatePatientService = new DuplicatePatientService(patientService, duplicatePatientRepository);
    }

    @Test
    public void shouldFindAllByCatchment() {
        Catchment catchment = new Catchment("102030");
        when(duplicatePatientRepository.findAllByCatchment(catchment)).thenReturn(buildDuplicatePatients());
        List<DuplicatePatientData> duplicatePatientDataList = duplicatePatientService.findAllByCatchment(catchment);

        assertTrue(isNotEmpty(duplicatePatientDataList));
        assertEquals(5, duplicatePatientDataList.size());
    }

    private List<DuplicatePatient> buildDuplicatePatients() {
        List<DuplicatePatient> duplicatePatients = new ArrayList<>();
        duplicatePatients.add(new DuplicatePatient("A102030", "99001", "99002", asSet("nid", "phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient("A102030", "99003", "99004", asSet("phoneNo"), timeBased()));
        duplicatePatients.add(new DuplicatePatient("A102030", "99005", "99006", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient("A102030", "99007", "99008", asSet("nid"), timeBased()));
        duplicatePatients.add(new DuplicatePatient("A102030", "99009", "99010", asSet("nid", "phoneNo"), timeBased()));
        return duplicatePatients;
    }

    @Test
    public void shouldIgnoreDuplicates() {
        PatientData patient1 = new PatientData();
        patient1.setHealthId("100");
        PatientData patient2 = new PatientData();
        patient2.setHealthId("200");

        DuplicatePatientMergeData data = new DuplicatePatientMergeData();
        data.setAction(DUPLICATION_ACTION_IGNORE);
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

        when(patientService.findByHealthId(healthId1)).thenReturn(patient1);
        when(patientService.findByHealthId(healthId2)).thenReturn(patient2);
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
}