package org.sharedhealth.mci.web.service;


import java.util.List;
import java.util.concurrent.ExecutionException;

import org.sharedhealth.mci.web.infrastructure.fr.FacilityRegistryWrapper;
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
    private FacilityRegistryWrapper facilityRegistryWrapper;

    @Autowired
    public PatientService(PatientRepository patientRepository, FacilityRegistryWrapper facilityRegistryWrapper) {
        this.patientRepository = patientRepository;
        this.facilityRegistryWrapper = facilityRegistryWrapper;
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

    public ListenableFuture<List<PatientMapper>> findAllByLocation(List<String> locations, String last) {
        return new ListenableFutureAdapter<List<PatientMapper>, List<PatientMapper>>(patientRepository.findAllByLocations(locations, last)) {
            @Override
            protected List<PatientMapper> adapt(List<PatientMapper> patientMappers) throws ExecutionException {
                return patientMappers;
            }
        };
    }

    public ListenableFuture<List<PatientMapper>> findAllByFacility(String facilityId,String last) {
        List<String> locations = facilityRegistryWrapper.getCatchmentAreasByFacility(facilityId);

        return findAllByLocation(locations, last);
    }

}
