package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.utils.LuhnChecksumGenerator;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.infrastructure.persistence.HealthIdRepository;
import org.sharedhealth.mci.web.model.HealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class HealthIdService {
    private final Pattern invalidHidPattern;
    private HealthIdRepository healthIdRepository;
    private LuhnChecksumGenerator checksumGenerator;

    @Autowired
    public HealthIdService(MCIProperties mciProperties, HealthIdRepository healthIdRepository, LuhnChecksumGenerator checksumGenerator) {
        this.healthIdRepository = healthIdRepository;
        this.checksumGenerator = checksumGenerator;
        invalidHidPattern = Pattern.compile(mciProperties.getInvalidHidPattern());
    }

    public long generate(long start, long end) {
        long numberOfValidHids = 0L;
        for (long i = start; i <= end; i++) {
            String possibleHid = String.valueOf(i);
            if (!invalidHidPattern.matcher(possibleHid).find()) {
                numberOfValidHids += 1;
                String newHealthId = possibleHid + checksumGenerator.generate(possibleHid.substring(1));
                healthIdRepository.saveHealthId(new HealthId(newHealthId));
            }
        }
        return numberOfValidHids;
    }

    public List<HealthId> getNextBlock() {
        return healthIdRepository.getNextBlock();
    }
}
