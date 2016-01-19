package org.sharedhealth.mci.web.service;

import org.apache.commons.collections4.CollectionUtils;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.model.RequesterDetails;
import org.sharedhealth.mci.utils.FileUtil;
import org.sharedhealth.mci.utils.LuhnChecksumGenerator;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.web.infrastructure.security.UserInfo;
import org.sharedhealth.mci.web.model.GeneratedHIDBlock;
import org.sharedhealth.mci.web.model.MciHealthId;
import org.sharedhealth.mci.web.model.OrgHealthId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static org.sharedhealth.mci.domain.util.DateUtil.SIMPLE_DATE_WITH_SECS_FORMAT;
import static org.sharedhealth.mci.domain.util.DateUtil.toDateString;
import static org.sharedhealth.mci.domain.util.JsonMapper.writeValueAsString;

@Component
public class HealthIdService {
    private static Logger logger = LoggerFactory.getLogger(HealthIdService.class);
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
            String possibleHid = String.valueOf(i);
            if (!mciInvalidHidPattern.matcher(possibleHid).find()) {
                numberOfValidHIDs += 1;
                String newHealthId = possibleHid + checksumGenerator.generate(possibleHid.substring(1));
                healthIdRepository.saveMciHealthId(new MciHealthId(newHealthId));
            }
        }
        saveGeneratedBlock(start, end, numberOfValidHIDs, MCI_ORG_CODE, userInfo);
        return numberOfValidHIDs;
    }

    public long generateBlock(long start, long totalHIDs, UserInfo userInfo) {
        return generateBlockForOrg(start, totalHIDs, MCI_ORG_CODE, userInfo);
    }

    public long generateBlockForOrg(long start, long totalHIDs, String orgCode, UserInfo userInfo) {
        long numberOfValidHIDs = 0L;
        long seriesNo = identifySeriesNo(start);
        long startForBlock = identifyStartInSeries(seriesNo);
        Pattern invalidHidPattern = getPattern(orgCode);
        HIDSave hidSave = getHIDSaveMethod(orgCode);
        File hidFile = createFileForOrg(orgCode);
        logger.info(String.format("Saving HIDs to file %s ", hidFile.getAbsolutePath()));
        long i;
        for (i = 0; numberOfValidHIDs < totalHIDs; i++) {
            long possibleHID = startForBlock + i;
            if (!isPartOfSeries(seriesNo, possibleHID)) {
                break;
            }
            String possibleHid = String.valueOf(possibleHID);
            if (!invalidHidPattern.matcher(possibleHid).find()) {
                numberOfValidHIDs += 1;
                String newHealthId = possibleHid + checksumGenerator.generate(possibleHid.substring(1));
                hidSave.saveHID(newHealthId, orgCode);
                FileUtil.addHidToFile(hidFile, newHealthId);
            }
        }
        saveGeneratedBlock(startForBlock, startForBlock + i - 1, numberOfValidHIDs, orgCode, userInfo);
        return numberOfValidHIDs;
    }

    private File createFileForOrg(String orgCode) {
        String hidStorageDirPath = mciProperties.getHidStoragePath();
        File outputDir = new File(hidStorageDirPath);
        outputDir.mkdirs();
        String fileName = String.format("%s-%s", orgCode, toDateString(new Date(), SIMPLE_DATE_WITH_SECS_FORMAT));
        return new File(outputDir, fileName);
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

    private Pattern getPattern(String orgCode) {
        return MCI_ORG_CODE.equals(orgCode) ? mciInvalidHidPattern : orgInvalidHidPattern;
    }

    //this is an interface, implementation will decide which save method to call
    private interface HIDSave {
        void saveHID(String hid, String allocatedFor);
    }

    private HIDSave getHIDSaveMethod(final String orgCode) {
        if (MCI_ORG_CODE.equals(orgCode)) {
            return new HIDSave() {
                @Override
                public void saveHID(String hid, String allocatedFor) {
                    healthIdRepository.saveMciHealthId(new MciHealthId(hid));
                }
            };
        } else {
            return new HIDSave() {
                @Override
                public void saveHID(String hid, String allocatedFor) {
                    healthIdRepository.saveOrgHealthId(new OrgHealthId(hid, allocatedFor, null));
                }
            };
        }
    }
}
