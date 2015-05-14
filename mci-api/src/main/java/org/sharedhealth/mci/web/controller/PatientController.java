package org.sharedhealth.mci.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.group.RequiredGroup;
import org.sharedhealth.mci.validation.group.RequiredOnUpdateGroup;
import org.sharedhealth.mci.web.exception.Forbidden;
import org.sharedhealth.mci.web.exception.SearchQueryParameterException;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PatientSummaryData;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.sharedhealth.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/patients")
public class PatientController extends MciController {
    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PreAuthorize("hasAnyRole('ROLE_PROVIDER', 'ROLE_FACILITY')")
    @RequestMapping(method = POST, consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<MCIResponse>> create(
            @RequestBody @Validated({RequiredGroup.class, Default.class}) PatientData patient,
            BindingResult bindingResult) {

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, format("Creating a new patient : %s", patient.getHealthId()));

        patient.setRequester(userInfo.getProperties());

        logger.debug("Trying to create patient.");
        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        if (null != patient.getActive() && !patient.getActive()) {
            throw new Forbidden(format("Cannot create inactive patient"));
        }

        if (null != patient.getMergedWith()) {
            throw new Forbidden(format("Cannot merge with another patient on creation"));
        }

        if (bindingResult.hasErrors()) {
            logger.debug("Validation error while trying to create patient.");
            throw new ValidationException(bindingResult);
        }

        MCIResponse mciResponse = patientService.create(patient);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_PROVIDER', 'ROLE_FACILITY', 'ROLE_PATIENT', " +
            "'ROLE_SHR System Admin', 'ROLE_MCI Admin', 'ROLE_MCI Approver')")
    @RequestMapping(value = "/{healthId}", method = GET)
    public DeferredResult<ResponseEntity<PatientData>> findByHealthId(@PathVariable String healthId) {
        UserInfo userInfo = getUserInfo();

        final DeferredResult<ResponseEntity<PatientData>> deferredResult = new DeferredResult<>();
        if (userInfo.getProperties().isPatientUserOnly()
                && !userInfo.getProperties().getPatientHid().equals(healthId)) {
            deferredResult.setErrorResult(new Forbidden(
                    format("Access is denied to user %s for patient with healthId : %s", userInfo.getProperties().getId(), healthId)));
            return deferredResult;
        }
        logAccessDetails(userInfo, format("Find patient given (healthId) : %s", healthId));
        logger.debug("Trying to find patient by health id [" + healthId + "]");

        PatientData result = formatResponse(patientService.findByHealthId(healthId));

        deferredResult.setResult(new ResponseEntity<>(result, OK));
        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_PROVIDER', 'ROLE_FACILITY', 'ROLE_MCI Admin')")
    @RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findPatients(
            @Valid SearchQuery searchQuery,
            BindingResult bindingResult) {
        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, format("Find patients matching query : %s", searchQuery));

        if (bindingResult.hasErrors()) {
            logger.debug("Validation error while finding all patients  by search query");
            throw new SearchQueryParameterException(bindingResult);
        }
        logger.debug("Find all patients  by search query");
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();
        final int limit = patientService.getPerPageMaximumLimit();
        final String note = patientService.getPerPageMaximumLimitNote();
        searchQuery.setMaximum_limit(limit);

        List<PatientSummaryData> results = formatResponse(patientService.findAllSummaryByQuery(searchQuery));
        HashMap<String, String> additionalInfo = new HashMap<>();
        if (results.size() > limit) {
            results = results.subList(0, limit);
            additionalInfo.put("note", note);
        }
        MCIMultiResponse mciMultiResponse = new MCIMultiResponse(results, additionalInfo, OK);
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));

        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_PROVIDER', 'ROLE_FACILITY', 'ROLE_MCI Admin')")
    @RequestMapping(method = PUT, value = "/{healthId}", consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<MCIResponse>> update(
            @PathVariable String healthId,
            @Validated({RequiredOnUpdateGroup.class, Default.class}) @RequestBody PatientData patient,
            BindingResult bindingResult) {

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, format("Updating patient (healthId): %s", healthId));

        patient.setRequester(userInfo.getProperties());

        logger.debug(" Health id [" + healthId + "]");
        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        if (bindingResult.hasErrors()) {
            logger.debug(format("Validation error while updating patient (healthId): %s", healthId));
            throw new ValidationException(bindingResult);
        }
        if (null != patient.getActive() || null != patient.getMergedWith()) {
            throw new Forbidden(format("Cannot update active field or merge with other patient"));
        }

        MCIResponse mciResponse = patientService.update(patient, healthId);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Approver')")
    @RequestMapping(method = PUT, value = "/active/{healthId}", consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<MCIResponse>> active(
            @PathVariable String healthId,
            @Validated({RequiredOnUpdateGroup.class, Default.class}) @RequestBody PatientData patient,
            BindingResult bindingResult) {

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, format("Updating patient (healthId): %s", healthId));

        patient.setRequester(userInfo.getProperties());

        logger.debug(" Health id [" + healthId + "]");
        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        if (mergingWithItself(patient.getMergedWith(), healthId)) {
            throw new Forbidden(format("Cannot merge with itself"));
        }


        MCIResponse mciResponse = patientService.update(patient, healthId);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    private boolean mergingWithItself(String mergedWith, String healthId) {
        if (StringUtils.isBlank(mergedWith)) {
            return false;
        }
        return mergedWith.equals(healthId);
    }

    private PatientData formatResponse(PatientData patient) {
        if (null == patient.getActive() || patient.getActive()) {
            return patient;
        }
        PatientData inactivePatientData = new PatientData();
        inactivePatientData.setHealthId(patient.getHealthId());
        inactivePatientData.setActive(patient.getActive());
        inactivePatientData.setMergedWith(patient.getMergedWith());
        return inactivePatientData;
    }


    private List<PatientSummaryData> formatResponse(List<PatientSummaryData> patientSummaryDataList) {
        ArrayList<PatientSummaryData> summaryDataList = new ArrayList<>();
        for (PatientSummaryData patientSummaryData : patientSummaryDataList) {
            summaryDataList.add(formatResponse(patientSummaryData));
        }
        return summaryDataList;
    }

    private PatientSummaryData formatResponse(PatientSummaryData patientSummaryData) {
        if (null == patientSummaryData.getActive() || patientSummaryData.getActive()) {
            return patientSummaryData;
        }
        PatientSummaryData inactivePatientSummaryData = new PatientSummaryData();
        inactivePatientSummaryData.setHealthId(patientSummaryData.getHealthId());
        inactivePatientSummaryData.setActive(patientSummaryData.getActive());
        inactivePatientSummaryData.setMergedWith(patientSummaryData.getMergedWith());
        return inactivePatientSummaryData;
    }

}
