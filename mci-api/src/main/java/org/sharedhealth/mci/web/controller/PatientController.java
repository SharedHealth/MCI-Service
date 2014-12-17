package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.validation.group.RequiredGroup;
import org.sharedhealth.mci.validation.group.RequiredOnUpdateGroup;
import org.sharedhealth.mci.web.exception.SearchQueryParameterException;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import javax.validation.groups.Default;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {
    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private PatientService patientService;


    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<MCIResponse>> create(
            @RequestBody @Validated({RequiredGroup.class, Default.class}) PatientData patient,
            BindingResult bindingResult) {

        logger.debug("Trying to create patient.");
        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        MCIResponse mciResponse = patientService.create(patient);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    @RequestMapping(value = "/{healthId}", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity<PatientData>> findByHealthId(@PathVariable String healthId) {
        logger.debug("Trying to find patient by health id [" + healthId + "]");
        final DeferredResult<ResponseEntity<PatientData>> deferredResult = new DeferredResult<>();

        PatientData result = patientService.findByHealthId(healthId);
        deferredResult.setResult(new ResponseEntity<>(result, OK));
        return deferredResult;
    }

    @RequestMapping(method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findPatients(
            @Valid SearchQuery searchQuery,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new SearchQueryParameterException(bindingResult);
        }
        logger.debug("Find all patients  by search query ");
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();
        final int limit = patientService.getPerPageMaximumLimit();
        final String note = patientService.getPerPageMaximumLimitNote();
        searchQuery.setMaximum_limit(limit);

        List<PatientData> results = patientService.findAllByQuery(searchQuery);
        HashMap<String, String> additionalInfo = new HashMap<>();
        if (results.size() > limit) {
            results.remove(limit);
            additionalInfo.put("note", note);
        }
        MCIMultiResponse mciMultiResponse = new MCIMultiResponse<>(results, additionalInfo, OK);
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));

        return deferredResult;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{healthId}", consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<MCIResponse>> update(
            @PathVariable String healthId,
            @Validated({RequiredOnUpdateGroup.class, Default.class}) @RequestBody PatientData patient,
            BindingResult bindingResult) {

        logger.debug(" Health id [" + healthId + "]");
        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        MCIResponse mciResponse = patientService.update(patient, healthId);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    @RequestMapping(value = "/facility/{facilityId}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findAllPatientsInCatchment(
            @PathVariable String facilityId,
            @Valid PaginationQuery paginationQuery,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        logger.debug("Find all patients  for catchment of facility [" + facilityId + "]");
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        List<PatientData> dataList = patientService.findAllByFacility(facilityId, paginationQuery.getLast(), paginationQuery.getDateSince());
        HashMap<String, String> additionalInfo = null;
        MCIMultiResponse mciMultiResponse = new MCIMultiResponse<>(dataList, additionalInfo, OK);
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));
        return deferredResult;
    }

    @RequestMapping(value = "/pendingapprovals", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findApprovals(
            @RequestHeader(value = DIVISION_ID) String divisionId,
            @RequestHeader(value = DISTRICT_ID) String districtId,
            @RequestHeader(value = UPAZILA_ID) String upazilaId,
            @RequestParam(value = LAST_ITEM_ID, required = false) UUID lastItemId) {

        logger.debug("Find list of pending approvals before : " + lastItemId);
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        Catchment catchment = new Catchment(divisionId, districtId, upazilaId);
        PendingApprovalListResponse response = patientService.findPendingApprovalList(catchment, lastItemId);

        MCIMultiResponse mciMultiResponse;
        if (response != null) {
            HashMap<String, String> additionalInfo = null;
            if (response.getLastItemId() != null) {
                additionalInfo = new HashMap<>();
                additionalInfo.put(LAST_ITEM_ID, response.getLastItemId().toString());
            }
            mciMultiResponse = new MCIMultiResponse(response.getPendingApprovals(), additionalInfo, OK);
        } else {
            mciMultiResponse = new MCIMultiResponse(Collections.emptyList(), null, OK);
        }
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));
        return deferredResult;
    }
}
