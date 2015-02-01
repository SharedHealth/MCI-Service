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
import org.springframework.web.client.AsyncRestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private static final String EXTRA_FILTER_PATTERN = "?limit=%s&updatedSince=%s";
    private List<String> failedDuringSaveOrUpdateOperation;

    private LocationService locationService;

    private String updatedSince;
    private int limit = 0;


    @Autowired
    public LocationDataSync(@Qualifier("MCIRestTemplate") AsyncRestTemplate mciRestTemplate, MCIProperties mciProperties, LocationService locationService) {
        this.mciRestTemplate = mciRestTemplate;
        this.mciProperties = mciProperties;
        this.locationService = locationService;
        this.failedDuringSaveOrUpdateOperation = new ArrayList<>();
    }

    private HttpEntity getHttpEntityWithAuthenticationHeader() {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(AUTH_KEY, mciProperties.getLocaitonRegistryToken());
        return new HttpEntity(header);
    }

    public void sync() throws IOException {
        initParam();
        List<LocationData> divisions = syncLRData(LR_DIVISION_URI_PATH, DIVISION_TYPE);
        logger.info(divisions.size() + " divisions entries synchronized");

        initParam();
        List<LocationData> districts = syncLRData(LR_DISTRICT_URI_PATH, DISTRICT_TYPE);
        logger.info(districts.size() + " districts entries synchronized");

        initParam();
        List<LocationData> upazilas = syncLRData(LR_UPAZILA_URI_PATH, UPAZILA_TYPE);
        logger.info(upazilas.size() + " upazilas entries synchronized");

        initParam();
        List<LocationData> paurasavas = syncLRData(LR_PAURASAVA_PATH, PAURASAVA_TYPE);
        logger.info(paurasavas.size() + " paurasavas entries synchronized");

        initParam();
        List<LocationData> unions = syncLRData(LR_UNION_URI_PATH, UNION_TYPE);
        logger.info(unions.size() + " unions entries synchronized");

        initParam();
        List<LocationData> wards = syncLRData(LR_WARD_URI_PATH, WARD_TYPE);
        logger.info(wards.size() + " wards entries synchronized");
    }

    public List<LocationData> syncLRData(String uri, String type) {

        List<LocationData> totalRetrieveList = new ArrayList<>();
        List<LocationData> lastRetrieveList;

        updatedSince = getUpdatedSince(type);
        limit = getLimit();

        String url = getCompleteUrl(uri, updatedSince, limit);
        try {
            do {
                lastRetrieveList = getNextChunkOfDataFromLR(url);
                if (lastRetrieveList != null && lastRetrieveList.size() > 0) {
                    saveOrUpdateLRData(lastRetrieveList, type);
                    totalRetrieveList.addAll(lastRetrieveList);
                    updatedSince = lastRetrieveList.get(lastRetrieveList.size() - 1).getUpdatedAt();
                }
            } while (lastRetrieveList != null && lastRetrieveList.size() == limit);
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException(e);
        }

        logger.info(totalRetrieveList.size() + " " + type + " entries synchronized");
        logger.info(failedDuringSaveOrUpdateOperation.size() + " entries failed during synchronization");
        logger.info("Synchronization Failed for the following LR");
        logger.info(failedDuringSaveOrUpdateOperation.toString());

        if (totalRetrieveList.size() == 0) {
            locationService.saveOrUpdateLRMarkerData(type, getCurrentDateTime());
        }
        return totalRetrieveList;

    }

    private void saveOrUpdateLRData(List<LocationData> lastRetrieveList, String type) {

        if (lastRetrieveList != null) {

            for (LocationData locationData : lastRetrieveList) {
                try {
                    Location location = locationService.saveOrUpdateLocationData(locationData);

                } catch (Exception e) {
                    logger.info("Failed to sync with Local DB " + e.getMessage());
                    failedDuringSaveOrUpdateOperation.add(locationData.toString());
                    locationService.saveOrUpdateLRMarkerData(type, locationData.getUpdatedAt());
                    throw e;
                }
            }
            updatedSince = lastRetrieveList.get(lastRetrieveList.size() - 1).getUpdatedAt();
            locationService.saveOrUpdateLRMarkerData(type, updatedSince);
        }
    }


    public List<LocationData> getNextChunkOfDataFromLR(String url) throws ExecutionException, InterruptedException {

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

    public String getCompleteUrl(String uri, String updatedSince, int limit) {
        return mciProperties.getLocaitonRegistryUrl() + uri + getExtraFilter(updatedSince, limit);
    }

    public String getExtraFilter(String updatedSince, int limit) {
        return String.format(EXTRA_FILTER_PATTERN, limit, updatedSince);
    }

    public String getUpdatedSince(String type) {

        if (updatedSince != null) {
            return updatedSince;
        }

        LRMarker lrMarker = locationService.getLRMarkerData(type);

        if (lrMarker != null) {
            updatedSince = lrMarker.getLastSync();
            return updatedSince;
        }

        return new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date());
    }

    public void setUpdatedSince(String updatedSince) {
        this.updatedSince = updatedSince;
    }

    public int getLimit() {
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    private void initParam() {
        updatedSince = null;
        limit = 0;
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date());
    }

}
