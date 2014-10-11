package org.sharedhealth.mci.web.controller;

import javax.validation.*;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.sharedhealth.mci.validation.group.RequiredGroup;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.mapper.PaginationQuery;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

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
    public DeferredResult<ResponseEntity<MCIResponse>> create(@RequestBody @Validated({RequiredGroup.class, Default.class}) PatientMapper patientMapper, BindingResult bindingResult)
            throws ExecutionException, InterruptedException {
        logger.debug("Trying to create patient.");
        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }


        patientService.create(patientMapper).addCallback(new ListenableFutureCallback<MCIResponse>() {
            @Override
            public void onSuccess(MCIResponse mciResponse) {
                deferredResult.setResult(new ResponseEntity<MCIResponse>(mciResponse, mciResponse.httpStatusObject));
            }

            @Override
            public void onFailure(Throwable e) {
                deferredResult.setErrorResult(extractAppException(e));
            }
        });
        return deferredResult;
    }

    @RequestMapping(value = "/{healthId}", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity<PatientMapper>> findByHealthId(@PathVariable String healthId)
            throws ExecutionException, InterruptedException {
        logger.debug("Trying to find patient by health id [" + healthId + "]");
        final DeferredResult<ResponseEntity<PatientMapper>> deferredResult = new DeferredResult<>();

        patientService.findByHealthId(healthId).addCallback(new ListenableFutureCallback<PatientMapper>() {
            @Override
            public void onSuccess(PatientMapper result) {
                deferredResult.setResult(new ResponseEntity<>(result, OK));
            }

            @Override
            public void onFailure(Throwable e) {
                deferredResult.setErrorResult(extractAppException(e));
            }
        });
        return deferredResult;
    }

    private Throwable extractAppException(Throwable e) {
        if (e instanceof ExecutionException && e.getCause() != null) {
            return e.getCause();
        }
        return e;
    }

    @RequestMapping(method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findPatients(@RequestParam MultiValueMap<String, String> parameters) {
        logger.debug("Find all patients  by search query ");
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        patientService.findAllByQuery(parameters).addCallback(new ListenableFutureCallback<List<PatientMapper>>() {
            @Override
            public void onSuccess(List<PatientMapper> results) {
                List<ArrayList> additionalInfo = null;
                MCIMultiResponse mciMultiResponse = new MCIMultiResponse<>(results, additionalInfo, OK);
                deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));
            }

            @Override
            public void onFailure(Throwable error) {
                deferredResult.setErrorResult(extractAppException(error));
            }
        });

        return deferredResult;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{healthId}", consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<MCIResponse>> update(@PathVariable String healthId, @Valid @RequestBody PatientMapper patientMapper, BindingResult bindingResult)
            throws ExecutionException, InterruptedException {
        logger.debug(" Health id [" + healthId + "]");
        final DeferredResult<ResponseEntity<MCIResponse>> deferredResult = new DeferredResult<>();

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        patientService.update(patientMapper, healthId).addCallback(new ListenableFutureCallback<MCIResponse>() {
            @Override
            public void onSuccess(MCIResponse mciResponse) {
                deferredResult.setResult(new ResponseEntity<>(mciResponse, mciResponse.httpStatusObject));
            }

            @Override
            public void onFailure(Throwable e) {
                deferredResult.setErrorResult(extractAppException(e));
            }
        });

        return deferredResult;
    }

    @RequestMapping(value = "/facility/{facilityId}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findAllPatientsInCatchment(
            @PathVariable String facilityId,
            @Valid PaginationQuery paginationQuery, BindingResult bindingResult
            )throws ExecutionException, InterruptedException  {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        logger.debug("Find all patients  for catchment of facility [" + facilityId+ "]");
        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        patientService.findAllByFacility(facilityId, paginationQuery.getLast(), paginationQuery.getDateSince()).addCallback(new ListenableFutureCallback<List<PatientMapper>>() {
            @Override
            public void onSuccess(List<PatientMapper> results) {
                List<ArrayList> additionalInfo = null;
                MCIMultiResponse mciMultiResponse = new MCIMultiResponse<>(results, additionalInfo, OK);
                deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));
            }

            @Override
            public void onFailure(Throwable error) {
                deferredResult.setErrorResult(extractAppException(error));
            }
        });

        return deferredResult;
    }
}
