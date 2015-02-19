package org.sharedhealth.mci.web.service;

import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sharedhealth.mci.web.infrastructure.persistence.ApprovalFieldRepository;
import org.sharedhealth.mci.web.model.ApprovalField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.utils.JsonConstants.GENDER;

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
