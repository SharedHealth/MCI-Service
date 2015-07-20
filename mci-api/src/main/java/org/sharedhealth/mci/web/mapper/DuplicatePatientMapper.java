package org.sharedhealth.mci.web.mapper;

import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.datastax.driver.core.utils.UUIDs.timeBased;

@Component
public class DuplicatePatientMapper {

    private PatientRepository patientRepository;
    private PatientMapper patientMapper;

    @Autowired
    public DuplicatePatientMapper(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    public List<DuplicatePatient> mapToDuplicatePatientList(List<DuplicatePatientData> duplicateDataList) {
        List<DuplicatePatient> duplicates = new ArrayList<>();
        for (DuplicatePatientData duplicateData : duplicateDataList) {
            map(duplicateData, duplicates);
        }
        return duplicates;
    }

    private List<DuplicatePatient> map(DuplicatePatientData duplicateData, List<DuplicatePatient> duplicates) {
        String healthId1 = duplicateData.getPatient1().getHealthId();
        String healthId2 = duplicateData.getPatient2().getHealthId();
        Set<String> reasons = duplicateData.getReasons();
        Catchment catchment1 = new Catchment(duplicateData.getPatient1().getAddress());
        Catchment catchment2 = new Catchment(duplicateData.getPatient2().getAddress());

        if (catchment1.equals(catchment2)) {
            buildDuplicates(catchment1, healthId1, healthId2, reasons, duplicates);
        } else {
            buildDuplicates(catchment1, healthId1, healthId2, reasons, duplicates);
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

    public List<DuplicatePatientData> mapToDuplicatePatientDataList(List<DuplicatePatient> duplicatePatients) {
        List<DuplicatePatientData> duplicates = new ArrayList<>();
        for (DuplicatePatient duplicate : duplicatePatients) {
            duplicates.add(mapToDuplicatePatientData(duplicate));
        }
        return duplicates;
    }

    private DuplicatePatientData mapToDuplicatePatientData(DuplicatePatient duplicatePatient) {
        DuplicatePatientData duplicatePatientData = new DuplicatePatientData();
        duplicatePatientData.setPatient1(buildPatientSummary(duplicatePatient.getHealth_id1()));
        duplicatePatientData.setPatient2(buildPatientSummary(duplicatePatient.getHealth_id2()));
        duplicatePatientData.setReasons(duplicatePatient.getReasons());
        duplicatePatientData.setCreatedAt(duplicatePatient.getCreated_at());
        return duplicatePatientData;
    }

    public DuplicatePatientData mapToDuplicatePatientData(PatientData patient1, PatientData patient2, Set<String> reasons) {
        DuplicatePatientData duplicatePatientData = new DuplicatePatientData();
        duplicatePatientData.setPatient1(patientMapper.mapSummary(patient1));
        duplicatePatientData.setPatient2(patientMapper.mapSummary(patient2));
        duplicatePatientData.setReasons(reasons);
        return duplicatePatientData;
    }

    private PatientSummaryData buildPatientSummary(String healthId) {
        PatientData patient = patientRepository.findByHealthId(healthId);
        return patientMapper.mapSummary(patient);
    }
}
