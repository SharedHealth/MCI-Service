package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.utils.DateUtil.fromIsoFormat;
import static org.sharedhealth.mci.web.utils.JsonConstants.AFTER;
import static org.sharedhealth.mci.web.utils.JsonConstants.FACILITY_ID;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/v1/catchments")
public class CatchmentController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private PatientService patientService;


    @Autowired
    public CatchmentController(PatientService patientService) {
        this.patientService = patientService;
    }

    @RequestMapping(value = "/{catchmentId}/patients", method = GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findAllPatients(
            @PathVariable String catchmentId,
            @RequestParam(value = AFTER, required = false) String after,
            @RequestHeader(FACILITY_ID) String facilityId) {

        Catchment catchment = new Catchment(catchmentId);
        logger.debug(format("Find all patients  for %s, after %s", catchment, after));
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        Date date = isNotBlank(after) ? fromIsoFormat(after) : null;
        List<PatientData> dataList = patientService.findAllByCatchment(catchment, date, facilityId);

        MCIMultiResponse mciMultiResponse = new MCIMultiResponse<>(dataList, null, OK);
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));
        return deferredResult;
    }
}
