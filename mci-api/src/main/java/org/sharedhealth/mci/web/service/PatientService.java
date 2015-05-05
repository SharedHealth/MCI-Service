package org.sharedhealth.mci.web.service;


import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.web.exception.InsufficientPrivilegeException;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientFeedRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PatientSummaryData;
import org.sharedhealth.mci.web.mapper.PendingApproval;
import org.sharedhealth.mci.web.mapper.PendingApprovalListResponse;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.sharedhealth.mci.web.model.PendingApprovalMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_INVALID;
import static org.sharedhealth.mci.web.utils.JsonConstants.HID;

@Component
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);
    private static final int PER_PAGE_MAXIMUM_LIMIT = 25;
    public static final String PER_PAGE_MAXIMUM_LIMIT_NOTE = "There are more record for this search criteria. " +
            "Please narrow down your search";
    public static final String MESSAGE_INSUFFICIENT_PRIVILEGE = "insufficient.privilege";
    public static final String MESSAGE_INVALID_PENDING_APPROVALS = "invalid.pending.approvals";
    public static final String MESSAGE_PENDING_APPROVALS_MISMATCH = "pending.approvals.mismatch";

    private PatientRepository patientRepository;
    private PatientFeedRepository feedRepository;
    private FacilityService facilityService;
    private SettingService settingService;

    @Autowired
    public PatientService(PatientRepository patientRepository,
                          PatientFeedRepository feedRepository,
                          FacilityService facilityService,
                          SettingService settingService) {
        this.patientRepository = patientRepository;
        this.feedRepository = feedRepository;
        this.facilityService = facilityService;
        this.settingService = settingService;
    }

    public MCIResponse create(PatientData patient) {
        logger.debug("Create patient");
        PatientData existingPatient = findPatientByMultipleIds(patient);
        if (existingPatient != null) {
            return this.update(patient, existingPatient.getHealthId());
        }
        return patientRepository.create(patient);
    }

    PatientData findPatientByMultipleIds(PatientData patient) {
        if (!this.containsMultipleIds(patient)) {
            return null;
        }

        SearchQuery query = new SearchQuery();
        query.setNid(patient.getNationalId());
        List<PatientData> patientsByNid = findAllByQuery(query);

        query = new SearchQuery();
        query.setBin_brn(patient.getBirthRegistrationNumber());
        List<PatientData> patientsByBrn = findAllByQuery(query);
        if (isEmpty(patientsByNid) && isEmpty(patientsByBrn)) {
            return null;
        }

        List<PatientData> matchingPatients = (List<PatientData>) intersection(patientsByNid, patientsByBrn);
        if (isNotEmpty(matchingPatients)) {
            return matchingPatients.get(0);
        }
        matchingPatients = (List<PatientData>) union(patientsByNid, patientsByBrn);

        query = new SearchQuery();
        query.setUid(patient.getUid());
        List<PatientData> patientsByUid = findAllByQuery(query);

        matchingPatients = (List<PatientData>) intersection(matchingPatients, patientsByUid);
        if (isNotEmpty(matchingPatients)) {
            return matchingPatients.get(0);
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
        logger.debug(format("Update patient healthId: (%s)", healthId));
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

    public List<PatientData> findAllByCatchment(Catchment catchment, Date since, UUID lastMarker) {
        return patientRepository.findAllByCatchment(catchment, since, lastMarker, getPerPageMaximumLimit());
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

    public List<PendingApprovalListResponse> findPendingApprovalList(Catchment catchment, UUID after, UUID before, int limit) {
        List<PendingApprovalListResponse> pendingApprovals = new ArrayList<>();
        List<PendingApprovalMapping> mappings = patientRepository.findPendingApprovalMapping(catchment, after, before, limit);
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

    public TreeSet<PendingApproval> findPendingApprovalDetails(String healthId, Catchment catchment) {
        logger.debug(format("find pending approval for healthId: %s and for catchment: %s", healthId, catchment.toString()));
        PatientData patient = this.findByHealthId(healthId);
        if (patient == null) {
            return null;
        }
        verifyCatchment(patient, catchment);
        TreeSet<PendingApproval> pendingApprovals = patient.getPendingApprovals();
        if (isNotEmpty(pendingApprovals)) {
            for (PendingApproval pendingApproval : pendingApprovals) {
                pendingApproval.setCurrentValue(patient.getValue(pendingApproval.getName()));
            }
        }
        return pendingApprovals;
    }

    public String processPendingApprovals(PatientData requestData, Catchment catchment, boolean shouldAccept) {
        logger.debug(format("process pending approval for healthId: %s and for catchment: %s", requestData.getHealthId(), catchment.toString()));
        PatientData existingPatient = this.findByHealthId(requestData.getHealthId());
        verifyCatchment(existingPatient, catchment);
        verifyPendingApprovalDetails(requestData, existingPatient);
        return patientRepository.processPendingApprovals(requestData, existingPatient, shouldAccept);
    }

    public List<PatientUpdateLog> findPatientsUpdatedSince(Date since, UUID lastMarker) {
        return feedRepository.findPatientsUpdatedSince(since, getPerPageMaximumLimit(), lastMarker);
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
