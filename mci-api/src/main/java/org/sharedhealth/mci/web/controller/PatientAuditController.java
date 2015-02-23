package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.web.mapper.PatientAuditLogData;
import org.sharedhealth.mci.web.service.PatientAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/v1/audit/patients")
public class PatientAuditController {

    private static final Logger logger = LoggerFactory.getLogger(PatientAuditController.class);

    private PatientAuditService auditService;

    @Autowired
    public PatientAuditController(PatientAuditService auditService) {
        this.auditService = auditService;
    }

    @RequestMapping(value = "/{healthId}", method = GET)
    public DeferredResult<ResponseEntity<List<PatientAuditLogData>>> findByHealthId(@PathVariable String healthId) {
        logger.debug("Trying to find audit details of patient by health id [" + healthId + "]");
        final DeferredResult<ResponseEntity<List<PatientAuditLogData>>> deferredResult = new DeferredResult<>();

        List<PatientAuditLogData> result = auditService.findByHealthId(healthId);
        deferredResult.setResult(new ResponseEntity<>(result, OK));
        return deferredResult;
    }
}
