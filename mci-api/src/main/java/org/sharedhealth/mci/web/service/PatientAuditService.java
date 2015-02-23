package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.infrastructure.persistence.PatientAuditRepository;
import org.sharedhealth.mci.web.mapper.PatientAuditLogData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientAuditService {
    private static final Logger logger = LoggerFactory.getLogger(PatientAuditService.class);

    private PatientAuditRepository auditRepository;

    @Autowired
    public PatientAuditService(PatientAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }


    public List<PatientAuditLogData> findByHealthId(String healthId) {
        return auditRepository.findByHealthId(healthId);
    }
}
