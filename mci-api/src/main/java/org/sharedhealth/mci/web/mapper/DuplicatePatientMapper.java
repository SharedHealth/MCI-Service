package org.sharedhealth.mci.web.mapper;

import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.datastax.driver.core.utils.UUIDs.timeBased;

@Component
public class DuplicatePatientMapper {

    public List<DuplicatePatient> map(List<DuplicatePatientData> duplicateDataList) {
        List<DuplicatePatient> duplicates = new ArrayList<>();
        for (DuplicatePatientData duplicateData : duplicateDataList) {
            map(duplicateData, duplicates);
        }
        return duplicates;
    }

    private List<DuplicatePatient> map(DuplicatePatientData duplicateData, List<DuplicatePatient> duplicates) {
        String healthId1 = duplicateData.getHealthId1();
        String healthId2 = duplicateData.getHealthId2();
        Set<String> reasons = duplicateData.getReasons();
        Catchment catchment1 = duplicateData.getCatchment1();
        Catchment catchment2 = duplicateData.getCatchment2();

        buildDuplicates(catchment1, healthId1, healthId2, reasons, duplicates);
        if (!catchment1.equals(catchment2)) {
            buildDuplicates(catchment2, healthId2, healthId1, reasons, duplicates);
        }
        return duplicates;
    }

    private void buildDuplicates(Catchment catchment, String healthId1, String healthId2, Set<String> reasons,
                                 List<DuplicatePatient> duplicates) {
        for (String catchmentId : catchment.getAllIds()) {
            DuplicatePatient duplicate = new DuplicatePatient(catchmentId, healthId1, healthId2, reasons, timeBased());
            duplicates.add(duplicate);
        }
    }
}
