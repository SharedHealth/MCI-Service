package org.mci.web.service;


import org.mci.web.infrastructure.persistence.PatientRepository;
import org.mci.web.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Component
public class PatientService {

    private PatientRepository patientRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public ListenableFuture<Boolean> createPatient(Patient patient) {
        return patientRepository.save(patient);
    }
}
