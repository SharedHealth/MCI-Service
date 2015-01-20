package org.sharedhealth.mci.web.infrastructure.fr;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.mapper.FacilityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;
import org.springframework.web.client.AsyncRestTemplate;
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

    public ListenableFuture<FacilityResponse> getFacility(String facilityId) {

        return new ListenableFutureAdapter<FacilityResponse, ResponseEntity<FacilityResponse>>(mciRestTemplate.exchange(
                mciProperties.getFacilityRegistryUrl() + "/" + facilityId + ".json",
                HttpMethod.GET,
                getHttpEntityWithAuthenticationHeader(),
                FacilityResponse.class)) {
            @Override
            protected FacilityResponse adapt(ResponseEntity<FacilityResponse> result) throws ExecutionException {
                if (result.getStatusCode().is2xxSuccessful()) {
                    return result.getBody();
                } else {
                    return null;
                }
            }
        };
    }

    public org.sharedhealth.mci.web.model.Facility find(String facilityId) {
        FacilityResponse facility;
        ListenableFuture<FacilityResponse> facilityResponse = getFacility(facilityId);

        try {
            facility = facilityResponse.get();
        } catch (Exception e) {
            return null;
        }

        return this.map(facility);
    }

    private org.sharedhealth.mci.web.model.Facility map(FacilityResponse facility) {

        if(facility == null) {
            return null;
        }

        org.sharedhealth.mci.web.model.Facility facilityEntity = new org.sharedhealth.mci.web.model.Facility();
        facilityEntity.setId(facility.getId());
        facilityEntity.setName(facility.getName());
        facilityEntity.setType(facility.getType());
        facilityEntity.setCatchments(facility.getCatchments());
        facilityEntity.setLocation(facility.getGeoCode());

        return facilityEntity;

    }
}
