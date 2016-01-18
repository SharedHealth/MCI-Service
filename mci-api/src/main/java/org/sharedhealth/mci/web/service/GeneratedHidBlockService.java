package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.infrastructure.persistence.GeneratedHidBlockRepository;
import org.sharedhealth.mci.web.model.GeneratedHIDBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeneratedHidBlockService {

    private GeneratedHidBlockRepository generatedHidBlockRepository;

    @Autowired
    public GeneratedHidBlockService(GeneratedHidBlockRepository generatedHidBlockRepository) {
        this.generatedHidBlockRepository = generatedHidBlockRepository;
    }

    public List<GeneratedHIDBlock> getPreGeneratedHidBlocks(long seriesNo) {
        return generatedHidBlockRepository.getPreGeneratedHidBlocks(seriesNo);
    }

    public GeneratedHIDBlock saveGeneratedHidBlock(GeneratedHIDBlock generatedHIDBlock) {
        return generatedHidBlockRepository.saveGeneratedHidBlock(generatedHIDBlock);
    }
}
