package org.sharedhealth.mci.web.service;

import org.apache.commons.collections4.CollectionUtils;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.model.RequesterDetails;
import org.sharedhealth.mci.utils.LuhnChecksumGenerator;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.model.GeneratedHIDBlock;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.sharedhealth.mci.web.model.OrgHealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

import static org.sharedhealth.mci.domain.util.JsonMapper.writeValueAsString;

@Component
public class HealthIdService {
    static final String MCI_ORG_CODE = "MCI";
    private static final int DIGITS_FOR_BLOCK_SEPARATION = 2;

    private final Pattern mciInvalidHidPattern;
    private final Pattern orgInvalidHidPattern;
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
        this.mciInvalidHidPattern = Pattern.compile(mciProperties.getMciInvalidHidPattern());
        this.orgInvalidHidPattern = Pattern.compile(mciProperties.getOrgInvalidHidPattern());
    }

    public long generateAll(UserInfo userInfo) {
        Long start = mciProperties.getMciStartHid();
        Long end = mciProperties.getMciEndHid();
        long numberOfValidHIDs = 0L;
        for (long i = start; i <= end; i++) {
            numberOfValidHIDs = saveIfValidMciHID(numberOfValidHIDs, i);
        }
        saveGeneratedBlock(start, end, numberOfValidHIDs, MCI_ORG_CODE, userInfo);
        return numberOfValidHIDs;
    }

    public long generateBlock(long start, long totalHIDs, UserInfo userInfo) {
        long numberOfValidHIDs = 0L;
        long seriesNo = identifySeriesNo(start);
        long startForBlock = identifyStartInSeries(seriesNo);
        int i;
        for (i = 0; numberOfValidHIDs < totalHIDs; i++) {
            long possibleHID = startForBlock + i;
            if (!isPartOfSeries(seriesNo, possibleHID)) {
                break;
            }
            numberOfValidHIDs = saveIfValidMciHID(numberOfValidHIDs, possibleHID);
        }
        saveGeneratedBlock(startForBlock, startForBlock + i - 1, numberOfValidHIDs, MCI_ORG_CODE, userInfo);
        return numberOfValidHIDs;
    }

    public long generateBlockForOrg(long start, long totalHIDs, String orgCode, UserInfo userInfo) {
        long numberOfValidHIDs = 0L;
        long seriesNo = identifySeriesNo(start);
        long startForBlock = identifyStartInSeries(seriesNo);
        int i;
        for (i = 0; numberOfValidHIDs < totalHIDs; i++) {
            long possibleHID = startForBlock + i;
            if (!isPartOfSeries(seriesNo, possibleHID)) {
                break;
            }
            numberOfValidHIDs = saveIfValidOrgHID(numberOfValidHIDs, possibleHID, orgCode);
        }
        saveGeneratedBlock(startForBlock, startForBlock + i - 1, numberOfValidHIDs, orgCode, userInfo);
        return numberOfValidHIDs;
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

    private long saveIfValidMciHID(long numberOfValidHids, long currentNumber) {
        String possibleHid = String.valueOf(currentNumber);
        if (!mciInvalidHidPattern.matcher(possibleHid).find()) {
            numberOfValidHids += 1;
            String newHealthId = possibleHid + checksumGenerator.generate(possibleHid.substring(1));
            healthIdRepository.saveMciHealthId(new MciHealthId(newHealthId));
        }
        return numberOfValidHids;
    }

    private long saveIfValidOrgHID(long numberOfValidHIDs, long currentNumber, String orgCode) {
        String possibleHid = String.valueOf(currentNumber);
        if (!orgInvalidHidPattern.matcher(possibleHid).find()) {
            numberOfValidHIDs += 1;
            String newHealthId = possibleHid + checksumGenerator.generate(possibleHid.substring(1));
            healthIdRepository.saveOrgHealthId(new OrgHealthId(newHealthId, orgCode, null));
        }
        return numberOfValidHIDs;
    }

    private void saveGeneratedBlock(Long start, Long end, Long numberOfValidHids, String orgCode, UserInfo userInfo) {
        if (numberOfValidHids > 0) {
            long seriesNo = identifySeriesNo(start);
            RequesterDetails requesterDetails = getRequesterDetails(userInfo);
            GeneratedHIDBlock generatedHIDBlock = new GeneratedHIDBlock(seriesNo, orgCode, start, end, numberOfValidHids, writeValueAsString(requesterDetails));
            generatedHidBlockService.saveGeneratedHidBlock(generatedHIDBlock);
        }
    }

    private RequesterDetails getRequesterDetails(UserInfo userInfo) {
        UserInfo.UserInfoProperties properties = userInfo.getProperties();
        if (properties.getAdminId() != null) {
            return new RequesterDetails(properties.getAdminId());
        }
        return null;
    }

    private boolean isPartOfSeries(long seriesNo, long possibleHID) {
        return identifySeriesNo(possibleHID) == seriesNo;
    }

    private long identifyStartInSeries(long seriesNo) {
        long endsAt = 0L;
        List<GeneratedHIDBlock> preGeneratedHIDBlocks = generatedHidBlockService.getPreGeneratedHidBlocks(seriesNo);
        if (CollectionUtils.isEmpty(preGeneratedHIDBlocks)) {
            return seriesNo;
        }
        for (GeneratedHIDBlock preGeneratedHIDBlock : preGeneratedHIDBlocks) {
            if (endsAt < preGeneratedHIDBlock.getEndsAt()) {
                endsAt = preGeneratedHIDBlock.getEndsAt();
            }
        }
        return endsAt + 1;
    }

    private long identifySeriesNo(Long start) {
        String startAsText = String.valueOf(start);
        String startPrefix = startAsText.substring(0, DIGITS_FOR_BLOCK_SEPARATION);
        String startSuffix = startAsText.substring(DIGITS_FOR_BLOCK_SEPARATION, startAsText.length());
        return Long.parseLong(String.valueOf(startPrefix + startSuffix.replaceAll(".", "0")));
    }
}
