package org.sharedhealth.mci.web.handler;

import org.apache.log4j.Logger;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.mapper.LocationData;
import org.sharedhealth.mci.web.model.LRMarker;
import org.sharedhealth.mci.web.model.Location;
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
import org.springframework.util.concurrent.ListenableFutureAdapter;
import org.springframework.web.client.AsyncRestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class LocationDataSync {

    private final Logger logger = Logger.getLogger(LocationDataSync.class);
    public static final String AUTH_KEY = "X-Auth-Token";

    private AsyncRestTemplate mciAsyncRestTemplate;
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

    public static final int INITIAL_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 100;
    public static final int MAX_SYNC_DATA_SIZE = 1000;
    private static final String EXTRA_FILTER_PATTERN = "?offset=%s&limit=%s&updatedSince=%s";
    private static final String INITIAL_DATETIME = "0000-00-00 00:00:00";
    private List<String> failedDuringSaveOrUpdateOperation;
    private int noOfEntriesSynchronizedSoFar;

    private LocationService locationService;


    @Autowired
    public LocationDataSync(@Qualifier("MCIRestTemplate") AsyncRestTemplate mciAsyncRestTemplate, MCIProperties mciProperties, LocationService locationService) {
        this.mciAsyncRestTemplate = mciAsyncRestTemplate;
        this.mciProperties = mciProperties;
        this.locationService = locationService;
        this.failedDuringSaveOrUpdateOperation = new ArrayList<>();
        this.noOfEntriesSynchronizedSoFar = 0;
    }

    private HttpEntity getHttpEntityWithAuthenticationHeader() {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(AUTH_KEY, mciProperties.getLocaitonRegistryToken());
        return new HttpEntity(header);
    }

    public void sync() throws IOException {
        List<LocationData> divisions = syncLRData(LR_DIVISION_URI_PATH, DIVISION_TYPE);
        logger.info(divisions.size() + " divisions entries synchronized");
        List<LocationData> districts = syncLRData(LR_DISTRICT_URI_PATH, DISTRICT_TYPE);
        logger.info(districts.size() + " divisions entries synchronized");
        List<LocationData> upazilas = syncLRData(LR_UPAZILA_URI_PATH, UPAZILA_TYPE);
        logger.info(upazilas.size() + " divisions entries synchronized");
        List<LocationData> paurasavas = syncLRData(LR_PAURASAVA_PATH, PAURASAVA_TYPE);
        logger.info(paurasavas.size() + " divisions entries synchronized");
        List<LocationData> unions = syncLRData(LR_UNION_URI_PATH, UNION_TYPE);
        logger.info(unions.size() + " divisions entries synchronized");
        List<LocationData> wards = syncLRData(LR_WARD_URI_PATH, WARD_TYPE);
        logger.info(wards.size() + " divisions entries synchronized");
    }

    public List<LocationData> syncLRData(String uri, String type) {

        List<LocationData> totalRetrieveList = new ArrayList<>();
        List<LocationData> lastRetrieveList = new ArrayList<>();

        if (noOfEntriesSynchronizedSoFar >= MAX_SYNC_DATA_SIZE) {
            return lastRetrieveList;
        }

        int offset = INITIAL_OFFSET;
        String updatedSince = INITIAL_DATETIME;
        LRMarker lrMarker = locationService.getLRMarkerData(type);

        if (lrMarker != null) {
            updatedSince = lrMarker.getLastSync();
            offset = lrMarker.getOffset();
        }

        String url = getCompleteUrl(uri, offset,updatedSince);
        ListenableFuture<LocationData[]> lrResponse;
        try {
            do {
                lastRetrieveList = new ArrayList<>();
                lrResponse = getNextChunkOfDataFromLR(url);
                LocationData[] locationDataArr = lrResponse.get();
                if (locationDataArr != null) {
                    for (LocationData locationData : locationDataArr) {
                        lastRetrieveList.add(locationData);
                    }
                }
                saveOrUpdateLRData(lastRetrieveList);
                totalRetrieveList.addAll(lastRetrieveList);
                offset = offset + DEFAULT_LIMIT;
                this.noOfEntriesSynchronizedSoFar += lastRetrieveList.size();
            } while (lastRetrieveList.size() == DEFAULT_LIMIT && noOfEntriesSynchronizedSoFar < MAX_SYNC_DATA_SIZE);
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException(e);
        }

        if (lastRetrieveList != null) {
            if (lastRetrieveList.size() == DEFAULT_LIMIT) {
                locationService.saveOrUpdateLRMarkerData(type, INITIAL_DATETIME, offset);
            } else {
                locationService.saveOrUpdateLRMarkerData(type, getCurrentDateAndTime(), INITIAL_OFFSET);
            }
        }

        logger.info(totalRetrieveList.size() + " " + type + " entries synchronized");
        logger.info(failedDuringSaveOrUpdateOperation.size() + " entries failed during synchronization");
        logger.info("Synchronization Failed for the following LR");
        logger.info(failedDuringSaveOrUpdateOperation.toString());

        return totalRetrieveList;

    }

    private void saveOrUpdateLRData(List<LocationData> lastRetrieveList) {

        if(lastRetrieveList != null) {

            for (LocationData locationData : lastRetrieveList) {
                try {
                    Location location = locationService.saveOrUpdateLocationData(locationData);

                } catch (Exception e) {
                    logger.info("Failed to sync with Local DB "+ e.getMessage());
                    failedDuringSaveOrUpdateOperation.add(locationData.toString());
                }
            }
        }
    }


    public ListenableFuture<LocationData[]> getNextChunkOfDataFromLR(String url) {

        return new ListenableFutureAdapter<LocationData[], ResponseEntity<LocationData[]>>(mciAsyncRestTemplate.exchange(
                url,
                HttpMethod.GET,
                getHttpEntityWithAuthenticationHeader(),
                LocationData[].class)) {
            @Override
            protected LocationData[] adapt(ResponseEntity<LocationData[]> result) throws ExecutionException {
                if (result.getStatusCode().is2xxSuccessful()) {
                    return result.getBody();
                } else {
                    return null;
                }
            }
        };
    }

    public String getCompleteUrl(String uri, int offset, String updatedSince) {
        return mciProperties.getLocaitonRegistryUrl() + uri + getExtraFilter(offset,updatedSince);
    }

    public String getExtraFilter(int offset, String updatedSince) {
        return String.format(EXTRA_FILTER_PATTERN, offset, DEFAULT_LIMIT, updatedSince);
    }

    public String getCurrentDateAndTime() {
        return new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date());
    }

}
