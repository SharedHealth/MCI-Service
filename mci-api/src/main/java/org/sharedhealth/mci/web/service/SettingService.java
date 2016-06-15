package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.infrastructure.persistence.SettingRepository;
import org.sharedhealth.mci.web.model.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingService {

    private static final Logger logger = LoggerFactory.getLogger(SettingService.class);

    private SettingRepository allSettings;

    @Autowired
    public SettingService(SettingRepository allSettings) {
        this.allSettings = allSettings;
    }

    public String getSettingAsStringByKey(String key) {
        Setting setting = findByKey(key);
        return setting != null ? setting.getValue() : null;
    }

    public Integer getSettingAsIntegerByKey(String key) {
        Setting setting = findByKey(key);
        return setting != null ? Integer.parseInt(setting.getValue()) : null;
    }

    public Setting findByKey(String key) {
        logger.debug(String.format("Find setting for key: %s",key));
        return allSettings.findByKey(key);
    }
}
