package org.sharedhealth.mci.domain.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sharedhealth.mci.domain.model.ApprovalField;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.sharedhealth.mci.domain.constant.JsonConstants.GENDER;

public class ApprovalFieldRepositoryIT extends BaseRepositoryIT {

    @Autowired
    private ApprovalFieldRepository approvalFieldRepository;

    private ApprovalField approvalField;
    private String field = GENDER;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        approvalField = new ApprovalField(field, "NA");
    }

    @Test
    public void shouldReturnNull_IfDataDoesNotExistForGivenKey() throws ExecutionException, InterruptedException {
        assertNull(approvalFieldRepository.findByField("random string"));
    }

    @Test
    public void shouldFindDataWithMatchingField() throws ExecutionException, InterruptedException {
        approvalFieldRepository.save(approvalField);
        final ApprovalField fieldData = approvalFieldRepository.findByField(field);

        assertNotNull(fieldData);
        assertEquals(approvalField, fieldData);
    }

    @Test
    public void shouldCacheData() throws ExecutionException, InterruptedException {
        approvalFieldRepository.save(approvalField);
        ApprovalField fieldData = approvalFieldRepository.findByField(field);
        assertEquals(approvalField, fieldData);
        cqlTemplate.execute("INSERT INTO approval_fields (\"field\", \"option\") VALUES ('gender', 'NU')");
        fieldData = approvalFieldRepository.findByField(field);
        assertEquals(approvalField, fieldData);

        approvalFieldRepository.resetCacheByKey(field);
        fieldData = approvalFieldRepository.findByField(field);
        assertEquals(new ApprovalField(field, "NU"), fieldData);
    }

    @After
    public void tearDown() throws ExecutionException, InterruptedException {
        cqlTemplate.execute("truncate approval_fields");
        approvalFieldRepository.resetAllCache();
    }
}