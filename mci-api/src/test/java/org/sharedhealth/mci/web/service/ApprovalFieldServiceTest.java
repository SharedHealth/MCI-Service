package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sharedhealth.mci.domain.model.ApprovalField;
import org.sharedhealth.mci.domain.repository.ApprovalFieldRepository;
import org.sharedhealth.mci.domain.service.ApprovalFieldService;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.constant.JsonConstants.GENDER;

public class ApprovalFieldServiceTest {

    @Mock
    private ApprovalFieldRepository approvalFieldRepository;

    private ApprovalFieldService approvalFieldService;

    @Before
    public void setUp() {
        initMocks(this);
        approvalFieldService = new ApprovalFieldService(approvalFieldRepository);
    }

    @Test
    public void shouldFindPropertyOptionIfFieldExist() throws ExecutionException,
            InterruptedException {
        ApprovalField approvalField = new ApprovalField(GENDER, "NA");

        Mockito.when(approvalFieldRepository.findByField(GENDER)).thenReturn(approvalField);
        String property = approvalFieldService.getProperty(GENDER);

        assertEquals("NA", property);
    }

    @Test
    public void shouldReturnNullIfFieldNotFound() throws ExecutionException,
            InterruptedException {

        Mockito.when(approvalFieldRepository.findByField(GENDER)).thenReturn(null);
        assertNull(approvalFieldService.getProperty("ANY_STRING"));
    }
}
