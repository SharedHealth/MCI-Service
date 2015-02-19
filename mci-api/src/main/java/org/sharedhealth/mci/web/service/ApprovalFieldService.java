package org.sharedhealth.mci.web.service;


import org.sharedhealth.mci.web.infrastructure.persistence.ApprovalFieldRepository;
import org.sharedhealth.mci.web.model.ApprovalField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApprovalFieldService {

    private ApprovalFieldRepository fieldsRepository;

    @Autowired
    public ApprovalFieldService(ApprovalFieldRepository fieldsRepository) {
        this.fieldsRepository = fieldsRepository;
    }

    public String getProperty(String field) {

        ApprovalField approvalField = fieldsRepository.findByField(field);

        return approvalField != null ? approvalField.getOption() : null;
    }

}
