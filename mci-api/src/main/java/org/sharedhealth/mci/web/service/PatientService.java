package org.sharedhealth.mci.web.service;


import java.util.List;
import java.util.concurrent.ExecutionException;

import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

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
    public ListenableFuture<String> update(Patient patient,String healthId) {
        return patientRepository.update(patient,healthId);
    }

    public ListenableFuture<Patient> findByHealthId(String healthId) {
        return patientRepository.findByHealthId(healthId);
    }

    public ListenableFuture<Patient> findByNationalId(String nationalId) {
        return patientRepository.findByNationalId(nationalId);
    }

    public ListenableFuture<Patient> findByBirthRegistrationNumber(String birthRegistrationNumber) {
        return patientRepository.findByBirthRegistrationNumber(birthRegistrationNumber);
    }

    public ListenableFuture<Patient> findByName(String name) {
        return patientRepository.findByName(name);
    }
    public ListenableFuture<Patient> findByUid(String uid) {
        return patientRepository.findByUid(uid);
    }

    public ListenableFuture<List<Patient>> findAll(MultiValueMap parameters) {
        return new ListenableFutureAdapter<List<Patient>, List<Patient>>(patientRepository.findAll(parameters)) {
            @Override
            protected List<Patient> adapt(List<Patient> patients) throws ExecutionException {
                return patients;
            }
        };
    }
}
