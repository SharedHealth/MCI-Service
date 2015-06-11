package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.exception.HealthIdExhaustedException;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.model.HealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class HealthIdRepositoryIT {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cqlTemplate;

    @Autowired
    private HealthIdRepository healthIdRepository;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        cqlTemplate.execute("truncate healthId");
        healthIdRepository.resetLastReservedHealthId();
    }

    @After
    public void tearDown() {
        cqlTemplate.execute("truncate healthId");
    }

    private void createHealthIds(long prefix) {
        for (int i = 0; i < 10; i++) {
            healthIdRepository.saveHealthIdSync(new HealthId(
                    String.valueOf(prefix + i), "MCI", 0));
        }
    }

    @Test(expected = HealthIdExhaustedException.class)
    public void shouldGetExceptionIfIdsAreNotGeneratedBeforeFetch() throws ExecutionException, InterruptedException {
        healthIdRepository.getNextBlock();
    }

    @Test(expected = HealthIdExhaustedException.class)
    public void shouldGetExceptionIfIdsAreExhausted() throws ExecutionException, InterruptedException {
        long prefix = 98190001231L;
        createHealthIds(prefix);
        healthIdRepository.getNextBlock();
        healthIdRepository.getNextBlock();
    }

    @Test
    public void shouldGetBlockFirstTime() throws ExecutionException, InterruptedException {
        long prefix = 98190001231L;
        createHealthIds(prefix);
        assertNotNull(healthIdRepository.getNextBlock(2));
    }

    @Test
    public void shouldMarkHidUsed() throws ExecutionException, InterruptedException {
        long prefix = 98190001231L;
        createHealthIds(prefix);
        List<HealthId> nextBlock = healthIdRepository.getNextBlock(2);
        HealthId healthId = nextBlock.remove(0);
        healthIdRepository.markUsedSync(healthId);
        HealthId id = healthIdRepository.getHealthId(healthId.getHid());
        assertEquals(healthId.getHid(), id.getHid());
        assertEquals("1", String.valueOf(id.getStatus()));
    }

    @Test
    public void shouldGetANewBlockEveryTime() throws ExecutionException, InterruptedException {
        long prefix = 98190001231L;
        createHealthIds(prefix);
        List<HealthId> healthIds = healthIdRepository.getNextBlock(2);
        String lastReservedHealthId = healthIdRepository.getLastReservedHealthId();
        assertFalse(lastReservedHealthId == null);
        List<HealthId> nextBlock = healthIdRepository.getNextBlock(2);
        assertFalse(lastReservedHealthId == healthIdRepository.getLastReservedHealthId());
        for (HealthId healthId : nextBlock) {
            assertFalse(healthIds.contains(healthId));
        }
    }
}

