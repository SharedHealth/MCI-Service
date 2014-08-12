package org.sharedhealth.mci.web.controller;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import static org.springframework.http.HttpStatus.CREATED;
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
    public DeferredResult<ResponseEntity<String>> create(@RequestBody @Valid Patient patient, BindingResult bindingResult)
            throws ExecutionException, InterruptedException {
        logger.debug("Trying to create patient. [" + patient + "]");
        final DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>();

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
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

    private DeferredResult<ResponseEntity<Patient>> findByNationalId(String nationalId)
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

    private DeferredResult<ResponseEntity<Patient>> findByBirthRegistrationNumber(String birthRegistrationNumber)
            throws ExecutionException, InterruptedException {
        logger.debug("Trying to find patient by birth registration number [" + birthRegistrationNumber + "]");
        final DeferredResult<ResponseEntity<Patient>> deferredResult = new DeferredResult<>();

        patientService.findByBirthRegistrationNumber(birthRegistrationNumber).addCallback(new ListenableFutureCallback<Patient>() {
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
    public DeferredResult<ResponseEntity<Patient>> findPatient(
            @RequestParam(value = "nid", required = false) String nationalId,
            @RequestParam(value = "bin_brn", required = false) String birthRegistrationNumber,
            @RequestParam(value = "uid", required = false) String uid,
            @RequestParam(value = "name", required = false) String name
    )
            throws ExecutionException, InterruptedException {

        if(nationalId != null) {
            return findByNationalId(nationalId);
        }

        if(birthRegistrationNumber != null) {
            return findByBirthRegistrationNumber(birthRegistrationNumber);
        }
        if(name != null) {
            return findByName(name);
        }
        if(uid != null) {
            return findByUid(uid);
        }

        throw new ValidationException("Invalid request");
    }

    private Throwable extractAppException(Throwable e) {
        if (e instanceof ExecutionException && e.getCause() != null) {
            return e.getCause();
        }
        return e;
    }

    public DeferredResult<ResponseEntity<Patient>> findByName(String name)
            throws ExecutionException, InterruptedException {
        logger.debug("Trying to find patient by name [" + name + "]");
        final DeferredResult<ResponseEntity<Patient>> deferredResult = new DeferredResult<>();

        patientService.findByName(name.toLowerCase()).addCallback(new ListenableFutureCallback<Patient>() {
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

    public DeferredResult<ResponseEntity<Patient>> findByUid(String uid)
            throws ExecutionException, InterruptedException {
        logger.debug("Trying to find patient by name [" + uid + "]");
        final DeferredResult<ResponseEntity<Patient>> deferredResult = new DeferredResult<>();

        patientService.findByUid(uid).addCallback(new ListenableFutureCallback<Patient>() {
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

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<List<Patient>> findAll(@RequestParam MultiValueMap parameters) {
        logger.debug("Find all patients  by search query ");
        final DeferredResult<List<Patient>> deferredResult = new DeferredResult<>();

        patientService.findAll(parameters).addCallback(new ListenableFutureCallback<List<Patient>>() {
            @Override
            public void onSuccess(List<Patient> result) {
                deferredResult.setResult(result);
            }

            @Override
            public void onFailure(Throwable error) {
                deferredResult.setErrorResult(error);
            }
        });

        return deferredResult;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{healthId}", consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<ResponseEntity<String>> update(@RequestBody @Valid Patient patient, @PathVariable String healthId, BindingResult bindingResult)
            throws ExecutionException, InterruptedException {
        logger.debug("Trying to update patient. [" + patient.getDateOfBirth() + "]");
        logger.debug(" Health id [" + healthId + "]");
        final DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>();

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        patientService.update(patient,healthId).addCallback(new ListenableFutureCallback<String>() {
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

}
