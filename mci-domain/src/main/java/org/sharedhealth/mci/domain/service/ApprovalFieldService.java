package org.sharedhealth.mci.domain.service;


import org.sharedhealth.mci.domain.model.ApprovalField;
import org.sharedhealth.mci.domain.repository.ApprovalFieldRepository;
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
