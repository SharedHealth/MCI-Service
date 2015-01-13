package org.sharedhealth.mci.web.service;


import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.web.exception.InsufficientPrivilegeException;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.infrastructure.fr.FacilityRegistryWrapper;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.model.PendingApprovalMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_INVALID;
import static org.sharedhealth.mci.web.utils.JsonConstants.HID;

@Component
public class PatientService {

    private static final int PER_PAGE_MAXIMUM_LIMIT = 25;
    private static final String PER_PAGE_MAXIMUM_LIMIT_NOTE = "There are more record for this search criteria. " +
            "Please narrow down your search";
    public static final String MESSAGE_INSUFFICIENT_PRIVILEGE = "insufficient.privilege";
    public static final String MESSAGE_INVALID_PENDING_APPROVALS = "invalid.pending.approvals";
    public static final String MESSAGE_PENDING_APPROVALS_MISMATCH = "pending.approvals.mismatch";

    private PatientRepository patientRepository;
    private FacilityRegistryWrapper facilityRegistryWrapper;
    private SettingService settingService;

    @Autowired
    public PatientService(PatientRepository patientRepository,
                          FacilityRegistryWrapper facilityRegistryWrapper,
                          SettingService settingService) {
        this.patientRepository = patientRepository;
        this.facilityRegistryWrapper = facilityRegistryWrapper;
        this.settingService = settingService;
    }

    public MCIResponse create(PatientData patient) {
        PatientData existingPatient = findPatientByMultipleIds(patient);
        if (existingPatient != null) {
            return this.update(patient, existingPatient.getHealthId());
        }
        return patientRepository.create(patient);
    }

    private PatientData findPatientByMultipleIds(PatientData patient) {
        if (!this.containsMultipleIds(patient)) {
            return null;
        }
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setNid(patient.getNationalId());
        searchQuery.setBin_brn(patient.getBirthRegistrationNumber());
        searchQuery.setUid(patient.getUid());
        List<PatientData> patients = this.findAllByQuery(searchQuery);
        if (isNotEmpty(patients)) {
            return patients.get(0);
        }
        return null;
    }

    boolean containsMultipleIds(PatientData patient) {
        int count = 0;
        if (isNotBlank(patient.getNationalId())) {
            count++;
        }
        if (isNotBlank(patient.getBirthRegistrationNumber())) {
            count++;
        }
        if (isNotBlank(patient.getUid())) {
            count++;
        }
        return count > 1;
    }

    public MCIResponse update(PatientData patient, String healthId) {
        if (patient.getHealthId() != null && !StringUtils.equals(patient.getHealthId(), healthId)) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patient, "patient");
            bindingResult.addError(new FieldError("patient", HID, ERROR_CODE_INVALID));
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

    public List<PatientSummaryData> findAllSummaryByQuery(SearchQuery searchQuery) {
        return patientRepository.findAllSummaryByQuery(searchQuery);
    }

    public List<PatientData> findAllByCatchment(Catchment catchment, Date after, String facilityId) {
        verifyCatchment(facilityId, catchment);
        return patientRepository.findAllByCatchment(catchment, after, getPerPageMaximumLimit());
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

    public List<PendingApprovalListResponse> findPendingApprovalList(Catchment catchment, UUID after, UUID before) {
        List<PendingApprovalListResponse> pendingApprovals = new ArrayList<>();
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(catchment, after, before, getPerPageMaximumLimit());
        if (isNotEmpty(mappings)) {
            for (PendingApprovalMapping mapping : mappings) {
                PatientData patient = patientRepository.findByHealthId(mapping.getHealthId());
                pendingApprovals.add(buildPendingApprovalListResponse(patient, mapping.getLastUpdated()));
            }
        }
        return pendingApprovals;
    }

    private PendingApprovalListResponse buildPendingApprovalListResponse(PatientData patient, UUID lastUpdated) {
        PendingApprovalListResponse pendingApproval = new PendingApprovalListResponse();
        pendingApproval.setHealthId(patient.getHealthId());
        pendingApproval.setGivenName(patient.getGivenName());
        pendingApproval.setSurname(patient.getSurName());
        pendingApproval.setLastUpdated(lastUpdated);
        return pendingApproval;
    }

    public TreeSet<PendingApproval> findPendingApprovalDetails(String healthId) {
        PatientData patient = this.findByHealthId(healthId);
        if (patient == null) {
            return null;
        }
        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        if (isNotEmpty(pendingApprovals)) {
            for (PendingApproval pendingApproval : pendingApprovals) {
                pendingApproval.setCurrentValue(patient.getValue(pendingApproval.getName()));
            }
        }
        return pendingApprovals;
    }

    public String processPendingApprovals(PatientData patient, Catchment catchment, boolean shouldAccept) {
        PatientData existingPatient = this.findByHealthId(patient.getHealthId());
        verifyCatchment(existingPatient, catchment);
        verifyPendingApprovalDetails(patient, existingPatient);
        return patientRepository.processPendingApprovals(patient, existingPatient, catchment, shouldAccept);
    }

    private void verifyCatchment(String facilityId, Catchment catchment) {
        List<Catchment> catchments = facilityRegistryWrapper.getCatchmentAreasByFacility(facilityId);
        if (!catchments.contains(catchment)) {
            throw new InsufficientPrivilegeException(MESSAGE_INSUFFICIENT_PRIVILEGE);
        }
    }

    private void verifyCatchment(PatientData patient, Catchment catchment) {
        if (!patient.belongsTo(catchment)) {
            throw new InsufficientPrivilegeException(MESSAGE_INSUFFICIENT_PRIVILEGE);
        }
    }

    private void verifyPendingApprovalDetails(PatientData patient, PatientData existingPatient) {
        if (isEmpty(existingPatient.getPendingApprovals())) {
            throw new IllegalArgumentException(MESSAGE_INVALID_PENDING_APPROVALS);
        }
        List<String> fieldNames = patient.findNonEmptyFieldNames();
        fieldNames.remove(HID);

        for (PendingApproval pendingApproval : existingPatient.getPendingApprovals()) {
            String fieldName = pendingApproval.getName();
            Object value = patient.getValue(fieldName);
            if (value != null && !pendingApproval.contains(value)) {
                throw new IllegalArgumentException(MESSAGE_PENDING_APPROVALS_MISMATCH);
            }
            fieldNames.remove(fieldName);
        }
        if (isNotEmpty(fieldNames)) {
            throw new IllegalArgumentException(MESSAGE_PENDING_APPROVALS_MISMATCH);
        }
    }
}
