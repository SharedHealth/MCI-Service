package org.sharedhealth.mci.web.service;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.infrastructure.fr.FacilityRegistryWrapper;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

@Component
public class PatientService {

    private PatientRepository patientRepository;
    private FacilityRegistryWrapper facilityRegistryWrapper;
    private SettingService settingService;
    public HashMap<String, String> systemSettings;

    @Autowired
    public PatientService(PatientRepository patientRepository, FacilityRegistryWrapper facilityRegistryWrapper, SettingService settingService) {
        this.patientRepository = patientRepository;
        this.facilityRegistryWrapper = facilityRegistryWrapper;
        this.settingService = settingService;
    }

    public ListenableFuture<MCIResponse> create(PatientMapper patientMapper) {
        return patientRepository.create(patientMapper);
    }

    public ListenableFuture<MCIResponse> update(PatientMapper patientMapper, String healthId) {
        return patientRepository.update(patientMapper, healthId);
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

    public int getPerPageMaximumLimit() {
        int limit;

        if (systemSettings == null) {
            systemSettings = settingService.getSettingAsHashMapByKey("system");
        }

        limit = patientRepository.getPerPageMaximumLimit();

        if (systemSettings.get("PER_PAGE_MAXIMUM_LIMIT") != null) {
            limit = Integer.parseInt(systemSettings.get("PER_PAGE_MAXIMUM_LIMIT"));
        }

        return limit;

    }

    public String getPerPageMaximumLimitNote() {
        if (systemSettings == null) {
            systemSettings = settingService.getSettingAsHashMapByKey("system");
        }
        return systemSettings.get("PER_PAGE_MAXIMUM_LIMIT_NOTE");

    }

}
