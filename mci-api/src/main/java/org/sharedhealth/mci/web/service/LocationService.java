package org.sharedhealth.mci.web.service;


import org.sharedhealth.mci.web.infrastructure.persistence.LocationRepository;
import org.sharedhealth.mci.web.mapper.LocationCriteria;
import org.sharedhealth.mci.web.mapper.LocationData;
import org.sharedhealth.mci.web.model.LRMarker;
import org.sharedhealth.mci.web.model.Location;
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

    public Location saveOrUpdateLocationData(LocationData locationData) {
        return locationRepository.saveOrUpdateLocationData(locationData);
    }

    public LRMarker getLRMarkerData(String type) {
        return locationRepository.getLRMarkerData(type);
    }

    public boolean saveOrUpdateLRMarkerData(String type, String lastSync, int offset) {
        return locationRepository.saveOrUpdateLRMarkerData(type, lastSync, offset);
    }
}
