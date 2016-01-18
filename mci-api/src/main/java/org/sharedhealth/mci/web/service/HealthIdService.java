package org.sharedhealth.mci.web.service;

import org.apache.commons.collections4.CollectionUtils;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.utils.LuhnChecksumGenerator;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.web.model.GeneratedHIDBlock;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class HealthIdService {
    static final String MCI_ORG_CODE = "MCI";
    private static final int DIGITS_FOR_BLOCK_SEPARATION = 2;

    private final Pattern invalidHidPattern;
    private final MCIProperties mciProperties;
    private HealthIdRepository healthIdRepository;
    private LuhnChecksumGenerator checksumGenerator;
    private GeneratedHidBlockService generatedHidBlockService;

    @Autowired
    public HealthIdService(MCIProperties mciProperties, HealthIdRepository healthIdRepository,
                           LuhnChecksumGenerator checksumGenerator, GeneratedHidBlockService generatedHidBlockService) {
        this.mciProperties = mciProperties;
        this.healthIdRepository = healthIdRepository;
        this.checksumGenerator = checksumGenerator;
        this.generatedHidBlockService = generatedHidBlockService;
        invalidHidPattern = Pattern.compile(mciProperties.getInvalidHidPattern());
    }

    public long generateAll() {
        Long start = mciProperties.getMciStartHid();
        Long end = mciProperties.getMciEndHid();
        long numberOfValidHids = 0L;
        for (long i = start; i <= end; i++) {
            numberOfValidHids = saveIfValidHID(numberOfValidHids, i);
        }
        saveGeneratedBlock(start, end, numberOfValidHids);
        return numberOfValidHids;
    }

    public long generateBlock(long start, long blockSize) {
        long numberOfValidHids = 0L;
        long startForBlock = identifyStartForBlock(start);
        int i;
        for (i = 0; numberOfValidHids < blockSize; i++) {
            numberOfValidHids = saveIfValidHID(numberOfValidHids, startForBlock + i);
        }
        saveGeneratedBlock(startForBlock, startForBlock + i - 1, numberOfValidHids);
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

    private long saveIfValidHID(long numberOfValidHids, long currentNumber) {
        String possibleHid = String.valueOf(currentNumber);
        if (!invalidHidPattern.matcher(possibleHid).find()) {
            numberOfValidHids += 1;
            String newHealthId = possibleHid + checksumGenerator.generate(possibleHid.substring(1));
            healthIdRepository.saveHealthId(new MciHealthId(newHealthId));
        }
        return numberOfValidHids;
    }

    private void saveGeneratedBlock(Long start, Long end, long numberOfValidHids) {
        if (numberOfValidHids > 0) {
            long seriesNo = identifySeriesNo(start);
            GeneratedHIDBlock generatedHIDBlock = new GeneratedHIDBlock(seriesNo, MCI_ORG_CODE, start, end, numberOfValidHids, null);
            generatedHidBlockService.saveGeneratedHidBlock(generatedHIDBlock);
        }
    }

    private long identifyStartForBlock(long start) {
        long endsAt = 0L;
        long seriesNo = identifySeriesNo(start);
        List<GeneratedHIDBlock> preGeneratedHIDBlocks = generatedHidBlockService.getPreGeneratedHidBlocks(seriesNo);
        if (CollectionUtils.isEmpty(preGeneratedHIDBlocks)) {
            return start;
        }
        for (GeneratedHIDBlock preGeneratedHIDBlock : preGeneratedHIDBlocks) {
            if (endsAt < preGeneratedHIDBlock.getEndsAt()) {
                endsAt = preGeneratedHIDBlock.getEndsAt();
            }
        }
        return endsAt + 1;
    }

    private long identifySeriesNo(Long start) {
        return Long.parseLong(String.valueOf(start).substring(0, DIGITS_FOR_BLOCK_SEPARATION));
    }
}
