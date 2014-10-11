package org.sharedhealth.mci.web.service;


import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.infrastructure.fr.FacilityRegistryWrapper;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.SearchQuery;
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

    public ListenableFuture<MCIResponse> create(PatientMapper patientMapper) {
        return patientRepository.create(patientMapper);
    }
    public ListenableFuture<MCIResponse> update(PatientMapper patientMapper,String healthId) {
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

    public ListenableFuture<List<PatientMapper>> findAllByQuery(SearchQuery searchQuery) {
        return new ListenableFutureAdapter<List<PatientMapper>, List<PatientMapper>>(patientRepository.findAllByQuery(searchQuery)) {
            @Override
            protected List<PatientMapper> adapt(List<PatientMapper> patientMappers) throws ExecutionException {
                return patientMappers;
            }
        };
    }

    public ListenableFuture<List<PatientMapper>> findAllByLocations(List<String> locations, String last, Date since) {
        return patientRepository.findAllByLocations(locations, last, since);
    }

    public ListenableFuture<List<PatientMapper>> findAllByFacility(String facilityId, String last, Date since) {

        List<String> locations = facilityRegistryWrapper.getCatchmentAreasByFacility(facilityId);

        return findAllByLocations(locations, last, since);
    }

}
