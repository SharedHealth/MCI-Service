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

    public ListenableFuture<String> create(Patient patient) {
        return patientRepository.create(patient);
    }

    public ListenableFuture<Patient> findByHealthId(String healthId) {
        return patientRepository.findByHealthId(healthId);
    }

    public ListenableFuture<Patient> findByNationalId(String nationalId) {
        return patientRepository.findByNationalId(nationalId);
    }
}
