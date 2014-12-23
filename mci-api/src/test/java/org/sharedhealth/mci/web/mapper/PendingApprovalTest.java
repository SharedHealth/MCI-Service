package org.sharedhealth.mci.web.mapper;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PendingApprovalTest {

    @Test
    public void shouldSetDetailsInDescendingOrderOfTimeuuids() throws Exception {
        PendingApproval pendingApproval = new PendingApproval();
        List<UUID> uuids = generateUUIDs();
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetails = new TreeMap<>();
        for (int i = 0; i < 4; i++) {
            fieldDetails.put(uuids.get(i), null);
        }
        pendingApproval.setFieldDetails(fieldDetails);
        Date date1 = null;
        for (UUID uuid : pendingApproval.getFieldDetails().keySet()) {
            Date date2 = new Date(UUIDs.unixTimestamp(uuid));
            if (date1 != null) {
                assertTrue(date1.after(date2));
            }
            date1 = date2;
        }
        assertNotNull(date1);
    }

    private List<UUID> generateUUIDs() throws Exception {
        List<UUID> uuids = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            uuids.add(UUIDs.timeBased());
            Thread.sleep(0, 10);
        }
        return uuids;
    }
}