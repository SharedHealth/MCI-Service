package org.sharedhealth.mci.web.service;


import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.infrastructure.fr.FacilityRegistryWrapper;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PendingApprovalResponse;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.sharedhealth.mci.web.model.PendingApprovalMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

@Component
public class PatientService {

    private static final String PER_PAGE_MAXIMUM_LIMIT_NOTE = "There are more record for this search criteria. Please narrow down your search";
    private static final int PER_PAGE_MAXIMUM_LIMIT = 25;
    private PatientRepository patientRepository;
    private FacilityRegistryWrapper facilityRegistryWrapper;
    private SettingService settingService;

    @Autowired
    public PatientService(PatientRepository patientRepository, FacilityRegistryWrapper facilityRegistryWrapper, SettingService settingService) {
        this.patientRepository = patientRepository;
        this.facilityRegistryWrapper = facilityRegistryWrapper;
        this.settingService = settingService;
    }

    public MCIResponse create(PatientData patient) {
        return patientRepository.create(patient);
    }

    public MCIResponse update(PatientData patient, String healthId) {
        if (patient.getHealthId() != null && !StringUtils.equals(patient.getHealthId(), healthId)) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patient, "patient");
            bindingResult.addError(new FieldError("patient", "hid", "1004"));
            throw new ValidationException(bindingResult);
        }
        return patientRepository.update(patient, healthId);
    }

    public PatientData findByHealthId(String healthId) {
        return patientRepository.findByHealthId(healthId);
    }

    public List<PatientData> findAllByQuery(SearchQuery searchQuery) {
        return patientRepository.findAllByQuery(searchQuery);
    }

    public List<PatientData> findAllByFacility(String facilityId, String last, Date since) {
        List<String> locations = facilityRegistryWrapper.getCatchmentAreasByFacility(facilityId);
        return patientRepository.findAllByLocations(locations, last, since);
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

    public PendingApprovalResponse findPendingApprovals(Catchment catchment, UUID since) {
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(catchment, since);
        if (isNotEmpty(mappings)) {
            List<PatientData> patients = patientRepository.findByHealthId(getHealthIds(mappings));
            UUID until = mappings.get(0).getCreatedAt();
            return buildPendingApprovalResponse(patients, until);
        }
        return null;
    }

    private List<String> getHealthIds(List<PendingApprovalMapping> mappings) {
        List<String> healthIds = new ArrayList<>();
        for (PendingApprovalMapping mapping : mappings) {
            healthIds.add(mapping.getHealthId());
        }
        return healthIds;
    }

    private PendingApprovalResponse buildPendingApprovalResponse(List<PatientData> patients, UUID until) {
        List<Map<String, String>> pendingApprovals = new ArrayList<>();
        for (PatientData patient : patients) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put(HID, patient.getHealthId());
            metadata.put(GIVEN_NAME, patient.getGivenName());
            metadata.put(SUR_NAME, patient.getSurName());
            pendingApprovals.add(metadata);
        }
        PendingApprovalResponse response = new PendingApprovalResponse();
        response.setPendingApprovals(pendingApprovals);
        response.setUntil(until);
        return response;
    }
}
