package org.sharedhealth.mci.web.handler;

import org.apache.log4j.Logger;
import org.sharedhealth.mci.utils.DateUtil;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;

@Component
public class LocationDataSync {

    private final Logger logger = Logger.getLogger(LocationDataSync.class);

    private AsyncRestTemplate mciRestTemplate;
    private MCIProperties mciProperties;

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
        header.add(AUTH_TOKEN_KEY, mciProperties.getIdpAuthToken());
        header.add(CLIENT_ID_KEY, mciProperties.getIdpClientId());
        return new HttpEntity(header);
    }

    public boolean syncLRData(String uri, String type) {

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
            return false;
        }

        return true;
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
        return DateUtil.toIsoFormat(new Date());
    }

}
