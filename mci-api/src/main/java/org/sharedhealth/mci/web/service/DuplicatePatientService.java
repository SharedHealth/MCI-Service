package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.utils.DateUtil;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DuplicatePatientService {

    private DuplicatePatientRepository duplicatePatientRepository;

    @Autowired
    public DuplicatePatientService(DuplicatePatientRepository duplicatePatientRepository) {
        this.duplicatePatientRepository = duplicatePatientRepository;
    }

    public List<DuplicatePatientData> findAllByCatchment(Catchment catchment) {
        List<DuplicatePatient> duplicatePatients = duplicatePatientRepository.findAllByCatchment(catchment);
        return buildDuplicatePatientData(duplicatePatients);
    }

    private List<DuplicatePatientData> buildDuplicatePatientData(List<DuplicatePatient> duplicatePatients) {
        List<DuplicatePatientData> duplicatePatientDataList = new ArrayList<>();
        for (DuplicatePatient duplicatePatient : duplicatePatients) {
            duplicatePatientDataList.add(buildDuplicatePatientData(duplicatePatient));
        }
        return duplicatePatientDataList;
    }

    private DuplicatePatientData buildDuplicatePatientData(DuplicatePatient duplicatePatient) {
        DuplicatePatientData duplicatePatientData = new DuplicatePatientData();
        duplicatePatientData.setHealthId1(duplicatePatient.getHealth_id1());
        duplicatePatientData.setHealthId2(duplicatePatient.getHealth_id2());
        duplicatePatientData.setReasons(duplicatePatient.getReasons());
        duplicatePatientData.setCreatedAt(DateUtil.toIsoFormat(duplicatePatient.getCreated_at()));
        return duplicatePatientData;
    }
}
