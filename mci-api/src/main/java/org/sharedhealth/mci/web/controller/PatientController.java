package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.validation.group.RequiredGroup;
import org.sharedhealth.mci.validation.group.RequiredOnUpdateGroup;
import org.sharedhealth.mci.web.exception.SearchQueryParameterException;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
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
import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.utils.DateUtil.fromIsoFormat;
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

    @RequestMapping(method = GET,value = "/updated-after/{after}", produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findPatients(@PathVariable String after) {

        Date date = isNotBlank(after) ? fromIsoFormat(after) : null;

        logger.debug("Find all patients  updated after [" + after +"] ");
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        List<PatientUpdateLog> results = patientService.findPatientsUpdatedSince(date);

        MCIMultiResponse mciMultiResponse = new MCIMultiResponse<>(results, null, OK);
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

    @RequestMapping(value = "/division/{divisionId}/district/{districtId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findAllPatientsByCatchment(
            @PathVariable String divisionId,
            @PathVariable String districtId,
            @RequestParam(value = AFTER, required = false) String after,
            @RequestHeader(FACILITY_ID) String facilityId) {
        return this.findAllPatientsByCatchment(divisionId, districtId, null, null, null, null, after, facilityId);
    }

    @RequestMapping(value = "/division/{divisionId}/district/{districtId}/upazila/{upazilaId}", method = GET,
            produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findAllPatientsByCatchment(
            @RequestHeader(FACILITY_ID) String facilityId,
            @PathVariable String divisionId,
            @PathVariable String districtId,
            @PathVariable String upazilaId,
            @RequestParam(value = AFTER, required = false) String after) {
        return this.findAllPatientsByCatchment(divisionId, districtId, upazilaId, null, null, null, after, facilityId);
    }

    @RequestMapping(value = "/division/{divisionId}/district/{districtId}/upazila/{upazilaId}/citycorp/{cityCorpId}",
            method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findAllPatientsByCatchment(
            @PathVariable String divisionId,
            @PathVariable String districtId,
            @PathVariable String upazilaId,
            @PathVariable String cityCorpId,
            @RequestParam(value = AFTER, required = false) String after,
            @RequestHeader(FACILITY_ID) String facilityId) {
        return this.findAllPatientsByCatchment(divisionId, districtId, upazilaId, cityCorpId, null, null, after, facilityId);
    }

    @RequestMapping(value = "/division/{divisionId}/district/{districtId}/upazila/{upazilaId}/citycorp/{cityCorpId}/union-urbanward/{unionOrUrbanWardId}",
            method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findAllPatientsByCatchment(
            @PathVariable String divisionId,
            @PathVariable String districtId,
            @PathVariable String upazilaId,
            @PathVariable String cityCorpId,
            @PathVariable String unionOrUrbanWardId,
            @RequestParam(value = AFTER, required = false) String after,
            @RequestHeader(FACILITY_ID) String facilityId) {
        return this.findAllPatientsByCatchment(divisionId, districtId, upazilaId, cityCorpId, unionOrUrbanWardId, null, after, facilityId);
    }

    @RequestMapping(value = "/division/{divisionId}/district/{districtId}/upazila/{upazilaId}/citycorp/{cityCorpId}/union-urbanward/{unionOrUrbanWardId}/ruralward/{ruralWardId}",
            method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findAllPatientsByCatchment(
            @PathVariable String divisionId,
            @PathVariable String districtId,
            @PathVariable String upazilaId,
            @PathVariable String cityCorpId,
            @PathVariable String unionOrUrbanWardId,
            @PathVariable String ruralWardId,
            @RequestParam(value = AFTER, required = false) String after,
            @RequestHeader(FACILITY_ID) String facilityId) {

        Catchment catchment = new Catchment(divisionId, districtId, upazilaId, cityCorpId, unionOrUrbanWardId, ruralWardId);
        logger.debug(format("Find all patients  for %s, after %s", catchment, after));
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        Date date = isNotBlank(after) ? fromIsoFormat(after) : null;
        List<PatientData> dataList = patientService.findAllByCatchment(catchment, date, facilityId);

        MCIMultiResponse mciMultiResponse = new MCIMultiResponse<>(dataList, null, OK);
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));
        return deferredResult;
    }


    @RequestMapping(value = "/pendingapprovals", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findPendingApprovalList(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = AFTER, required = false) UUID after,
            @RequestParam(value = BEFORE, required = false) UUID before) {

        logger.debug("Find list of pending approvals.");
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        Catchment catchment = buildCatchment(headers);
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

        logger.debug("Accepting pending approvals. Health ID : " + healthId);
        return processPendingApprovals(headers, healthId, patient, bindingResult, true);
    }

    @RequestMapping(value = "/pendingapprovals/{healthId}", method = DELETE, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIResponse>> rejectPendingApprovals(
            @RequestHeader HttpHeaders headers,
            @PathVariable String healthId,
            @Validated({RequiredOnUpdateGroup.class, Default.class}) @RequestBody PatientData patient,
            BindingResult bindingResult) {

        logger.debug("Accepting pending approvals. Health ID : " + healthId);
        return processPendingApprovals(headers, healthId, patient, bindingResult, false);
    }

    private DeferredResult<ResponseEntity<MCIResponse>> processPendingApprovals(
            HttpHeaders headers, String healthId, PatientData patient, BindingResult bindingResult, boolean shouldAccept) {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        patient.setHealthId(healthId);
        String hid = patientService.processPendingApprovals(patient, buildCatchment(headers), shouldAccept);

        MCIResponse mciResponse = new MCIResponse(hid, ACCEPTED);
        deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
        return deferredResult;
    }

    Catchment buildCatchment(HttpHeaders headers) {
        Catchment catchment = new Catchment(headers.getFirst(DIVISION_ID), headers.getFirst(DISTRICT_ID));
        String upazilaId = headers.getFirst(UPAZILA_ID);

        if (isNotBlank(upazilaId)) {
            catchment.setUpazilaId(upazilaId);
            String cityCorpId = headers.getFirst(CITY_CORPORATION_ID);

            if (isNotBlank(cityCorpId)) {
                catchment.setCityCorpId(cityCorpId);
                String unionOrUrbanWardId = headers.getFirst(UNION_OR_URBAN_WARD_ID);

                if (isNotBlank(unionOrUrbanWardId)) {
                    catchment.setUnionOrUrbanWardId(unionOrUrbanWardId);
                    String ruralWardId = headers.getFirst(RURAL_WARD_ID);

                    if (isNotBlank(ruralWardId)) {
                        catchment.setRuralWardId(ruralWardId);
                    }
                }
            }
        }
        return catchment;
    }
}
