package org.sharedhealth.mci.web.infrastructure.fr;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.exception.FacilityNotFoundException;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.Facility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.List;
import java.util.concurrent.ExecutionException;


@Component
public class FacilityRegistryWrapper {

    public static final String AUTH_KEY = "X-Auth-Token";
    private AsyncRestTemplate mciRestTemplate;
    private MCIProperties mciProperties;


    @Autowired
    public FacilityRegistryWrapper(@Qualifier("MCIRestTemplate") AsyncRestTemplate shrRestTemplate, MCIProperties mciProperties) {
        this.mciRestTemplate = shrRestTemplate;
        this.mciProperties = mciProperties;
    }

    private HttpEntity getHttpEntityWithAuthenticationHeader() {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(AUTH_KEY, mciProperties.getFacilityRegistryToken());
        return new HttpEntity(header);
    }

    public ListenableFuture<Facility> getFacility(String facilityId) {

        return new ListenableFutureAdapter<Facility, ResponseEntity<Facility>>(mciRestTemplate.exchange(
                mciProperties.getFacilityRegistryUrl() + "/" + facilityId + ".json",
                HttpMethod.GET,
                getHttpEntityWithAuthenticationHeader(),
                Facility.class)) {
            @Override
            protected Facility adapt(ResponseEntity<Facility> result) throws ExecutionException {
                if (result.getStatusCode().is2xxSuccessful()) {
                    return result.getBody();
                } else {
                    return null;
                }
            }
        };
    }

    @Cacheable({"facilities"})
    public List<Catchment> getCatchmentAreasByFacility(String facilityId) throws FacilityNotFoundException {
        Facility facility;
        ListenableFuture<Facility> facilityResponse = getFacility(facilityId);

        try {
            facility = facilityResponse.get();
        } catch (Exception e) {
            throw new FacilityNotFoundException();
        }

        return facility.getCatchments();
    }
}
