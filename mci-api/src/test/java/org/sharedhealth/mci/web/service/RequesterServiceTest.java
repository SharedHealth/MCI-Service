package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.domain.model.Requester;
import org.sharedhealth.mci.web.mapper.ProviderResponse;
import org.sharedhealth.mci.web.model.Facility;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RequesterServiceTest {

    @Mock
    private FacilityService facilityService;
    @Mock
    private ProviderService providerService;

    private RequesterService requesterService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        requesterService = new RequesterService(facilityService, providerService);
    }

    @Test
    public void shouldPopulateRequesterDetails() throws Exception {
        String facilityId = "f100";
        String facilityName = "Bahmni";
        String providerId = "p100";
        String providerName = "Dr. Monika";

        Facility facility = new Facility(facilityId, facilityName, null, null, null);
        ProviderResponse provider = new ProviderResponse();
        provider.setId(providerId);
        provider.setName(providerName);

        when(facilityService.find(facilityId)).thenReturn(facility);
        when(providerService.find(providerId)).thenReturn(provider);

        Requester requester = new Requester(facilityId, providerId);
        requesterService.populateRequesterDetails(requester);

        assertNotNull(requester.getFacility());
        assertEquals(facilityId, requester.getFacility().getId());
        assertEquals(facilityName, requester.getFacility().getName());

        assertNotNull(requester.getProvider());
        assertEquals(providerId, requester.getProvider().getId());
        assertEquals(providerName, requester.getProvider().getName());
    }
}