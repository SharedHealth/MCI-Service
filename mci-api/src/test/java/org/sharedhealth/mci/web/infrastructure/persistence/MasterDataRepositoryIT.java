package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.model.MasterData;
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

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class MasterDataRepositoryIT {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cqlTemplate;

    @Autowired
    private MasterDataRepository masterDataRepository;

    private MasterData masterData;
    private String type = "relations";
    private String key = "FTH";
    private String value = "father";

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        masterData = new MasterData(type, key, value);
    }

    @Test
    public void shouldReturnNull_IfDataDoesNotExistForGivenKeyForValidType() throws ExecutionException, InterruptedException {
        assertNull(masterDataRepository.findDataByKey(type, "random string"));
    }

    @Test
    public void shouldFindDataWithMatchingKeyType() throws ExecutionException, InterruptedException {
        final MasterData m = masterDataRepository.findDataByKey(type, key);

        assertNotNull(m);
        assertEquals(masterData, m);
    }
}