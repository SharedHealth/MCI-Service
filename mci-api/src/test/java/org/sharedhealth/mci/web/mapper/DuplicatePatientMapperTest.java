package org.sharedhealth.mci.web.mapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.deduplication.config.model.DuplicatePatient;
import org.sharedhealth.mci.deduplication.config.model.DuplicatePatientData;
import org.sharedhealth.mci.deduplication.config.model.DuplicatePatientMapper;
import org.sharedhealth.mci.domain.model.Address;
import org.sharedhealth.mci.domain.model.Catchment;
import org.sharedhealth.mci.domain.model.PatientMapper;
import org.sharedhealth.mci.domain.model.PatientSummaryData;
import org.sharedhealth.mci.domain.repository.PatientRepository;

import java.util.*;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicatePatientMapperTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PatientMapper patientMapper;

    private DuplicatePatientMapper duplicatePatientMapper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        duplicatePatientMapper = new DuplicatePatientMapper(patientRepository, patientMapper);
    }

    @Test
    public void shouldMapDuplicatesWhenCatchmentsAreUnique() {
        Address address1 = new Address("10", "11", "12", "13", "14", null);
        PatientSummaryData patientSummaryData1 = buildPatientSummaryData("healthId1", address1);
        Address address2 = new Address("20", "21", "22", "23", "24", null);
        PatientSummaryData patientSummaryData2 = buildPatientSummaryData("healthId2", address2);
        Address address3 = new Address("30", "31", "32", "33", "34", null);
        PatientSummaryData patientSummaryData3 = buildPatientSummaryData("healthId3", address3);

        Set<String> reasons = new HashSet<>(asList("NID", "PHONE"));

        List<DuplicatePatientData> duplicateDataList = new ArrayList<>();

        DuplicatePatientData duplicateData1 = new DuplicatePatientData();
        duplicateData1.setPatient1(patientSummaryData1);
        duplicateData1.setPatient2(patientSummaryData2);
        duplicateData1.setReasons(reasons);

        DuplicatePatientData duplicateData2 = new DuplicatePatientData();
        duplicateData2.setPatient1(patientSummaryData1);
        duplicateData2.setPatient2(patientSummaryData3);
        duplicateData2.setReasons(reasons);

        duplicateDataList.add(duplicateData1);
        duplicateDataList.add(duplicateData2);

        List<DuplicatePatient> duplicates = duplicatePatientMapper.mapToDuplicatePatientList(duplicateDataList);
        assertTrue(isNotEmpty(duplicates));
        assertEquals(16, duplicates.size());

        Catchment catchment1 = new Catchment(address1);
        Catchment catchment2 = new Catchment(address2);
        Catchment catchment3 = new Catchment(address3);

        assertDuplicates(catchment1.getAllIds().get(0), patientSummaryData1, patientSummaryData2, reasons, duplicates.get(0));
        assertDuplicates(catchment1.getAllIds().get(1), patientSummaryData1, patientSummaryData2, reasons, duplicates.get(1));
        assertDuplicates(catchment1.getAllIds().get(2), patientSummaryData1, patientSummaryData2, reasons, duplicates.get(2));
        assertDuplicates(catchment1.getAllIds().get(3), patientSummaryData1, patientSummaryData2, reasons, duplicates.get(3));

        assertDuplicates(catchment2.getAllIds().get(0), patientSummaryData2, patientSummaryData1, reasons, duplicates.get(4));
        assertDuplicates(catchment2.getAllIds().get(1), patientSummaryData2, patientSummaryData1, reasons, duplicates.get(5));
        assertDuplicates(catchment2.getAllIds().get(2), patientSummaryData2, patientSummaryData1, reasons, duplicates.get(6));
        assertDuplicates(catchment2.getAllIds().get(3), patientSummaryData2, patientSummaryData1, reasons, duplicates.get(7));

        assertDuplicates(catchment1.getAllIds().get(0), patientSummaryData1, patientSummaryData3, reasons, duplicates.get(8));
        assertDuplicates(catchment1.getAllIds().get(1), patientSummaryData1, patientSummaryData3, reasons, duplicates.get(9));
        assertDuplicates(catchment1.getAllIds().get(2), patientSummaryData1, patientSummaryData3, reasons, duplicates.get(10));
        assertDuplicates(catchment1.getAllIds().get(3), patientSummaryData1, patientSummaryData3, reasons, duplicates.get(11));

        assertDuplicates(catchment3.getAllIds().get(0), patientSummaryData3, patientSummaryData1, reasons, duplicates.get(12));
        assertDuplicates(catchment3.getAllIds().get(1), patientSummaryData3, patientSummaryData1, reasons, duplicates.get(13));
        assertDuplicates(catchment3.getAllIds().get(2), patientSummaryData3, patientSummaryData1, reasons, duplicates.get(14));
        assertDuplicates(catchment3.getAllIds().get(3), patientSummaryData3, patientSummaryData1, reasons, duplicates.get(15));
    }

    @Test
    public void shouldNotMapDuplicatesWhenCatchmentsAreSame() {
        Address address = new Address("10", "11", "12", "13", "14", null);
        PatientSummaryData patientSummaryData1 = buildPatientSummaryData("healthId1", address);
        PatientSummaryData patientSummaryData2 = buildPatientSummaryData("healthId2", address);
        Set<String> reasons = new HashSet<>(asList("NID", "PHONE"));

        DuplicatePatientData duplicateData = new DuplicatePatientData();
        duplicateData.setPatient1(patientSummaryData1);
        duplicateData.setPatient2(patientSummaryData2);
        duplicateData.setReasons(reasons);

        List<DuplicatePatient> duplicates = duplicatePatientMapper.mapToDuplicatePatientList(asList(duplicateData));
        assertTrue(isNotEmpty(duplicates));
        assertEquals(4, duplicates.size());

        Catchment catchment = new Catchment(address);
        assertDuplicates(catchment.getAllIds().get(0), patientSummaryData1, patientSummaryData2, reasons, duplicates.get(0));
        assertDuplicates(catchment.getAllIds().get(1), patientSummaryData1, patientSummaryData2, reasons, duplicates.get(1));
        assertDuplicates(catchment.getAllIds().get(2), patientSummaryData1, patientSummaryData2, reasons, duplicates.get(2));
        assertDuplicates(catchment.getAllIds().get(3), patientSummaryData1, patientSummaryData2, reasons, duplicates.get(3));
    }

    private PatientSummaryData buildPatientSummaryData(String healthId, Address address) {
        PatientSummaryData patientSummaryData1 = new PatientSummaryData();
        patientSummaryData1.setHealthId(healthId);
        patientSummaryData1.setAddress(address);
        return patientSummaryData1;
    }

    private void assertDuplicates(String catchmentId, PatientSummaryData patient1, PatientSummaryData patient2,
                                  Set<String> reasons, DuplicatePatient duplicate) {
        assertEquals(catchmentId, duplicate.getCatchment_id());
        assertEquals(patient1.getHealthId(), duplicate.getHealth_id1());
        assertEquals(patient2.getHealthId(), duplicate.getHealth_id2());
        assertEquals(reasons, duplicate.getReasons());
        UUID createdAt = duplicate.getCreated_at();
        assertNotNull(createdAt);
        assertEquals(1, createdAt.version());
    }
}