package org.sharedhealth.mci.web.service;


import org.apache.commons.collections4.CollectionUtils;
import org.sharedhealth.mci.domain.exception.Forbidden;
import org.sharedhealth.mci.domain.exception.InvalidRequestException;
import org.sharedhealth.mci.domain.exception.PatientNotFoundException;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.repository.PatientFeedRepository;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.sharedhealth.mci.domain.service.PendingApprovalFilter;
import org.sharedhealth.mci.web.exception.InsufficientPrivilegeException;
import org.sharedhealth.mci.web.mapper.PendingApprovalListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.intersection;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.CollectionUtils.union;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.domain.constant.JsonConstants.HID;
import static org.sharedhealth.mci.domain.constant.JsonConstants.RELATIONS;
import static org.sharedhealth.mci.utils.HttpUtil.AVAILABILITY_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.REASON_KEY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);
    private static final int PER_PAGE_MAXIMUM_LIMIT = 25;
    public static final String PER_PAGE_MAXIMUM_LIMIT_NOTE = "There are more record for this search criteria. " +
            "Please narrow down your search";
    public static final String MESSAGE_INSUFFICIENT_PRIVILEGE = "insufficient.privilege";
    public static final String MESSAGE_INVALID_PENDING_APPROVALS = "invalid.pending.approvals";
    public static final String MESSAGE_PENDING_APPROVALS_MISMATCH = "pending.approvals.mismatch";
    public static final int CREATED = 201;

    private PatientRepository patientRepository;
    private PatientFeedRepository feedRepository;
    private SettingService settingService;
    private HealthIdService healthIdService;
    private PendingApprovalFilter pendingApprovalFilter;

    @Autowired
    public PatientService(PatientRepository patientRepository,
                          PatientFeedRepository feedRepository,
                          SettingService settingService,
                          HealthIdService healthIdService, PendingApprovalFilter pendingApprovalFilter) {
        this.patientRepository = patientRepository;
        this.feedRepository = feedRepository;
        this.settingService = settingService;
        this.healthIdService = healthIdService;
        this.pendingApprovalFilter = pendingApprovalFilter;
    }

    public MCIResponse createPatientForMCI(PatientData patient) throws InterruptedException {
        logger.debug("Create patient");
        PatientData existingPatient = findPatientByMultipleIds(patient);
        if (existingPatient != null) {
            return this.update(patient, existingPatient.getHealthId());
        }

        MCIResponse mciResponse;
        try {
            setHealthIdAssignor(patient);
            String healthId = healthIdService.getNextHealthId();
            patient.setHealthId(healthId);
            mciResponse = patientRepository.create(patient);
            if (CREATED != mciResponse.getHttpStatus()) {
                healthIdService.putBackHealthId(healthId);
            }
        } catch (NoSuchElementException e) {
            mciResponse = new MCIResponse("Can not create patient as there is no hid available in MCI to assign", BAD_REQUEST);
        }
        return mciResponse;
    }

    public MCIResponse createPatientForOrg(PatientData patient, String facilityId) {
        String healthId = patient.getHealthId();
        logger.debug(String.format("Creating patient for Organization [%s]", facilityId));
        PatientData existingPatient = findPatientByMultipleIds(patient);
        if (existingPatient != null) {
            return this.update(patient, existingPatient.getHealthId());
        }
        Map response = healthIdService.validateHIDForOrg(healthId, facilityId);
        boolean isValid = (boolean) response.get(AVAILABILITY_KEY);
        if (!isValid) {
            String reason = (String) response.get(REASON_KEY);
            return new MCIResponse(reason, BAD_REQUEST);
        }
        MCIResponse mciResponse = patientRepository.create(patient);
        return mciResponse;
    }

    private void setHealthIdAssignor(PatientData patient) {
        if (null == patient.getHealthId()) {
            patient.setAssignedBy("MCI");
        }
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

    public MCIResponse update(PatientData patientData, String healthId) {
        logger.debug(String.format("Update patient: %s", healthId));
        patientData.setHealthId(healthId);
        Requester requester = patientData.getRequester();

        PatientData existingPatientData = patientRepository.findByHealthId(healthId);
        if (Boolean.FALSE.equals(existingPatientData.isActive())) {
            String mergedWith = existingPatientData.getMergedWith();
            String errorMessage = mergedWith != null ? String.format("Cannot update inactive patient, already merged with %s", mergedWith) : "Cannot update inactive patient";
            throw new InvalidRequestException(errorMessage);
        }

        checkIfTryingToMergeWithNonExistingOrInactiveHid(patientData.getMergedWith());
        PatientData newPatientData = pendingApprovalFilter.filter(existingPatientData, patientData);
        if (!checkUpdateNeeded(newPatientData, healthId)) {
            return new MCIResponse(healthId, HttpStatus.ACCEPTED);
        }
        return patientRepository.update(newPatientData, existingPatientData, requester);
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

    public List<Map<String, Object>> findAllByCatchment(Catchment catchment, Date since, UUID lastMarker) {
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
        if (null != patient.isActive() && !patient.isActive()) {
            throw new Forbidden("patient is already marked inactive");
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
        PatientData existingPatient = this.findByHealthId(requestData.getHealthId());
        if (null != existingPatient.isActive() && !existingPatient.isActive()) {
            throw new Forbidden("patient is already marked inactive");
        }
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

            if (value != null && fieldName.equals(RELATIONS) && !pendingApproval.compareRelation(value)) {
                throw new IllegalArgumentException(MESSAGE_PENDING_APPROVALS_MISMATCH);
            }
            if (value != null && !pendingApproval.contains(value) && !fieldName.equals(RELATIONS)) {
                throw new IllegalArgumentException(MESSAGE_PENDING_APPROVALS_MISMATCH);
            }
            fieldNames.remove(fieldName);
        }
        if (isNotEmpty(fieldNames)) {
            throw new IllegalArgumentException(MESSAGE_PENDING_APPROVALS_MISMATCH);
        }
    }

    private boolean checkIfTryingToMergeWithNonExistingOrInactiveHid(String mergedWith) {
        if (null == mergedWith) {
            return false;
        }
        PatientData targetPatient;
        try {
            targetPatient = this.findByHealthId(mergedWith);
        } catch (PatientNotFoundException e) {
            throw new PatientNotFoundException("Merge_with patient not found with health id: " + mergedWith);
        }
        if (!targetPatient.isActive()) {
            throw new Forbidden("Cannot merge with inactive patient");
        }
        return false;
    }

    private boolean checkUpdateNeeded(PatientData newPatientData, String healthId) {
        TreeSet<PendingApproval> newPendingApprovals = newPatientData.getPendingApprovals();
        PatientData existingPatientData = patientRepository.findByHealthId(healthId);
        boolean isSamePatientData = existingPatientData.equals(newPatientData);
        if (CollectionUtils.isEmpty(newPendingApprovals) && isSamePatientData) return false;
        TreeSet<PendingApproval> existingPendingApprovals = existingPatientData.getPendingApprovals();
        if (isSamePatientData && CollectionUtils.isNotEmpty(newPendingApprovals) && CollectionUtils.isNotEmpty(existingPendingApprovals)) {
            for (PendingApproval newPendingApproval : newPendingApprovals) {
                if (matchingApprovalNotPresent(newPendingApproval, existingPendingApprovals)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean matchingApprovalNotPresent(PendingApproval newPendingApproval, TreeSet<PendingApproval> existingPendingApprovals) {
        for (PendingApproval existingPendingApproval : existingPendingApprovals) {
            if (existingPendingApproval.equals(newPendingApproval)) {
                return matchingFieldValueNotPresent(existingPendingApproval, newPendingApproval);
            }
        }
        return true;
    }

    private boolean matchingFieldValueNotPresent(PendingApproval existingPendingApproval, PendingApproval newPendingApproval) {
        for (Map.Entry<UUID, PendingApprovalFieldDetails> entry : newPendingApproval.getFieldDetails().entrySet()) {
            if (!existingPendingApproval.getFieldDetails().containsValue(entry.getValue()))
                return true;
        }
        return false;
    }
}
