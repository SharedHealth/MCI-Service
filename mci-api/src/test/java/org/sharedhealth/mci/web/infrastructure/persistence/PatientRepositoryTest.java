package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.Before;
import org.junit.Test;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PendingApproval;
import org.sharedhealth.mci.web.mapper.PendingApprovalFieldDetails;

import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.web.utils.JsonConstants.GENDER;
import static org.sharedhealth.mci.web.utils.JsonConstants.OCCUPATION;

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
            uuid = timeBased();
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

    @Test
    public void shouldRemoveEntirePendingApprovalWhenAnyValueIsAccepted() {
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApprovalForOccupation());
        pendingApprovals.add(buildPendingApprovalForGender());

        PatientData patient = new PatientData();
        patient.setOccupation("02");

        TreeSet<PendingApproval> result = patientRepository.updatePendingApprovals(pendingApprovals, patient, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(GENDER, result.iterator().next().getName());
    }

    @Test
    public void shouldRemoveTheMatchingValueWhenRejected() {
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApprovalForOccupation());
        pendingApprovals.add(buildPendingApprovalForGender());

        PatientData patient = new PatientData();
        patient.setOccupation("02");

        TreeSet<PendingApproval> result = patientRepository.updatePendingApprovals(pendingApprovals, patient, false);
        assertNotNull(result);
        assertEquals(2, result.size());

        for (PendingApproval pendingApproval : result) {
            if (pendingApproval.getName().equals(GENDER)) {
                TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
                assertNotNull(fieldDetailsMap);
                assertEquals(1, fieldDetailsMap.size());

            } else if (pendingApproval.getName().equals(OCCUPATION)) {
                TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
                assertNotNull(fieldDetailsMap);
                assertEquals(2, fieldDetailsMap.size());
                for (PendingApprovalFieldDetails fieldDetails : fieldDetailsMap.values()) {
                    assertTrue(asList("01", "03").contains(fieldDetails.getValue()));
                }

            } else {
                fail("Invalid pending approval");
            }
        }
    }

    @Test
    public void shouldRemoveEntirePendingApprovalWhenAllValuesAreRejected() {
        TreeSet<PendingApproval> pendingApprovals = new TreeSet<>();
        pendingApprovals.add(buildPendingApprovalForGender());

        PatientData patient = new PatientData();
        patient.setGender("F");

        TreeSet<PendingApproval> result = patientRepository.updatePendingApprovals(pendingApprovals, patient, false);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    private PendingApproval buildPendingApprovalForOccupation() {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(OCCUPATION);
        pendingApproval.setCurrentValue("00");
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        for (int i = 1; i <= 3; i++) {
            PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
            fieldDetails.setValue("0" + i);
            fieldDetailsMap.put(timeBased(), fieldDetails);
        }
        pendingApproval.setFieldDetails(fieldDetailsMap);
        return pendingApproval;
    }

    private PendingApproval buildPendingApprovalForGender() {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(GENDER);
        pendingApproval.setCurrentValue("M");
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();
        PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
        fieldDetails.setValue("F");
        fieldDetailsMap.put(timeBased(), fieldDetails);
        pendingApproval.setFieldDetails(fieldDetailsMap);
        return pendingApproval;
    }
}
