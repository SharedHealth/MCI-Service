package org.sharedhealth.mci.web.handler;

import org.apache.log4j.Logger;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.mapper.LocationData;
import org.sharedhealth.mci.web.model.LRMarker;
import org.sharedhealth.mci.web.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class LocationDataSync {

    private final Logger logger = Logger.getLogger(LocationDataSync.class);
    public static final String AUTH_KEY = "X-Auth-Token";

    private AsyncRestTemplate mciRestTemplate;
    private MCIProperties mciProperties;

    public static final String LR_DIVISION_URI_PATH = "/list/division";
    public static final String LR_DISTRICT_URI_PATH = "/list/district";
    public static final String LR_UPAZILA_URI_PATH = "/list/upazila";
    public static final String LR_PAURASAVA_PATH = "/list/paurasava";
    public static final String LR_UNION_URI_PATH = "/list/union";
    public static final String LR_WARD_URI_PATH = "/list/ward";
    public static final String DIVISION_TYPE = "DIVISION";

    public static final String DISTRICT_TYPE = "DISTRICT";
    public static final String UPAZILA_TYPE = "UPAZILA";
    public static final String PAURASAVA_TYPE = "PAURASAVA";
    public static final String UNION_TYPE = "UNION";
    public static final String WARD_TYPE = "WARD";

    private static final int DEFAULT_LIMIT = 100;
    private static final String EXTRA_FILTER_PATTERN = "?limit=%s";
    private static final String EXTRA_FILTER_PATTERN_WITH_UPDATED_SINCE = "?limit=%s&updatedSince=%s";

    private LocationService locationService;


    @Autowired
    public LocationDataSync(@Qualifier("MCIRestTemplate") AsyncRestTemplate mciRestTemplate, MCIProperties mciProperties, LocationService locationService) {
        this.mciRestTemplate = mciRestTemplate;
        this.mciProperties = mciProperties;
        this.locationService = locationService;
    }

    private HttpEntity getHttpEntityWithAuthenticationHeader() {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(AUTH_KEY, mciProperties.getLocaitonRegistryToken());
        return new HttpEntity(header);
    }

    public void sync() throws IOException {
        syncLRData(LR_DIVISION_URI_PATH, DIVISION_TYPE);
        syncLRData(LR_DISTRICT_URI_PATH, DISTRICT_TYPE);
        syncLRData(LR_UPAZILA_URI_PATH, UPAZILA_TYPE);
        syncLRData(LR_PAURASAVA_PATH, PAURASAVA_TYPE);
        syncLRData(LR_UNION_URI_PATH, UNION_TYPE);
        syncLRData(LR_WARD_URI_PATH, WARD_TYPE);
    }

    private void syncLRData(String uri, String type) {

        List<LocationData> lastRetrieveList;

        String updatedSince = getUpdatedSince(type);

        String url = getCompleteUrl(uri, updatedSince, DEFAULT_LIMIT);
        try {
            lastRetrieveList = getNextChunkOfDataFromLR(url);
            if (lastRetrieveList != null && lastRetrieveList.size() > 0) {
                locationService.saveOrUpdateLocationData(lastRetrieveList);
                updatedSince = lastRetrieveList.get(lastRetrieveList.size() - 1).getUpdatedAt();
                locationService.saveOrUpdateLRMarkerData(type, updatedSince);
            } else {
                locationService.saveOrUpdateLRMarkerData(type, getCurrentDateTime());
            }
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private List<LocationData> getNextChunkOfDataFromLR(String url) throws ExecutionException, InterruptedException {

        ListenableFuture<ResponseEntity<LocationData[]>> listenableFuture = mciRestTemplate.exchange(
                url,
                HttpMethod.GET,
                getHttpEntityWithAuthenticationHeader(),
                LocationData[].class);

        ResponseEntity<LocationData[]> responseEntity = listenableFuture.get();
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return Arrays.asList(responseEntity.getBody());
        }

        return null;
    }

    private String getCompleteUrl(String uri, String updatedSince, int limit) {
        return mciProperties.getLocaitonRegistryUrl() + uri + getExtraFilter(updatedSince, limit);
    }

    private String getExtraFilter(String updatedSince, int limit) {
        if (updatedSince != null) {

            return String.format(EXTRA_FILTER_PATTERN_WITH_UPDATED_SINCE, limit, updatedSince);
        }

        return String.format(EXTRA_FILTER_PATTERN, limit);
    }

    private String getUpdatedSince(String type) {

        LRMarker lrMarker = locationService.getLRMarkerData(type);

        if (lrMarker != null) {
            return lrMarker.getLastSync();
        }

        return null;
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date());
    }

}
