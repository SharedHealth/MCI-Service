package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRuleEngine;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PatientUpdateLogData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class DuplicatePatientEventProcessor {

    private static final String ALPHABETS = "[A-Za-z]";

    private DuplicatePatientRuleEngine ruleEngine;
    private DuplicatePatientMapper mapper;
    private PatientRepository patientRepository;
    private DuplicatePatientRepository duplicatePatientRepository;

    public abstract void process(PatientUpdateLogData log, UUID marker);

    protected List<DuplicatePatient> buildDuplicates(String healthId) {
        List<DuplicatePatientData> duplicates = getRuleEngine().apply(healthId);
        return mapper.mapToDuplicatePatientList(duplicates);
    }

    protected List<DuplicatePatient> findDuplicatesByHealthId1(String healthId) {
        PatientData patient = patientRepository.findByHealthId(healthId);
        return duplicatePatientRepository.findByCatchmentAndHealthId(patient.getCatchment(), healthId);
    }

    protected DuplicatePatientRuleEngine getRuleEngine() {
        return ruleEngine;
    }

    @Autowired
    void setRuleEngine(DuplicatePatientRuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    protected DuplicatePatientMapper getMapper() {
        return mapper;
    }

    @Autowired
    void setMapper(DuplicatePatientMapper mapper) {
        this.mapper = mapper;
    }

    protected PatientRepository getPatientRepository() {
        return patientRepository;
    }

    @Autowired
    void setPatientRepository(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    protected DuplicatePatientRepository getDuplicatePatientRepository() {
        return duplicatePatientRepository;
    }

    @Autowired
    void setDuplicatePatientRepository(DuplicatePatientRepository duplicatePatientRepository) {
        this.duplicatePatientRepository = duplicatePatientRepository;
    }

    protected List<DuplicatePatient> filterPersistentDuplicates(DuplicatePatientRepository duplicatePatientRepository, List<DuplicatePatient> duplicates) {
        List<DuplicatePatient> filteredList = new ArrayList<>();
        filteredList.addAll(duplicates);
        for (DuplicatePatient duplicatepatient : duplicates) {
            List<DuplicatePatient> persistentEntries = duplicatePatientRepository.findByCatchmentAndHealthId(new Catchment(formatCatchmentId(duplicatepatient.getCatchment_id())), duplicatepatient.getHealth_id2());
            persistentEntries.addAll(duplicatePatientRepository.findByCatchmentAndHealthId(new Catchment(formatCatchmentId(duplicatepatient.getCatchment_id())), duplicatepatient.getHealth_id1()));
            for (DuplicatePatient persistentEntry : persistentEntries) {
                if (isSameEntry(persistentEntry, duplicatepatient)) {
                    filteredList.remove(duplicatepatient);
                }
            }
        }
        return filteredList;
    }

    private String formatCatchmentId(String catchmentId) {
        return catchmentId.replaceAll(ALPHABETS, "");
    }

    private boolean isSameEntry(DuplicatePatient entry1, DuplicatePatient entry2) {
        if (entry1.getHealth_id2().equals(entry2.getHealth_id1()) && entry1.getHealth_id1().equals(entry2.getHealth_id2()) && entry1.getReasons().equals(entry2.getReasons())) {
            return true;
        }
        if (entry1.getHealth_id1().equals(entry2.getHealth_id1()) && entry1.getHealth_id2().equals(entry2.getHealth_id2()) && entry1.getReasons().equals(entry2.getReasons())) {
            return true;
        }
        return false;
    }
}
