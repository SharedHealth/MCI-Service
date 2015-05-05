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
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;
import static org.sharedhealth.mci.web.utils.URLParser.parseURL;

@Component
public class LocationDataSync {

    private final Logger logger = Logger.getLogger(LocationDataSync.class);

    private AsyncRestTemplate mciRestTemplate;
    private MCIProperties mciProperties;

    private static final int DEFAULT_LIMIT = 100;
    private static final int INITIAL_OFFSET = 0;
    private static final String OFFSET = "offset";
    private static final String UPDATED_SINCE = "updatedSince";
    private static final String INITIAL_UPDATED_SINCE = "0000-00-00";
    private static final String EXTRA_FILTER_PATTERN = "?offset=%s&limit=%s&updatedSince=%s";

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

    public boolean syncLRData(String uri, String type) throws IOException {

        List<LocationData> lastRetrieveList;
        int offset;
        String updatedSince;

        String lastFeedUrl = getLastFeedUri(type);

        if (lastFeedUrl != null) {
            Map<String, String> parameters = parseURL(new URL(lastFeedUrl));
            offset = Integer.parseInt(parameters.get(OFFSET));
            updatedSince = parameters.get(UPDATED_SINCE);
        } else {
            offset = INITIAL_OFFSET;
            updatedSince = INITIAL_UPDATED_SINCE;
        }

        String url = getCompleteUrl(uri, offset, DEFAULT_LIMIT, updatedSince);

        try {
            lastRetrieveList = getNextChunkOfDataFromLR(url);
            if (lastRetrieveList != null && lastRetrieveList.size() > 0) {
                locationService.saveOrUpdateLocationData(lastRetrieveList);
                updatedSince = lastRetrieveList.get(lastRetrieveList.size() - 1).getUpdatedAt();

                if (lastRetrieveList.size() == DEFAULT_LIMIT) {
                    url = getCompleteUrl(uri, offset + DEFAULT_LIMIT, DEFAULT_LIMIT, updatedSince);
                } else {
                    url = getCompleteUrl(uri, INITIAL_OFFSET, DEFAULT_LIMIT, updatedSince);
                }
                locationService.saveOrUpdateLRMarkerData(type, url);
            }
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
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

    private String getCompleteUrl(String uri, int offset, int limit, String updatedSince) {
        return mciProperties.getLocaitonRegistryUrl() + uri + getExtraFilter(offset, limit, updatedSince);
    }

    private String getExtraFilter(int offset, int limit, String updatedSince) {
        return String.format(EXTRA_FILTER_PATTERN, offset, limit, updatedSince);
    }

    private String getLastFeedUri(String type) {

        LRMarker lrMarker = locationService.getLRMarkerData(type);

        if (lrMarker != null) {
            return lrMarker.getLastFeedUrl();
        }
        return null;
    }

}
