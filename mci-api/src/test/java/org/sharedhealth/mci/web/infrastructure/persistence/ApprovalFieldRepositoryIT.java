package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.model.ApprovalField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.sharedhealth.mci.web.utils.JsonConstants.GENDER;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class ApprovalFieldRepositoryIT {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cqlTemplate;

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