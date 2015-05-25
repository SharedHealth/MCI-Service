package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMergeData;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.asSet;
import static org.sharedhealth.mci.web.utils.MCIConstants.DUPLICATION_ACTION_IGNORE;
import static org.sharedhealth.mci.web.utils.MCIConstants.DUPLICATION_ACTION_MERGE;

public class DuplicatePatientServiceTest {

    @Mock
    private DuplicatePatientRepository duplicatePatientRepository;
    @Mock
    private PatientRepository patientRepository;

    private DuplicatePatientService duplicatePatientService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        duplicatePatientService = new DuplicatePatientService(patientRepository, duplicatePatientRepository);
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
    public void shouldIgnorePatients() {
        PatientData patient1 = new PatientData();
        patient1.setHealthId("100");
        PatientData patient2 = new PatientData();
        patient2.setHealthId("200");

        DuplicatePatientMergeData data = new DuplicatePatientMergeData();
        data.setAction(DUPLICATION_ACTION_IGNORE);
        data.setPatient1(patient1);
        data.setPatient2(patient2);
        duplicatePatientService.mergeOrIgnore(data);

        ArgumentCaptor<PatientData> argument1 = ArgumentCaptor.forClass(PatientData.class);
        ArgumentCaptor<PatientData> argument2 = ArgumentCaptor.forClass(PatientData.class);
        verify(duplicatePatientRepository).ignore(argument1.capture(), argument2.capture());
        assertEquals(patient1, argument1.getValue());
        assertEquals(patient2, argument2.getValue());
    }

    @Test
    public void shouldMergePatients() {
        PatientData patient1 = new PatientData();
        patient1.setHealthId("100");
        patient1.setActive(false);
        PatientData patient2 = new PatientData();
        patient2.setHealthId("200");

        DuplicatePatientMergeData data = new DuplicatePatientMergeData();
        data.setAction(DUPLICATION_ACTION_MERGE);
        data.setPatient1(patient1);
        data.setPatient2(patient2);
        duplicatePatientService.mergeOrIgnore(data);

        ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(patientRepository).update(argument.capture());
        assertEquals(asList(patient1, patient2), argument.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotMergePatientsIfPatient1IsActive() {
        PatientData patient1 = new PatientData();
        patient1.setHealthId("100");
        patient1.setActive(true);
        PatientData patient2 = new PatientData();
        patient2.setHealthId("200");

        DuplicatePatientMergeData data = new DuplicatePatientMergeData();
        data.setAction(DUPLICATION_ACTION_MERGE);
        data.setPatient1(patient1);
        data.setPatient2(patient2);
        duplicatePatientService.mergeOrIgnore(data);
    }
}