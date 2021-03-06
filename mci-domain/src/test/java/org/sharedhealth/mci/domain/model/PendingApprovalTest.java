package org.sharedhealth.mci.domain.model;

import org.junit.Test;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;

import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.domain.constant.JsonConstants.PRESENT_ADDRESS;

public class PendingApprovalTest {

    @Test
    public void shouldSetDetailsInDescendingOrderOfTimestampsEvenWhenTheUuidsAreInAscendingOrder() {
        PendingApproval pendingApproval = new PendingApproval();
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetails = new TreeMap<>();
        pendingApproval.setName("given_name");

        // 2015-02-13T05:44:08.673Z MSB: 6075625349398598116
        String uuidString1 = "5450f510-b343-11e4-845f-238d247bf04a";
        fieldDetails.put(fromString(uuidString1), buildFieldDetails("one"));

        // 2015-02-13T05:45:27.533Z  MSB: -8984107514685353500
        String uuidString2 = "835209d0-b343-11e4-8b78-238d247bf04a";
        fieldDetails.put(fromString(uuidString2), buildFieldDetails("two"));
        pendingApproval.addFieldDetails(fieldDetails);

        // Added to the map in descending order of UUID's unix timestamps
        Iterator<UUID> iterator = pendingApproval.getFieldDetails().keySet().iterator();
        UUID uuid1 = iterator.next(); // uuidString2
        UUID uuid2 = iterator.next(); // uuidString1

        // MSB of uuid1 is lesser than that of uuid2
        assertEquals(-1, uuid1.compareTo(uuid2));

        // Timestamp of uuid1 is later than that of uuid2
        Long timestamp1 = TimeUuidUtil.getTimeFromUUID(uuid1);
        Long timestamp2 = TimeUuidUtil.getTimeFromUUID(uuid2);
        assertEquals(1, timestamp1.compareTo(timestamp2));
        assertTrue(new Date(timestamp1).after(new Date(timestamp2)));
    }

    @Test
    public void shouldSetDetailsInDescendingOrderOfTimestampsOrUuidsForGeneratedUuids() {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName("given_name");
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetails = new TreeMap<>();
        for (int i = 0; i < 5; i++) {
            fieldDetails.put(TimeUuidUtil.uuidForDate(new Date()), buildFieldDetails(i));
        }
        pendingApproval.addFieldDetails(fieldDetails);
        assertEquals(5, pendingApproval.getFieldDetails().size());

        Date date1 = null;
        UUID uuid1 = null;
        for (UUID uuid2 : pendingApproval.getFieldDetails().keySet()) {
            Date date2 = new Date(TimeUuidUtil.getTimeFromUUID(uuid2));
            if (uuid1 != null) {
                if (date1.equals(date2)) {
                    assertEquals(1, uuid1.compareTo(uuid2));
                } else {
                    assertTrue(date1.after(date2));
                }
            }
            uuid1 = uuid2;
            date1 = date2;
        }
        assertNotNull(uuid1);
        assertNotNull(date1);
    }

    @Test
    public void shouldFindIfValuePresent() {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(PRESENT_ADDRESS);
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();

        Address address1 = new Address("1", "2", "3");
        fieldDetailsMap.put(TimeUuidUtil.uuidForDate(new Date()), buildFieldDetails(address1));

        Address address2 = new Address("1", "2", "5");
        fieldDetailsMap.put(TimeUuidUtil.uuidForDate(new Date()), buildFieldDetails(address2));

        pendingApproval.addFieldDetails(fieldDetailsMap);

        assertTrue(pendingApproval.contains(address1));
        assertTrue(pendingApproval.contains(address2));
        assertFalse(pendingApproval.contains(new Address()));
    }

    @Test
    public void shouldNotAddFieldDetailsIfValueExists() throws Exception {
        PendingApproval pendingApproval = new PendingApproval();
        pendingApproval.setName(PRESENT_ADDRESS);
        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = new TreeMap<>();

        Address address1 = new Address("1", "2", "3");
        UUID createdAt = TimeUuidUtil.uuidForDate(new Date());
        fieldDetailsMap.put(createdAt, buildFieldDetails(address1));

        Address address2 = new Address("1", "2", "3");
        fieldDetailsMap.put(TimeUuidUtil.uuidForDate(new Date()), buildFieldDetails(address2));

        pendingApproval.addFieldDetails(fieldDetailsMap);

        assertEquals(1, pendingApproval.getFieldDetails().size());
        assertEquals(createdAt, pendingApproval.getFieldDetails().firstKey());
    }

    private PendingApprovalFieldDetails buildFieldDetails(Object value) {
        PendingApprovalFieldDetails fieldDetails = new PendingApprovalFieldDetails();
        fieldDetails.setValue(value);
        return fieldDetails;
    }
}