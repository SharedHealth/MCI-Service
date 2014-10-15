package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.infrastructure.persistence.MasterDataRepository;
import org.sharedhealth.mci.web.model.MasterData;
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
