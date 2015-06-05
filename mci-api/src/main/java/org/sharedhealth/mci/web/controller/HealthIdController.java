package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.service.HealthIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import static java.lang.String.format;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/healthIds")
public class HealthIdController extends MciController {
    private static final Logger logger = LoggerFactory.getLogger(HealthIdController.class);

    private HealthIdService healthIdService;
    private MCIProperties properties;

    @Autowired
    public HealthIdController(HealthIdService healthIdService, MCIProperties properties) {
        this.healthIdService = healthIdService;
        this.properties = properties;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Admin')")
    @RequestMapping(method = POST, value = "/generate")
    public DeferredResult<String> generate(){
        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, format("Generating new hids"));
        final DeferredResult<String> deferredResult = new DeferredResult<>();
        long numberOfValidHids = healthIdService.generate(properties.getMciStartHid(), properties.getMciEndHid());
        deferredResult.setResult(String.format("GENERATED %s Ids", numberOfValidHids));
        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Admin')")
    @RequestMapping(method = POST, value = "/generateRange")
    public DeferredResult<String> generateRange(@RequestParam(value = "start") long start,
                                           @RequestParam(value = "end") long end){
        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, format("Generating new hids"));
        final DeferredResult<String> deferredResult = new DeferredResult<>();
        long numberOfValidHids = healthIdService.generate(start, end);
        deferredResult.setResult(String.format("GENERATED %s Ids", numberOfValidHids));
        return deferredResult;
    }
}
