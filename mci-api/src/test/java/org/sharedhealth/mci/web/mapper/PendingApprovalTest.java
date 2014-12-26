package org.sharedhealth.mci.web.mapper;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

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

    @Test
    public void shouldFindIfValuePresent() {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName("f_address");
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();

        Address address = new Address("1", "2", "3");
        fieldDetailsMap.put(UUIDs.timeBased(), buildFieldDetails(address));

        String city = "Bangalore";
        fieldDetailsMap.put(UUIDs.timeBased(), buildFieldDetails(city));

        pendingApproval.setFieldDetails(fieldDetailsMap);

        assertTrue(pendingApproval.contains(address));
        assertTrue(pendingApproval.contains(city));
        assertFalse(pendingApproval.contains(new Address()));
    }

    private PendingApprovalFieldDetails buildFieldDetails(Object value) {
        PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
        fieldDetails.setValue(value);
        return fieldDetails;
    }
}