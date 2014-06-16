package org.mci.web.controller;

import java.util.concurrent.ExecutionException;
import javax.validation.Valid;
import org.mci.web.exception.ValidationException;
import org.mci.web.model.Patient;
import org.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/patient")
public class PatientController {
    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<String>> create(@RequestBody @Valid Patient patient, BindingResult bindingResult)
            throws ExecutionException, InterruptedException {
        logger.debug("Trying to create patient. [" + patient + "]");
        final DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>();

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult.toString());
        }


        patientService.create(patient).addCallback(new ListenableFutureCallback<String>() {
            @Override
            public void onSuccess(String healthId) {
                deferredResult.setResult(new ResponseEntity<>(healthId, CREATED));
            }

            @Override
            public void onFailure(Throwable e) {
                deferredResult.setErrorResult(extractAppException(e));
            }
        });
        return deferredResult;
    }

    @RequestMapping(value = "/{healthId}", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity<Patient>> findByHealthId(@PathVariable String healthId)
            throws ExecutionException, InterruptedException {
        logger.debug("Trying to find patient by health id [" + healthId + "]");
        final DeferredResult<ResponseEntity<Patient>> deferredResult = new DeferredResult<>();

        patientService.findByHealthId(healthId).addCallback(new ListenableFutureCallback<Patient>() {
            @Override
            public void onSuccess(Patient result) {
                deferredResult.setResult(new ResponseEntity<>(result, OK));
            }

            @Override
            public void onFailure(Throwable e) {
                deferredResult.setErrorResult(extractAppException(e));
            }
        });
        return deferredResult;
    }

    @RequestMapping(method = RequestMethod.GET)
    public DeferredResult<ResponseEntity<Patient>> findByNationalId(@RequestParam("nid") String nationalId)
            throws ExecutionException, InterruptedException {
        logger.debug("Trying to find patient by national id [" + nationalId + "]");
        final DeferredResult<ResponseEntity<Patient>> deferredResult = new DeferredResult<>();

        patientService.findByNationalId(nationalId).addCallback(new ListenableFutureCallback<Patient>() {
            @Override
            public void onSuccess(Patient result) {
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
}
