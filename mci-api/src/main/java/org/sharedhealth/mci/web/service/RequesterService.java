package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.mapper.ProviderResponse;
import org.sharedhealth.mci.web.mapper.Requester;
import org.sharedhealth.mci.web.mapper.RequesterDetails;
import org.sharedhealth.mci.web.model.Facility;
import org.springframework.beans.factory.annotation.Autowired;

public class RequesterService {

    private FacilityService facilityService;
    private ProviderService providerService;

    @Autowired
    public RequesterService(FacilityService facilityService, ProviderService providerService) {
        this.facilityService = facilityService;
        this.providerService = providerService;
    }

    public void populateRequesterDetails(Requester requester) {
        if (requester == null) {
            return;
        }
        populateFacilityDetails(requester.getFacility());
        populateProviderDetails(requester.getProvider());
    }

    private void populateFacilityDetails(RequesterDetails requesterDetails) {
        if (requesterDetails == null) {
            return;
        }
        Facility facility = facilityService.find(requesterDetails.getId());
        if (facility != null) {
            requesterDetails.setName(facility.getName());
        }
    }

    private void populateProviderDetails(RequesterDetails requesterDetails) {
        if (requesterDetails == null) {
            return;
        }
        ProviderResponse providerResponse = providerService.find(requesterDetails.getId());
        if (providerResponse != null) {
            requesterDetails.setName(providerResponse.getName());
        }
    }
}
