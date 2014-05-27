package org.mci.web.controller;

import org.mci.web.model.Patient;
import org.mci.web.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public DeferredResult<Boolean> create(@RequestBody Patient patient) throws ExecutionException, InterruptedException {
        final DeferredResult<Boolean> deferredResult = new DeferredResult<Boolean>();
        patientService.createPatient(patient).addCallback(new ListenableFutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
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
