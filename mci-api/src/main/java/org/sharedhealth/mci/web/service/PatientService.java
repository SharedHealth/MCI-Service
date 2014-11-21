package org.sharedhealth.mci.web.service;


import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.infrastructure.fr.FacilityRegistryWrapper;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Date;
import java.util.List;

@Component
public class PatientService {

    private static final String PER_PAGE_MAXIMUM_LIMIT_NOTE = "There are more record for this search criteria. Please narrow down your search";
    private static final int PER_PAGE_MAXIMUM_LIMIT = 25;
    private PatientRepository patientRepository;
    private FacilityRegistryWrapper facilityRegistryWrapper;
    private SettingService settingService;
    private static Integer perpageMaximimLimit = 25;
    private static String perPageMaximimLimitNote = "There are more record for this search criteria. Please narrow down your search";

    @Autowired
    public PatientService(PatientRepository patientRepository, FacilityRegistryWrapper facilityRegistryWrapper, SettingService settingService) {
        this.patientRepository = patientRepository;
        this.facilityRegistryWrapper = facilityRegistryWrapper;
        this.settingService = settingService;
    }

    public MCIResponse create(PatientMapper patientDto) {
        return patientRepository.create(patientDto);
    }

    public MCIResponse update(PatientMapper patientDto, String healthId) {
        return patientRepository.update(patientDto, healthId);
    }

    public PatientMapper findByHealthId(String healthId) {
        return patientRepository.findByHealthId(healthId);
    }

    public List<PatientMapper> findAllByQuery(SearchQuery searchQuery) {
        return patientRepository.findAllByQuery(searchQuery);
    }

    public ListenableFuture<List<PatientMapper>> findAllByLocations(List<String> locations, String last, Date since) {
        return patientRepository.findAllByLocations(locations, last, since);
    }

    public ListenableFuture<List<PatientMapper>> findAllByFacility(String facilityId, String last, Date since) {

        List<String> locations = facilityRegistryWrapper.getCatchmentAreasByFacility(facilityId);

        return findAllByLocations(locations, last, since);
    }

    public int getPerPageMaximumLimit() {
        Integer limit = settingService.getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT");

        if (limit == null) {
            return PER_PAGE_MAXIMUM_LIMIT;
        }

        return limit;
    }

    public String getPerPageMaximumLimitNote() {
        String note = settingService.getSettingAsStringByKey("PER_PAGE_MAXIMUM_LIMIT_NOTE");

        if (note == null) {
            return PER_PAGE_MAXIMUM_LIMIT_NOTE;
        }

        return note;
    }
}
