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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import javax.validation.groups.Default;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {
    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private PatientService patientService;


    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @RequestMapping(method = POST, consumes = {APPLICATION_JSON_VALUE})
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

    @RequestMapping(value = "/{healthId}", method = GET)
    public DeferredResult<ResponseEntity<PatientData>> findByHealthId(@PathVariable String healthId) {
        logger.debug("Trying to find patient by health id [" + healthId + "]");
        final DeferredResult<ResponseEntity<PatientData>> deferredResult = new DeferredResult<>();

        PatientData result = patientService.findByHealthId(healthId);
        deferredResult.setResult(new ResponseEntity<>(result, OK));
        return deferredResult;
    }

    @RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
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

        List<PatientSummaryData> results = patientService.findAllSummaryByQuery(searchQuery);
        HashMap<String, String> additionalInfo = new HashMap<>();
        if (results.size() > limit) {
            results.remove(limit);
            additionalInfo.put("note", note);
        }
        MCIMultiResponse mciMultiResponse = new MCIMultiResponse<>(results, additionalInfo, OK);
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));

        return deferredResult;
    }

    @RequestMapping(method = PUT, value = "/{healthId}", consumes = {APPLICATION_JSON_VALUE})
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

    @RequestMapping(value = "/facility/{facilityId}", method = GET, produces = APPLICATION_JSON_VALUE)
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

    @RequestMapping(value = "/pendingapprovals", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findPendingApprovalList(
            @RequestHeader(value = DIVISION_ID) String divisionId,
            @RequestHeader(value = DISTRICT_ID) String districtId,
            @RequestHeader(value = UPAZILA_ID) String upazilaId,
            @RequestParam(value = AFTER, required = false) UUID after,
            @RequestParam(value = BEFORE, required = false) UUID before) {

        logger.debug("Find list of pending approvals.");
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        Catchment catchment = new Catchment(divisionId, districtId, upazilaId);
        List<PendingApprovalListResponse> response = patientService.findPendingApprovalList(catchment, after, before);

        MCIMultiResponse mciMultiResponse;
        if (response != null) {
            mciMultiResponse = new MCIMultiResponse(response, null, OK);
        } else {
            mciMultiResponse = new MCIMultiResponse(emptyList(), null, OK);
        }
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));
        return deferredResult;
    }

    @RequestMapping(value = "/pendingapprovals/{healthId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findPendingApprovalDetails(@PathVariable String healthId) {
        logger.debug("Find list of pending approval details. Health ID : " + healthId);
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        TreeSet<PendingApproval> response = patientService.findPendingApprovalDetails(healthId);

        MCIMultiResponse mciMultiResponse;
        if (response != null) {
            mciMultiResponse = new MCIMultiResponse(response, null, OK);
        } else {
            mciMultiResponse = new MCIMultiResponse(emptyList(), null, OK);
        }
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));

        return deferredResult;
    }

    @RequestMapping(value = "/pendingapprovals/{healthId}", method = PUT, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIResponse>> acceptPendingApprovals(
            @RequestHeader HttpHeaders headers,
            @PathVariable String healthId,
            @Validated({RequiredOnUpdateGroup.class, Default.class}) @RequestBody PatientData patient,
            BindingResult bindingResult) {

        logger.debug("Updating pending approvals. Health ID : " + healthId);
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        patient.setHealthId(healthId);
        String hid = patientService.acceptPendingApprovals(patient, buildCatchment(headers));

        MCIResponse mciResponse = new MCIResponse(hid, ACCEPTED);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    Catchment buildCatchment(HttpHeaders headers) {
        Catchment catchment = new Catchment();
        catchment.setDivisionId(getHeader(headers, DIVISION_ID));
        catchment.setDistrictId(getHeader(headers, DISTRICT_ID));
        catchment.setUpazilaId(getHeader(headers, UPAZILA_ID));
        catchment.setCityCorpId(getHeader(headers, CITY_CORPORATION_ID));
        catchment.setUnionOrUrbanWardId(getHeader(headers, UNION_OR_URBAN_WARD_ID));
        catchment.setRuralWardId(getHeader(headers, RURAL_WARD_ID));
        return catchment;
    }

    private String getHeader(HttpHeaders headers, String headerName) {
        String headerValue = headers.getFirst(headerName);
        if (isNotBlank(headerValue)) {
            return headerValue;
        }
        return null;
    }
}
