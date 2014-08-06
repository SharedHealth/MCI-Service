package org.sharedhealth.mci.web.service;


import org.sharedhealth.mci.web.infrastructure.persistence.LocationRepository;
import org.sharedhealth.mci.web.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Component
public class LocationService {

    private LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public ListenableFuture<Location> findByGeoCode(String geoCode) {
        return locationRepository.findByGeoCode(geoCode);
    }
}
