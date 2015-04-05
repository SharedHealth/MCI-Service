package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.exception.FacilityNotFoundException;
import org.sharedhealth.mci.web.infrastructure.persistence.FacilityRepository;
import org.sharedhealth.mci.web.infrastructure.registry.FacilityRegistryClient;
import org.sharedhealth.mci.web.mapper.FacilityResponse;
import org.sharedhealth.mci.web.model.Facility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FacilityService {

    private FacilityRepository allFacilities;
    private FacilityRegistryClient client;
    private MCIProperties mciProperties;

    @Autowired
    public FacilityService(FacilityRepository facilityRepository, FacilityRegistryClient facilityRegistryClient,
                           MCIProperties mciProperties) {
        this.allFacilities = facilityRepository;
        this.client = facilityRegistryClient;
        this.mciProperties = mciProperties;
    }

    public Facility ensurePresent(final String facilityId) {
        Facility facility = allFacilities.find(facilityId);
        if (facility == null) {
            facility = map(client.find(facilityId));
            allFacilities.save(facility, mciProperties.getFrCacheTtl());
        }
        return facility;
    }

    public Facility find(String facilityId) throws FacilityNotFoundException {
        Facility facility = ensurePresent(facilityId);
        if (facility == null) {
            throw new FacilityNotFoundException("No facility found with id " + facilityId);
        }
        return facility;
    }

    Facility map(FacilityResponse facilityResponse) {
        if (facilityResponse == null) {
            return null;
        }
        Facility facility = new Facility();
        facility.setId(facilityResponse.getId());
        facility.setName(facilityResponse.getName());
        facility.setType(facilityResponse.getType());
        facility.setCatchments(facilityResponse.getCatchments());
        facility.setLocation(facilityResponse.getGeoCode());
        return facility;
    }
}
