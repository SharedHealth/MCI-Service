package org.sharedhealth.mci.web.service;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.fr.FacilityRegistryWrapper;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PendingApprovalResponse;
import org.sharedhealth.mci.web.model.PendingApprovalMapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    FacilityRegistryWrapper facilityRegistryWrapper;
    @Mock
    SettingService settingService;

    private PatientService patientService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        patientService = new PatientService(patientRepository, facilityRegistryWrapper, settingService);
    }

    @Test
    public void shouldFindPendingApprovals() throws Exception {
        Catchment catchment = new Catchment("10", "20", "30");
        UUID since = UUIDs.timeBased();

        List<PendingApprovalMapping> mappings = asList(buildPendingApprovalMapping("hid-100"),
                buildPendingApprovalMapping("hid-200"),
                buildPendingApprovalMapping("hid-300"));
        Collections.reverse(mappings);
        when(settingService.getSettingAsIntegerByKey("PER_PAGE_MAXIMUM_LIMIT")).thenReturn(25);
        when(patientRepository.findPendingApprovalMapping(catchment, since, 25)).thenReturn(mappings);

        List<PatientData> patients = asList(buildPatient("hid-300"),
                buildPatient("hid-200"),
                buildPatient("hid-100"));
        when(patientRepository.findByHealthId(asList("hid-300", "hid-200", "hid-100"))).thenReturn(patients);

        PendingApprovalResponse response = patientService.findPendingApprovals(catchment, since);

        InOrder inOrder = inOrder(patientRepository);
        inOrder.verify(patientRepository).findPendingApprovalMapping(catchment, since, 25);
        inOrder.verify(patientRepository).findByHealthId(asList("hid-300", "hid-200", "hid-100"));

        assertNotNull(response);
        assertEquals(mappings.get(0).getCreatedAt(), response.getUntil());
        assertNotNull(response.getPendingApprovals());
        assertEquals(3, response.getPendingApprovals().size());

        Map<String, String> metadata = response.getPendingApprovals().get(0);
        String healthId = metadata.get(HID);
        assertNotNull(healthId);
        assertEquals("Scott-" + healthId, metadata.get(GIVEN_NAME));
        assertEquals("Tiger-" + healthId, metadata.get(SUR_NAME));
    }

    private PendingApprovalMapping buildPendingApprovalMapping(String healthId) throws InterruptedException {
        PendingApprovalMapping mapping = new PendingApprovalMapping();
        mapping.setHealthId(healthId);
        mapping.setDivisionId("10");
        mapping.setDistrictId("20");
        mapping.setUpazilaId("30");
        mapping.setCreatedAt(UUIDs.timeBased());
        Thread.sleep(0, 10);
        return mapping;
    }

    private PatientData buildPatient(String healthId) {
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);
        patient.setGivenName("Scott-" + healthId);
        patient.setSurName("Tiger-" + healthId);
        return patient;
    }
}