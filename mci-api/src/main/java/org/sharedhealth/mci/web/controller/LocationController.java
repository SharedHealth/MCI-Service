package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIMultiResponse;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.mapper.LocationCriteria;
import org.sharedhealth.mci.web.mapper.LocationData;
import org.sharedhealth.mci.web.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/locations")
public class LocationController extends MciController {
    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    private LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Approver', 'ROLE_MCI Admin')")
    @RequestMapping(method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<MCIMultiResponse>> findLocationsByParent(
            @Valid LocationCriteria locationCriteria,
            BindingResult bindingResult) {

        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, String.format("Find list of locations by parent, given search criteria : %s", locationCriteria));


        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        logger.debug("Find locations by Parent ");

        final DeferredResult<ResponseEntity<MCIMultiResponse>> deferredResult = new DeferredResult<>();

        List<LocationData> results = locationService.findLocationsByParent(locationCriteria);
        HashMap<String, String> additionalInfo = new HashMap<>();

        MCIMultiResponse mciMultiResponse = new MCIMultiResponse(results, additionalInfo, OK);
        deferredResult.setResult(new ResponseEntity<>(mciMultiResponse, mciMultiResponse.httpStatusObject));

        return deferredResult;
    }


}
