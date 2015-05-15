package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.asSet;

public class DuplicatePatientServiceTest {

    @Mock
    private DuplicatePatientRepository duplicatePatientRepository;

    private DuplicatePatientService duplicatePatientService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        duplicatePatientService = new DuplicatePatientService(duplicatePatientRepository);
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
}