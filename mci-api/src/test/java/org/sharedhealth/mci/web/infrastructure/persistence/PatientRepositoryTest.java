package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class PatientRepositoryTest {

    private PatientRepository patientRepository;

    @Before
    public void setUp() throws Exception {
        patientRepository = new PatientRepository(null, null, null);
    }

    @Test
    public void shouldFindLatestUuid() throws Exception {
        UUID uuid = null;
        Map<UUID, String> map = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            uuid = UUIDs.timeBased();
            map.put(uuid, "test");
            Thread.sleep(0, 10);
        }
        assertEquals(uuid, patientRepository.findLatestUuid(map));
    }
}
