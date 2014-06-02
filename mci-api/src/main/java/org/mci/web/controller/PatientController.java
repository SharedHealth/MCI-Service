package org.mci.web.controller;

import org.mci.web.model.Patient;
import org.mci.web.service.PatientService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ExecutionException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/patient")
public class PatientController {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PatientController.class);

    private PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<String> create(@RequestBody Patient patient) throws ExecutionException, InterruptedException {
        logger.debug("Creating patient. [" + patient + "]");
        final DeferredResult<String> deferredResult = new DeferredResult<String>();

        patientService.create(patient).addCallback(new ListenableFutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                deferredResult.setResult(result);
            }

            @Override
            public void onFailure(Throwable error) {
                deferredResult.setErrorResult(error);
            }
        });
        return deferredResult;
    }

    @RequestMapping(value = "/{healthId}", method = RequestMethod.GET)
    public DeferredResult<Patient> find(@PathVariable String healthId) throws ExecutionException, InterruptedException {
        logger.debug("Finding patient. HealthId: [" + healthId + "]");
        final DeferredResult<Patient> deferredResult = new DeferredResult<Patient>();

        patientService.find(healthId).addCallback(new ListenableFutureCallback<Patient>() {
            @Override
            public void onSuccess(Patient result) {
                deferredResult.setResult(result);
            }

            @Override
            public void onFailure(Throwable error) {
                deferredResult.setErrorResult(error);
            }
        });
        return deferredResult;
    }
}
