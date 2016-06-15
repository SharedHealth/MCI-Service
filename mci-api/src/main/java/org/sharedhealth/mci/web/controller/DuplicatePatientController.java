package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.deduplication.model.DuplicatePatientData;
import org.sharedhealth.mci.deduplication.model.DuplicatePatientMergeData;
import org.sharedhealth.mci.deduplication.service.DuplicatePatientService;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.exception.Forbidden;
import org.sharedhealth.mci.domain.exception.ValidationException;
import org.sharedhealth.mci.domain.model.Catchment;
import org.sharedhealth.mci.domain.model.MCIResponse;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.service.SettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.sharedhealth.mci.domain.constant.JsonConstants.AFTER;
import static org.sharedhealth.mci.domain.constant.JsonConstants.BEFORE;
import static org.sharedhealth.mci.web.infrastructure.security.UserProfile.ADMIN_TYPE;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/patients/duplicates")
public class DuplicatePatientController extends MciController {

    private static final Logger logger = LoggerFactory.getLogger(DuplicatePatientController.class);
    public static final String PER_PAGE_MAXIMUM_LIMIT_NOTE = "PER_PAGE_MAXIMUM_LIMIT_NOTE";

    private DuplicatePatientService duplicatePatientService;
    private SettingService settingService;
    private static final int PER_PAGE_MAXIMUM_LIMIT = 25;

    @Autowired
    public DuplicatePatientController(DuplicatePatientService duplicatePatientService, SettingService settingService, MCIProperties
            properties) {
        super(properties);
        this.duplicatePatientService = duplicatePatientService;
        this.settingService = settingService;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Approver')")
    @RequestMapping(value = "/catchments/{catchmentId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findAllByCatchment(
            @PathVariable String catchmentId,
            @RequestParam(value = AFTER, required = false) UUID after,
            @RequestParam(value = BEFORE, required = false) UUID before,
            HttpServletRequest request) {

        UserInfo userInfo = getUserInfo();
        String message = format("Find list of patient duplicates for catchment %s", catchmentId);
        logAccessDetails(userInfo, message);
        logger.debug(message);

        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        if (!userInfo.getProperties().hasCatchmentForProfileType(catchmentId, asList(ADMIN_TYPE))) {
            String errorMessage = format("Access is denied to user %s for catchment %s",
                    userInfo.getProperties().getEmail(), catchmentId);
            deferredResult.setErrorResult(new Forbidden(errorMessage));
            logger.error(errorMessage);
            return deferredResult;
        }
        List<DuplicatePatientData> response = duplicatePatientService.findAllByCatchment
                (new Catchment(catchmentId), after, before, getPerPageMaximumLimit() + 1);

        if (null != before) {
            Collections.reverse(response);
        }
        MCIMultiResponse mciMultiResponse;
        if (response != null) {
            mciMultiResponse = buildPaginatedResponse(request, response, after, before, getPerPageMaximumLimit());
        } else {
            mciMultiResponse = new MCIMultiResponse(emptyList(), null, OK);
        }
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));
        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Approver')")
    @RequestMapping(method = PUT, consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<MCIResponse>> merge(
            @Valid @RequestBody DuplicatePatientMergeData data,
            BindingResult bindingResult) {

        UserInfo userInfo = getUserInfo();
        String message = format("Duplicate patients. HIDs: %s and %s. Action: %s",
                data.getPatient1().getHealthId(), data.getPatient2().getHealthId(), data.getAction());
        logAccessDetails(userInfo, message);
        logger.info(message);

        if (bindingResult.hasErrors()) {
            logger.error("ValidationException while merging duplicate patients");
            throw new ValidationException(bindingResult);
        }

        setRequester(data, userInfo);
        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();
        duplicatePatientService.processDuplicates(data);

        MCIResponse mciResponse = new MCIResponse(ACCEPTED);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    private void setRequester(DuplicatePatientMergeData data, UserInfo userInfo) {
        UserInfo.UserInfoProperties properties = userInfo.getProperties();
        data.getPatient1().setRequester(
                properties.getFacilityId(), properties.getProviderId(), properties.getAdminId()
                , properties.getName());
        data.getPatient2().setRequester(properties.getFacilityId(), properties.getProviderId(), properties.getAdminId()
                , properties.getName());
    }

    public int getPerPageMaximumLimit() {
        try {
            return Integer.parseInt(settingService.getSettingAsStringByKey(PER_PAGE_MAXIMUM_LIMIT_NOTE));
        } catch (Exception e) {
            return PER_PAGE_MAXIMUM_LIMIT;
        }
    }
}
