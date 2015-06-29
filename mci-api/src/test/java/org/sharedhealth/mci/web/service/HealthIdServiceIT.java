package org.sharedhealth.mci.web.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.model.HealthId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class HealthIdServiceIT {
    private static final Logger logger = LoggerFactory.getLogger(HealthIdServiceIT.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cqlTemplate;

    @Autowired
    private HealthIdRepository healthIdRepository;

    @Autowired
    private HealthIdService healthIdService;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        cqlTemplate.execute("truncate healthId");
        healthIdRepository.resetLastReservedHealthId();
        createHealthIds(9800000000L);
    }

    @After
    public void cleanIp() throws Exception {
        cqlTemplate.execute("truncate healthId");
    }

    @Test
    public void shouldGenerateUniqueBlock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        final Set<Future<List<HealthId>>> eventualHealthIds = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            Callable<List<HealthId>> nextBlock = new Callable<List<HealthId>>() {
                @Override
                public List<HealthId> call() throws Exception {
                    return healthIdService.getNextBlock(2);
                }
            };
            Future<List<HealthId>> eventualHealthId = executor.submit(nextBlock);
            eventualHealthIds.add(eventualHealthId);
        }
        Set<HealthId> uniqueHealthIds = new HashSet<>();

        for (Future<List<HealthId>> eventualHealthId : eventualHealthIds) {
            uniqueHealthIds.addAll(eventualHealthId.get());
        }
        assertEquals(200, uniqueHealthIds.size());
    }

    private void createHealthIds(long prefix) {
        logger.debug("generating health Id for test");
        for (int i = 0; i < 200; i++) {
            healthIdRepository.saveHealthIdSync(new HealthId(
                    String.valueOf(prefix + i), "MCI", 0));
        }
    }
}
