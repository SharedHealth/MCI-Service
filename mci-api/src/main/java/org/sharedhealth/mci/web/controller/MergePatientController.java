package org.sharedhealth.mci.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.domain.exception.Forbidden;
import org.sharedhealth.mci.domain.exception.ValidationException;
import org.sharedhealth.mci.domain.model.MCIResponse;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.validation.group.RequiredOnUpdateGroup;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
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

import javax.validation.groups.Default;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/mergerequest")
public class MergePatientController extends MciController {
    private static final Logger logger = LoggerFactory.getLogger(MergePatientController.class);
    static final String ERROR_MSG_MERGE_WITH_ITSELF = "Cannot merge with itself";

    private PatientService patientService;

    @Autowired
    public MergePatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Approver')")
    @RequestMapping(method = PUT, value = "/{healthId}", consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<MCIResponse>> merge(
            @PathVariable String healthId,
            @Validated({RequiredOnUpdateGroup.class, Default.class}) @RequestBody PatientData patient,
            BindingResult bindingResult) {

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, format("Updating patient (healthId): %s", healthId));

        UserInfo.UserInfoProperties properties = userInfo.getProperties();
        patient.setRequester(properties.getFacilityId(), properties.getProviderId(),
                properties.getAdminId(), properties.getName());

        logger.debug(" Health id [" + healthId + "]");
        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        if (mergingWithItself(patient.getMergedWith(), healthId)) {
            throw new Forbidden(format(ERROR_MSG_MERGE_WITH_ITSELF));
        }

        MCIResponse mciResponse = patientService.update(patient, healthId).toBlocking().first();
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    private boolean mergingWithItself(String mergedWith, String healthId) {
        if (StringUtils.isBlank(mergedWith)) {
            return false;
        }
        return mergedWith.equals(healthId);
    }
}
