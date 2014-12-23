package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.Before;
import org.junit.Test;
import org.sharedhealth.mci.web.mapper.PendingApproval;
import org.sharedhealth.mci.web.mapper.PendingApprovalFieldDetails;

import java.util.TreeMap;
import java.util.TreeSet;
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
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();

        for (int i = 0; i < 5; i++) {
            uuid = UUIDs.timeBased();
            PendingApproval pendingApproval = new PendingApproval();
            pendingApproval.setName("name" + i);

            TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
            fieldDetailsMap.put(uuid, new PendingApprovalFieldDetails());
            pendingApproval.setFieldDetails(fieldDetailsMap);

            pendingApprovals.add(pendingApproval);
            Thread.sleep(0, 10);
        }
        assertEquals(uuid, patientRepository.findLatestUuid(pendingApprovals));
    }
}
