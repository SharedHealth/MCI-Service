package org.sharedhealth.mci.web.service;

import java.util.List;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.exception.FacilityNotFoundException;
import org.sharedhealth.mci.web.infrastructure.fr.FacilityRegistryWrapper;
import org.sharedhealth.mci.web.infrastructure.persistence.FacilityRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.model.Facility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FacilityService {


    private FacilityRepository allFacilities;
    private FacilityRegistryWrapper client;
    private MCIProperties mciProperties;

    @Autowired
    public FacilityService(FacilityRepository facilityRepository, FacilityRegistryWrapper facilityRegistryWrapper, MCIProperties mciProperties) {
        this.allFacilities = facilityRepository;
        this.client = facilityRegistryWrapper;
        this.mciProperties = mciProperties;
    }

    public Facility ensurePresent(final String facilityId) {

        Facility facility = allFacilities.find(facilityId);

        if(facility == null) {
            facility = client.find(facilityId);
            allFacilities.save(facility, mciProperties.getFrCacheTtl());
        }

        return facility;
    }

    public List<Catchment> getCatchmentAreasByFacility(String facilityId) throws FacilityNotFoundException {
        Facility facility;
        facility = ensurePresent(facilityId);

        if(facility == null) {
            throw new FacilityNotFoundException();
        }

        return facility.getCatchmentsList();
    }

}
