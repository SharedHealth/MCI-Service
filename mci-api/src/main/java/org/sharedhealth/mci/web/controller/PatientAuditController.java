package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.mapper.PatientAuditLogData;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.service.PatientAuditService;
import org.sharedhealth.mci.web.service.PatientService;
import org.sharedhealth.mci.web.utils.JsonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.sharedhealth.mci.web.utils.JsonConstants.CREATED_AT;
import static org.sharedhealth.mci.web.utils.JsonConstants.CREATED_BY;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/v1/audit/patients")
public class PatientAuditController extends MciController{

    private static final Logger logger = LoggerFactory.getLogger(PatientAuditController.class);

    private PatientService patientService;
    private PatientAuditService auditService;

    @Autowired
    public PatientAuditController(PatientService patientService, PatientAuditService auditService) {
        this.patientService = patientService;
        this.auditService = auditService;
    }

    @RequestMapping(value = "/{healthId}", method = GET)
    public DeferredResult<ResponseEntity<Map<String, Object>>> findByHealthId(@PathVariable String healthId) {
        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, String.format("Find audit details of patient by health id : %s", healthId));

        logger.debug("Trying to find audit details of patient by health id [" + healthId + "]");

        final DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult = new DeferredResult<>();

        PatientData patient = patientService.findByHealthId(healthId);
        List<PatientAuditLogData> auditLogs = auditService.findByHealthId(healthId);

        Map<String, Object> result = new HashMap<>();
        result.put(CREATED_AT, patient.getCreatedAtAsString());
        result.put(CREATED_BY, patient.getCreatedBy());
        result.put(JsonConstants.UPDATES, auditLogs);

        deferredResult.setResult(new ResponseEntity<>(result, OK));
        return deferredResult;
    }
}
