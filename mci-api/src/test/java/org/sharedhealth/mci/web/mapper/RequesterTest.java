package org.sharedhealth.mci.web.mapper;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RequesterTest {

    @Test
    public void shouldSortRequesters() {
        Requester requester3 = new Requester("f1", "p1");
        Requester requester2 = new Requester("f1", "p2");
        Requester requester5 = new Requester("f1", "p3");

        Requester requester1 = new Requester(new RequesterDetails("f2"), null, null);
        Requester requester4 = new Requester(null, new RequesterDetails("p1"), null);

        Requester requester6 = new Requester("f4", "p4");
        Requester requester7 = new Requester("f4", "p4");

        Requester requester8 = new Requester(null, null, new RequesterDetails("admin"));
        Requester requester9 = new Requester(null, null, new RequesterDetails("admin"));

        Set<Requester> requesters = new HashSet<>();
        requesters.add(requester1);
        requesters.add(requester1);
        requesters.add(requester2);
        requesters.add(requester3);
        requesters.add(requester4);
        requesters.add(requester4);
        requesters.add(requester5);
        requesters.add(requester6);
        requesters.add(requester7);
        requesters.add(requester7);
        requesters.add(requester8);
        requesters.add(requester9);

        assertEquals(7, requesters.size());

        assertTrue(requesters.contains(requester1));
        assertTrue(requesters.contains(requester2));
        assertTrue(requesters.contains(requester3));
        assertTrue(requesters.contains(requester4));
        assertTrue(requesters.contains(requester5));
        assertTrue(requesters.contains(requester6));
        assertTrue(requesters.contains(requester8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenBothFacilityAndProviderAreEmpty() {
        new Requester(null, "");
    }
}