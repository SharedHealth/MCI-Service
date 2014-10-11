package org.sharedhealth.mci.web.service;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sharedhealth.mci.web.infrastructure.persistence.SettingRepository;
import org.sharedhealth.mci.web.model.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import static org.sharedhealth.mci.utils.FileUtil.asString;

@Component
public class SettingService {

    private SettingRepository settingRepository;

    @Autowired
    public SettingService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public HashMap<String, String> getSettingAsHashMapByKey(String key) {

        HashMap<String, String> settings = new HashMap<>();

        if(StringUtils.isBlank(key)) return settings;

        try {
            settings  = new ObjectMapper().readValue(getSettingAsStringByKey(key), HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return settings;
    }

    @Cacheable({"setting"})
    public String getSettingAsStringByKey(String key) {
        try {
            Setting setting = findByKey(key).get();
            return setting.getValue();
        } catch (Exception e) {
            return getDefaultSettingByKey(key);
        }
    }

    private String getDefaultSettingByKey(String key) {

        try {
            return asString("settings/" + key + ".json");
        } catch (Exception e) {
            return null;
        }
    }

    public ListenableFuture<Setting> findByKey(String key) {
        return settingRepository.findByKey(key);
    }
}
