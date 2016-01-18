package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.sharedhealth.mci.web.service.HealthIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

import static java.lang.String.format;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/healthIds")
public class HealthIdController extends MciController {
    private static final Logger logger = LoggerFactory.getLogger(HealthIdController.class);
    public static final String GENERATE_ALL_URI = "/generate";
    public static final String GENERATE_RANGE_URI = "/generateRange";
    public static final String NEXT_BLOCK_URI = "/nextBlock";

    private HealthIdService healthIdService;

    @Autowired
    public HealthIdController(HealthIdService healthIdService) {
        this.healthIdService = healthIdService;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Admin')")
    @RequestMapping(method = POST, value = GENERATE_ALL_URI)
    public DeferredResult<String> generate() {
        UserInfo userInfo = getUserInfo();
        final DeferredResult<String> deferredResult = new DeferredResult<>();

        logAccessDetails(userInfo, format("Generating new hids"));
        long numberOfValidHids = healthIdService.generateAll();
        deferredResult.setResult(String.format("GENERATED %s Ids", numberOfValidHids));
        logger.info(String.format("%s healthIds generated", numberOfValidHids));
        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Admin')")
    @RequestMapping(method = POST, value = GENERATE_RANGE_URI)
    public DeferredResult<String> generateRange(@RequestParam(value = "start") long start,
                                                @RequestParam(value = "totalHIDs") long totalHIDs) {
        UserInfo userInfo = getUserInfo();
        final DeferredResult<String> deferredResult = new DeferredResult<>();
        logAccessDetails(userInfo, "Generating new hids");
        long numberOfValidHids = healthIdService.generateBlock(start, totalHIDs);
        deferredResult.setResult(String.format("GENERATED %s Ids", numberOfValidHids));
        logger.info(String.format("%s healthIds generated", numberOfValidHids));
        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Admin')")
    @RequestMapping(method = GET, value = NEXT_BLOCK_URI)
    public List<MciHealthId> nextBlock() {
        return healthIdService.getNextBlock();
    }
}
