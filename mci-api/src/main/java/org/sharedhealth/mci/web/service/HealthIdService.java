package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.utils.LuhnChecksumGenerator;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.web.model.GeneratedHidRange;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class HealthIdService {
    private final Pattern invalidHidPattern;
    private final MCIProperties mciProperties;
    private HealthIdRepository healthIdRepository;
    private LuhnChecksumGenerator checksumGenerator;
    private GeneratedHidRangeService generatedHidRangeService;

    @Autowired
    public HealthIdService(MCIProperties mciProperties, HealthIdRepository healthIdRepository,
                           LuhnChecksumGenerator checksumGenerator, GeneratedHidRangeService generatedHidRangeService) {
        this.mciProperties = mciProperties;
        this.healthIdRepository = healthIdRepository;
        this.checksumGenerator = checksumGenerator;
        this.generatedHidRangeService = generatedHidRangeService;
        invalidHidPattern = Pattern.compile(mciProperties.getInvalidHidPattern());
    }

    public long generateAll() {
        Long start = mciProperties.getMciStartHid();
        Long end = mciProperties.getMciEndHid();
        long numberOfValidHids = 0L;
        for (long i = start; i <= end; i++) {
            numberOfValidHids = saveIfValidHID(numberOfValidHids, i);
        }
        if (numberOfValidHids > 0) {
            generatedHidRangeService.saveGeneratedHidRange(new GeneratedHidRange(start, end));
        }
        return numberOfValidHids;
    }

    public long generateBlock(long start, long blockSize) {
        long numberOfValidHids = 0L;
        int i;
        for (i = 0; numberOfValidHids < blockSize; i++) {
            numberOfValidHids = saveIfValidHID(numberOfValidHids, start + i);
        }
        if (numberOfValidHids > 0) {
            generatedHidRangeService.saveGeneratedHidRange(new GeneratedHidRange(start, start + i));
        }
        return numberOfValidHids;
    }

    public synchronized List<MciHealthId> getNextBlock() {
        return healthIdRepository.getNextBlock(mciProperties.getHealthIdBlockSize());
    }

    public synchronized List<MciHealthId> getNextBlock(int blockSize) {
        return healthIdRepository.getNextBlock(blockSize);
    }

    public void markUsed(MciHealthId nextMciHealthId) {
        healthIdRepository.removedUsedHid(nextMciHealthId);
    }

    private long saveIfValidHID(long numberOfValidHids, long i) {
        String possibleHid = String.valueOf(i);
        if (!invalidHidPattern.matcher(possibleHid).find()) {
            numberOfValidHids += 1;
            String newHealthId = possibleHid + checksumGenerator.generate(possibleHid.substring(1));
            healthIdRepository.saveHealthId(new MciHealthId(newHealthId));
        }
        return numberOfValidHids;
    }
}
