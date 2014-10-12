package org.sharedhealth.mci.web.service;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sharedhealth.mci.web.infrastructure.persistence.SettingRepository;
import org.sharedhealth.mci.web.model.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import static org.sharedhealth.mci.utils.FileUtil.asString;

@Component
public class SettingService {

    private static final Logger logger = LoggerFactory.getLogger(SettingService.class);

    private SettingRepository allSettings;

    @Autowired
    public SettingService(SettingRepository allSettings) {
        this.allSettings = allSettings;
    }

    @Cacheable({"mciSettingsHash"})
    public HashMap<String, String> getSettingAsHashMapByKey(String key) {

        HashMap<String, String> settings = new HashMap<>();

        if (StringUtils.isBlank(key)) return settings;

        try {
            String settingAsString = getSettingAsStringByKey(key);
            settings = new ObjectMapper().readValue(settingAsString, HashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return settings;
    }

    public String getSettingAsStringByKey(String key) {

        Setting setting = getSettingByKey(key);

        return setting.getValue();
    }

    private Setting getSettingByKey(String key) {

        Setting setting = findByKey(key);

        if(null == setting) {
            setting = getDefaultByKey(key);

            if (setting != null) {
                allSettings.save(setting);
            }
        }

        return setting;
    }

    private String getDefaultSettingByKey(String key) {

        try {
            return asString("settings/" + key + ".json");
        } catch (Exception e) {
            return null;
        }
    }

    private Setting getDefaultByKey(String key) {
        String value = getDefaultSettingByKey(key);

        if (value == null) return null;

        Setting setting = new Setting();
        setting.setKey(key);
        setting.setValue(value);

        return setting;
    }

    public Setting findByKey(String key) {
        return allSettings.findByKey(key);
    }

    public ListenableFuture<Setting> findSettingsListenableFutureByKey(String key) {
        return allSettings.findSettingListenableFutureByKey(key);
    }
}
