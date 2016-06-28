package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.util.BaseIntegrationTest;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
public class HealthIdServiceIT extends BaseIntegrationTest {

    @Autowired
    private HealthIdRepository healthIdRepository;

    @Autowired
    private HealthIdService healthIdService;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        cassandraOps.execute("truncate mci_healthId");
        healthIdRepository.resetLastReservedHealthId();
    }

    @Ignore
    @Test
    public void shouldGenerateUniqueBlock() throws Exception {
        createHealthIds(9800000000L);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        final Set<Future<List<MciHealthId>>> eventualHealthIds = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            Callable<List<MciHealthId>> nextBlock = new Callable<List<MciHealthId>>() {
                @Override
                public List<MciHealthId> call() throws Exception {
                    return healthIdService.getNextBlock(2);
                }
            };
            Future<List<MciHealthId>> eventualHealthId = executor.submit(nextBlock);
            eventualHealthIds.add(eventualHealthId);
        }
        Set<MciHealthId> uniqueMciHealthIds = new HashSet<>();

        for (Future<List<MciHealthId>> eventualHealthId : eventualHealthIds) {
            uniqueMciHealthIds.addAll(eventualHealthId.get());
        }
        assertEquals(200, uniqueMciHealthIds.size());
    }

    private void createHealthIds(long prefix) {
        for (int i = 0; i < 200; i++) {
            healthIdRepository.saveMciHealthIdSync(new MciHealthId(String.valueOf(prefix + i)));
        }
    }
}
