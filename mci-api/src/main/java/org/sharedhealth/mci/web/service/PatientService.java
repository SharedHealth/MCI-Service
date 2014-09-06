package org.sharedhealth.mci.web.service;


import java.util.List;
import java.util.concurrent.ExecutionException;

import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.PatientMapper;
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

    public ListenableFuture<String> create(PatientMapper patientMapper) {
        return patientRepository.create(patientMapper);
    }
    public ListenableFuture<String> update(PatientMapper patientMapper,String healthId) {
        return patientRepository.update(patientMapper,healthId);
    }

    public ListenableFuture<PatientMapper> findByHealthId(String healthId) {
        return patientRepository.findByHealthId(healthId);
    }

    public ListenableFuture<PatientMapper> findByNationalId(String nationalId) {
        return patientRepository.findByNationalId(nationalId);
    }

    public ListenableFuture<PatientMapper> findByBirthRegistrationNumber(String birthRegistrationNumber) {
        return patientRepository.findByBirthRegistrationNumber(birthRegistrationNumber);
    }

    public ListenableFuture<PatientMapper> findByName(String name) {
        return patientRepository.findByName(name);
    }
    public ListenableFuture<PatientMapper> findByUid(String uid) {
        return patientRepository.findByUid(uid);
    }

    public ListenableFuture<List<PatientMapper>> findAllByQuery(MultiValueMap parameters) {
        return new ListenableFutureAdapter<List<PatientMapper>, List<PatientMapper>>(patientRepository.findAllByQuery(parameters)) {
            @Override
            protected List<PatientMapper> adapt(List<PatientMapper> patientMappers) throws ExecutionException {
                return patientMappers;
            }
        };
    }
}
