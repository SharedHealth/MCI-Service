package org.sharedhealth.mci.web.mapper;

import org.junit.Test;
import org.sharedhealth.mci.web.model.DuplicatePatient;

import java.util.*;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.*;

public class DuplicatePatientMapperTest {

    @Test
    public void shouldMapDuplicatesWhenCatchmentsAreUnique() {
        String healthId1 = "healthId1";
        String healthId2 = "healthId2";
        String healthId3 = "healthId3";
        Catchment catchment1 = new Catchment("1011121314");
        Catchment catchment2 = new Catchment("2021222324");
        Catchment catchment3 = new Catchment("3031323334");
        Set<String> reasons = new HashSet<>(asList("NID", "PHONE"));

        List<DuplicatePatientData> duplicateDataList = new ArrayList<>();

        DuplicatePatientData duplicateData1 = new DuplicatePatientData();
        duplicateData1.setHealthId1(healthId1);
        duplicateData1.setHealthId2(healthId2);
        duplicateData1.setCatchment1(catchment1);
        duplicateData1.setCatchment2(catchment2);
        duplicateData1.setReasons(reasons);

        DuplicatePatientData duplicateData2 = new DuplicatePatientData();
        duplicateData2.setHealthId1(healthId1);
        duplicateData2.setHealthId2(healthId3);
        duplicateData2.setCatchment1(catchment1);
        duplicateData2.setCatchment2(catchment3);
        duplicateData2.setReasons(reasons);

        duplicateDataList.add(duplicateData1);
        duplicateDataList.add(duplicateData2);

        List<DuplicatePatient> duplicates = new DuplicatePatientMapper().map(duplicateDataList);
        assertTrue(isNotEmpty(duplicates));
        assertEquals(16, duplicates.size());

        assertDuplicates(catchment1.getAllIds().get(0), healthId1, healthId2, reasons, duplicates.get(0));
        assertDuplicates(catchment1.getAllIds().get(1), healthId1, healthId2, reasons, duplicates.get(1));
        assertDuplicates(catchment1.getAllIds().get(2), healthId1, healthId2, reasons, duplicates.get(2));
        assertDuplicates(catchment1.getAllIds().get(3), healthId1, healthId2, reasons, duplicates.get(3));

        assertDuplicates(catchment2.getAllIds().get(0), healthId2, healthId1, reasons, duplicates.get(4));
        assertDuplicates(catchment2.getAllIds().get(1), healthId2, healthId1, reasons, duplicates.get(5));
        assertDuplicates(catchment2.getAllIds().get(2), healthId2, healthId1, reasons, duplicates.get(6));
        assertDuplicates(catchment2.getAllIds().get(3), healthId2, healthId1, reasons, duplicates.get(7));

        assertDuplicates(catchment1.getAllIds().get(0), healthId1, healthId3, reasons, duplicates.get(8));
        assertDuplicates(catchment1.getAllIds().get(1), healthId1, healthId3, reasons, duplicates.get(9));
        assertDuplicates(catchment1.getAllIds().get(2), healthId1, healthId3, reasons, duplicates.get(10));
        assertDuplicates(catchment1.getAllIds().get(3), healthId1, healthId3, reasons, duplicates.get(11));

        assertDuplicates(catchment3.getAllIds().get(0), healthId3, healthId1, reasons, duplicates.get(12));
        assertDuplicates(catchment3.getAllIds().get(1), healthId3, healthId1, reasons, duplicates.get(13));
        assertDuplicates(catchment3.getAllIds().get(2), healthId3, healthId1, reasons, duplicates.get(14));
        assertDuplicates(catchment3.getAllIds().get(3), healthId3, healthId1, reasons, duplicates.get(15));
    }

    @Test
    public void shouldMapDuplicatesWhenCatchmentsAreNotUnique() {
        String healthId1 = "healthId1";
        String healthId2 = "healthId2";
        String healthId3 = "healthId3";
        Catchment catchment = new Catchment("1011121314");
        Set<String> reasons = new HashSet<>(asList("NID", "PHONE"));

        List<DuplicatePatientData> duplicateDataList = new ArrayList<>();

        DuplicatePatientData duplicateData = new DuplicatePatientData();
        duplicateData.setHealthId1(healthId1);
        duplicateData.setHealthId2(healthId2);
        duplicateData.setCatchment1(catchment);
        duplicateData.setCatchment2(catchment);
        duplicateData.setReasons(reasons);

        duplicateDataList.add(duplicateData);

        List<DuplicatePatient> duplicates = new DuplicatePatientMapper().map(duplicateDataList);
        assertTrue(isNotEmpty(duplicates));
        assertEquals(8, duplicates.size());

        assertDuplicates(catchment.getAllIds().get(0), healthId1, healthId2, reasons, duplicates.get(0));
        assertDuplicates(catchment.getAllIds().get(1), healthId1, healthId2, reasons, duplicates.get(1));
        assertDuplicates(catchment.getAllIds().get(2), healthId1, healthId2, reasons, duplicates.get(2));
        assertDuplicates(catchment.getAllIds().get(3), healthId1, healthId2, reasons, duplicates.get(3));

        assertDuplicates(catchment.getAllIds().get(0), healthId2, healthId1, reasons, duplicates.get(4));
        assertDuplicates(catchment.getAllIds().get(1), healthId2, healthId1, reasons, duplicates.get(5));
        assertDuplicates(catchment.getAllIds().get(2), healthId2, healthId1, reasons, duplicates.get(6));
        assertDuplicates(catchment.getAllIds().get(3), healthId2, healthId1, reasons, duplicates.get(7));
    }

    private void assertDuplicates(String catchmentId, String healthId1, String healthId2, Set<String> reasons, DuplicatePatient duplicate) {
        assertEquals(catchmentId, duplicate.getCatchment_id());
        assertEquals(healthId1, duplicate.getHealth_id1());
        assertEquals(healthId2, duplicate.getHealth_id2());
        assertEquals(reasons, duplicate.getReasons());
        UUID createdAt = duplicate.getCreated_at();
        assertNotNull(createdAt);
        assertEquals(1, createdAt.version());
    }
}