package org.sharedhealth.mci.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.domain.exception.Forbidden;
import org.sharedhealth.mci.domain.exception.InvalidRequestException;
import org.sharedhealth.mci.domain.exception.ValidationException;
import org.sharedhealth.mci.domain.model.MCIResponse;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.PatientSummaryData;
import org.sharedhealth.mci.domain.model.SearchQuery;
import org.sharedhealth.mci.domain.validation.group.RequiredGroup;
import org.sharedhealth.mci.domain.validation.group.RequiredOnUpdateGroup;
import org.sharedhealth.mci.web.exception.SearchQueryParameterException;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.mapper.ProviderResponse;
import org.sharedhealth.mci.web.service.PatientService;
import org.sharedhealth.mci.web.service.ProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.sharedhealth.mci.domain.constant.ErrorConstants.ERROR_CODE_INVALID;
import static org.sharedhealth.mci.domain.constant.JsonConstants.HID;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/patients")
public class PatientController extends MciController {
    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);
    private final String PROVIDER_RESPONSE_ORG_REFERENCE_KEY = "reference";

    private PatientService patientService;
    private ProviderService providerService;

    @Autowired
    public PatientController(PatientService patientService, ProviderService providerService) {
        this.patientService = patientService;
        this.providerService = providerService;
    }

    @PreAuthorize("hasAnyRole('ROLE_PROVIDER', 'ROLE_FACILITY')")
    @RequestMapping(method = POST, consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<MCIResponse>> create(HttpServletRequest request,
                                                              @RequestBody @Validated({RequiredGroup.class, Default.class}) PatientData patient,
                                                              BindingResult bindingResult) throws InterruptedException {

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, format("Creating a new patient : %s %s", patient.getGender(), patient.getSurName()));

        UserInfo.UserInfoProperties properties = userInfo.getProperties();
        patient.setRequester(
                properties.getFacilityId(), properties.getProviderId(), properties.getAdminId()
                , properties.getName());

        logger.debug("Trying to create patient.");
        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        if (null != patient.getMergedWith()) {
            throw new InvalidRequestException(format("Cannot merge with another patient on creation"));
        }

        if (bindingResult.hasErrors()) {
            logger.debug("Validation error while trying to create patient.");
            throw new ValidationException(bindingResult);
        }

        MCIResponse mciResponse;
        if (StringUtils.isNotBlank(patient.getHealthId())) {
            String facilityId = identifyFacility(properties);
            mciResponse = patientService.createPatientForOrg(patient, facilityId);
        } else {
            mciResponse = patientService.createPatientForMCI(patient);
        }
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    private String identifyFacility(UserInfo.UserInfoProperties properties) {
        String facilityId = properties.getFacilityId();
        if (facilityId != null) return facilityId;
        String providerId = properties.getProviderId();
        if (providerId != null) {
            ProviderResponse response = providerService.find(providerId);
            Map<String, String> organization = response.getOrganization();
            String orgReference = organization.get(PROVIDER_RESPONSE_ORG_REFERENCE_KEY);
            String lastUriPart = StringUtils.substringAfterLast(orgReference, "/");
            return StringUtils.substringBefore(lastUriPart, ".json");
        }
        return null;
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
            logger.debug("Validation error while finding all patients by search query");
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

        if (patient.getHealthId() != null && !StringUtils.equals(patient.getHealthId(), healthId)) {
            bindingResult.addError(new FieldError("patient", HID, ERROR_CODE_INVALID));
            throw new ValidationException(bindingResult);
        }

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, format("Updating patient (healthId): %s", healthId));

        UserInfo.UserInfoProperties properties = userInfo.getProperties();
        patient.setRequester(properties.getFacilityId(), properties.getProviderId(),
                properties.getAdminId(), properties.getName());

        if (null != patient.isActive() || null != patient.getMergedWith()) {
            throw new InvalidRequestException(format("Cannot update active field or merge with other patient"));
        }

        if (bindingResult.hasErrors()) {
            logger.debug(format("Validation error while updating patient (healthId): %s", healthId));
            throw new ValidationException(bindingResult);
        }

        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();
        MCIResponse mciResponse = patientService.update(patient, healthId);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    private PatientData formatResponse(PatientData patient) {
        if (null == patient.isActive() || patient.isActive()) {
            return patient;
        }
        PatientData inactivePatientData = new PatientData();
        inactivePatientData.setHealthId(patient.getHealthId());
        inactivePatientData.setActive(patient.isActive());
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
