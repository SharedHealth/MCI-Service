package org.sharedhealth.mci.domain.service;

import org.sharedhealth.mci.domain.model.MasterData;
import org.sharedhealth.mci.domain.repository.MasterDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MasterDataService {

    private MasterDataRepository allData;

    @Autowired
    public MasterDataService(MasterDataRepository repository) {
        this.allData = repository;
    }

    public MasterData findByKey(String type, String key) {
        return allData.findByKey(type, key);
    }
}
