package org.sharedhealth.mci.web.controller;

import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.model.GeneratedHidRange;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.sharedhealth.mci.web.service.GeneratedHidRangeService;
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

    private HealthIdService healthIdService;
    private GeneratedHidRangeService generatedHidRangeService;
    private MCIProperties properties;

    @Autowired
    public HealthIdController(HealthIdService healthIdService, GeneratedHidRangeService generatedHidRangeService, MCIProperties properties) {
        this.healthIdService = healthIdService;
        this.generatedHidRangeService = generatedHidRangeService;
        this.properties = properties;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Admin')")
    @RequestMapping(method = POST, value = "/generate")
    public DeferredResult<String> generate(){
        UserInfo userInfo = getUserInfo();
        final DeferredResult<String> deferredResult = new DeferredResult<>();
        Long start = properties.getMciStartHid();
        Long end = properties.getMciEndHid();
        if (hasOverlappingRange(generatedHidRangeService.getPreGeneratedHidRanges(), start, end)) {
            deferredResult.setErrorResult(String.format("Range overlaps with pregenerated healthIds"));
        } else {
            logAccessDetails(userInfo, format("Generating new hids"));
            long numberOfValidHids = healthIdService.generate(start, end);
            if (numberOfValidHids > 0) {
                generatedHidRangeService.saveGeneratedHidRange(new GeneratedHidRange(start, end));
            }
            deferredResult.setResult(String.format("GENERATED %s Ids", numberOfValidHids));
            logger.info(String.format("%s healthIds generated", numberOfValidHids));
        }
        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Admin')")
    @RequestMapping(method = POST, value = "/generateRange")
    public DeferredResult<String> generateRange(@RequestParam(value = "start") long start,
                                           @RequestParam(value = "end") long end){
        UserInfo userInfo = getUserInfo();
        final DeferredResult<String> deferredResult = new DeferredResult<>();
        if (hasOverlappingRange(generatedHidRangeService.getPreGeneratedHidRanges(), start, end)) {
            deferredResult.setErrorResult(String.format("Range overlaps with pregenerated healthIds"));
        } else {
            logAccessDetails(userInfo, format("Generating new hids"));
            long numberOfValidHids = healthIdService.generate(start, end);
            if (numberOfValidHids > 0) {
                generatedHidRangeService.saveGeneratedHidRange(new GeneratedHidRange(start, end));
            }
            deferredResult.setResult(String.format("GENERATED %s Ids", numberOfValidHids));
            logger.info(String.format("%s healthIds generated", numberOfValidHids));
        }
        return deferredResult;
    }

    private boolean hasOverlappingRange(List<GeneratedHidRange> preGeneratedHidRanges, long start, long end) {
        for (GeneratedHidRange preGeneratedHidRange : preGeneratedHidRanges) {
            if (isOverlapping(preGeneratedHidRange, start, end)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverlapping(GeneratedHidRange preGeneratedHidRange, long start, long end) {
        if (preGeneratedHidRange.getBeginsAt() <= start && start <= preGeneratedHidRange.getEndsAt()) {
            return true;
        }
        if (preGeneratedHidRange.getBeginsAt() <= end && end <= preGeneratedHidRange.getEndsAt()) {
            return true;
        }
        if (start <= preGeneratedHidRange.getBeginsAt() && preGeneratedHidRange.getBeginsAt() <= end) {
            return true;
        }
        if (start <= preGeneratedHidRange.getEndsAt() && preGeneratedHidRange.getEndsAt() <= end) {
            return true;
        }
        return false;
    }

    @PreAuthorize("hasAnyRole('ROLE_MCI Admin')")
    @RequestMapping(method = GET, value = "/nextBlock")
    public List<MciHealthId> nextBlock() {
        return healthIdService.getNextBlock();
    }
}
