package org.sharedhealth.mci.domain.service;


import org.sharedhealth.mci.domain.model.LocationData;
import org.sharedhealth.mci.domain.repository.LocationCriteria;
import org.sharedhealth.mci.domain.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocationService {

    private LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public LocationData findByGeoCode(String geoCode) {
        return locationRepository.findByGeoCode(geoCode);
    }

    public List<LocationData> findLocationsByParent(LocationCriteria locationCriteria) {
        return locationRepository.findLocationsByParent(locationCriteria);
    }
}
