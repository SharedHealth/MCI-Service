package org.sharedhealth.mci.web.infrastructure.fr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.mapper.Facility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;
import org.springframework.web.client.AsyncRestTemplate;


@Component
public class FacilityRegistryWrapper {

    private static final String API_BASE_URL = "http://pagani.websitewelcome.com/~stagedgh/dghshrml4/public/api/1.0/facilities";

    private AsyncRestTemplate mciRestTemplate;
    private MCIProperties mciProperties;


    @Autowired
    public FacilityRegistryWrapper(@Qualifier("MCIRestTemplate") AsyncRestTemplate shrRestTemplate, MCIProperties mciProperties) {
        this.mciRestTemplate = shrRestTemplate;
        this.mciProperties = mciProperties;
    }

    public ListenableFuture<Facility> getFacility(String facilityId) {
        return new ListenableFutureAdapter<Facility, ResponseEntity<Facility>>(mciRestTemplate.exchange(
                API_BASE_URL + "/" + facilityId,
                HttpMethod.GET,
                new HttpEntity(null),
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

    public List<String> getCatchmentAreasByFacility(String facilityId) {
        ArrayList<String> areas = new ArrayList<>();

        if(facilityId.equals("1")){
            areas.add("1004092004");
            areas.add("1004092005");
            areas.add("1004092006");
            areas.add("1004092007");
        }

        return areas;
    }
}
