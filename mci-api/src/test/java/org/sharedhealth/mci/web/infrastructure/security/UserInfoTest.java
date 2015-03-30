package org.sharedhealth.mci.web.infrastructure.security;

import org.junit.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sharedhealth.mci.web.infrastructure.security.UserInfo.*;
import static org.sharedhealth.mci.web.infrastructure.security.UserProfile.FACILITY_TYPE;

public class UserInfoTest {
    @Test
    public void shouldAddFacilityGroupIfUserProfileHasFacilityAndGroupHasFacilityAdmin() throws Exception {
        UserProfile userProfile = new UserProfile(FACILITY_TYPE, "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList("Facility Admin")), asList(userProfile));
        assertTrue(userInfo.getProperties().getGroups().contains(FACILITY_GROUP));
    }

    @Test
    public void shouldNotAddFacilityGroupIfUserProfileNotHaveFacilityAndGroupNotHaveFacilityAdmin() throws Exception {
        UserProfile userProfile = new UserProfile("facility", "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(userProfile));
        assertFalse(userInfo.getProperties().getGroups().contains(FACILITY_GROUP));

        new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(FACILITY_ADMIN_GROUP)), null);
        assertFalse(userInfo.getProperties().getGroups().contains(FACILITY_GROUP));
    }

    @Test
    public void shouldAddProviderGroupOnlyIfUserProfileHasProvider() throws Exception {
        UserProfile userProfile = new UserProfile("provider", "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(userProfile));
        assertTrue(userInfo.getProperties().getGroups().contains(PROVIDER_GROUP));

        userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), null);
        assertFalse(userInfo.getProperties().getGroups().contains(PROVIDER_GROUP));
    }

    @Test
    public void shouldAddPatientGroupOnlyIfUserProfileHasPatient() throws Exception {
        UserProfile userProfile = new UserProfile("patient", "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(userProfile));
        assertTrue(userInfo.getProperties().getGroups().contains(PATIENT_GROUP));

        userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), null);
        assertFalse(userInfo.getProperties().getGroups().contains(PATIENT_GROUP));
    }
}