package org.sharedhealth.mci.web.infrastructure.security;

import org.junit.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sharedhealth.mci.web.infrastructure.security.UserInfo.*;
import static org.sharedhealth.mci.web.infrastructure.security.UserProfile.ADMIN_TYPE;
import static org.sharedhealth.mci.web.infrastructure.security.UserProfile.FACILITY_TYPE;
import static org.sharedhealth.mci.web.infrastructure.security.UserProfile.PATIENT_TYPE;
import static org.sharedhealth.mci.web.infrastructure.security.UserProfile.PROVIDER_TYPE;

public class UserInfoTest {

    @Test
    public void shouldNotAddUserGroupsIfItDoesNotHaveMciUserGroup() throws Exception {
        UserProfile userProfile = new UserProfile(FACILITY_TYPE, "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(FACILITY_ADMIN_GROUP)), asList(userProfile));
        assertFalse(userInfo.getProperties().getGroups().contains(FACILITY_GROUP));
    }

    @Test
    public void shouldAddFacilityGroupIfUserProfileHasFacilityAndGroupHasFacilityAdmin() throws Exception {
        UserProfile userProfile = new UserProfile(FACILITY_TYPE, "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(MCI_USER_GROUP, FACILITY_ADMIN_GROUP)), asList(userProfile));
        assertTrue(userInfo.getProperties().getGroups().contains(FACILITY_GROUP));
    }

    @Test
    public void shouldNotAddFacilityGroupIfUserProfileNotHaveFacilityAndGroupNotHaveFacilityAdmin() throws Exception {
        UserProfile userProfile = new UserProfile(FACILITY_TYPE, "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(userProfile));
        assertFalse(userInfo.getProperties().getGroups().contains(FACILITY_GROUP));

        userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(MCI_USER_GROUP, FACILITY_ADMIN_GROUP)), null);
        assertFalse(userInfo.getProperties().getGroups().contains(FACILITY_GROUP));
    }

    @Test
    public void shouldNotAddFacilityGroupIfItIsSHRAdminFacility() throws Exception {
        UserProfile userProfile = new UserProfile(FACILITY_TYPE, "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(FACILITY_ADMIN_GROUP, SHR_SYSTEM_ADMIN_GROUP)), asList(userProfile));
        assertFalse(userInfo.getProperties().getGroups().contains(FACILITY_GROUP));
    }

    @Test
    public void shouldAddProviderGroupOnlyIfUserProfileHasProvider() throws Exception {
        UserProfile userProfile = new UserProfile("provider", "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(MCI_USER_GROUP)), asList(userProfile));
        assertTrue(userInfo.getProperties().getGroups().contains(PROVIDER_GROUP));

        userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(MCI_USER_GROUP)), null);
        assertFalse(userInfo.getProperties().getGroups().contains(PROVIDER_GROUP));
    }

    @Test
    public void shouldAddPatientGroupOnlyIfUserProfileHasPatient() throws Exception {
        UserProfile userProfile = new UserProfile("patient", "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(MCI_USER_GROUP)), asList(userProfile));
        assertTrue(userInfo.getProperties().getGroups().contains(PATIENT_GROUP));

        userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(MCI_USER_GROUP)), null);
        assertFalse(userInfo.getProperties().getGroups().contains(PATIENT_GROUP));
    }

    @Test
    public void shouldCheckCatchmentsForGivenType() throws Exception {
        UserProfile patientProfile = new UserProfile(PATIENT_TYPE, "100067", null);
        UserProfile facilityProfile = new UserProfile(FACILITY_TYPE, "100067", asList("3026", "4019"));
        UserProfile providerProfile = new UserProfile(PROVIDER_TYPE, "100067", asList("1001"));
        UserProfile adminProfile = new UserProfile(ADMIN_TYPE, "100067", asList("2020"));

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(adminProfile, providerProfile, facilityProfile, patientProfile));

        assertTrue(userInfo.getProperties().hasCatchmentForProfileType("3026", asList(FACILITY_TYPE)));
        assertTrue(userInfo.getProperties().hasCatchmentForProfileType("4019", asList(FACILITY_TYPE)));
        assertFalse(userInfo.getProperties().hasCatchmentForProfileType("1001", asList(FACILITY_TYPE)));

        assertTrue(userInfo.getProperties().hasCatchmentForProfileType("1001", asList(PROVIDER_TYPE)));
        assertFalse(userInfo.getProperties().hasCatchmentForProfileType("3026", asList(PROVIDER_TYPE)));

        assertTrue(userInfo.getProperties().hasCatchmentForProfileType("2020", asList(ADMIN_TYPE)));
        assertFalse(userInfo.getProperties().hasCatchmentForProfileType("3026", asList(ADMIN_TYPE)));

        assertFalse(userInfo.getProperties().hasCatchmentForProfileType("2020", asList(PATIENT_TYPE)));

        assertTrue(userInfo.getProperties().hasCatchmentForProfileType("2020", asList(ADMIN_TYPE, FACILITY_TYPE)));
        assertTrue(userInfo.getProperties().hasCatchmentForProfileType("3026", asList(ADMIN_TYPE, FACILITY_TYPE)));
    }
}