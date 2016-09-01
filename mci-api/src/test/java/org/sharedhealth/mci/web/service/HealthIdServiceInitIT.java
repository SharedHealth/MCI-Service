package org.sharedhealth.mci.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.model.MciHealthIdStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = {EnvironmentMock.class}, classes = WebMvcConfig.class)
public class HealthIdServiceInitIT {
    @Autowired
    private MciHealthIdStore mciHealthIdStore;
    @Autowired
    private HealthIdService healthIdService;
    @Autowired
    private WebApplicationContext webApplicationContext;

    static {
        setupHidFile();
    }

    private static String hidLocalStoragePath;

    private static void setupHidFile() {
        InputStream inputStream = HealthIdServiceInitIT.class.getResourceAsStream("/test.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            hidLocalStoragePath = (String) properties.get("HID_LOCAL_STORAGE_PATH");
            List<String> healthIdBlock = Lists.newArrayList("healthId1", "healthId2");
            String content = new ObjectMapper().writeValueAsString(healthIdBlock);
            IOUtils.write(content, new FileOutputStream(hidLocalStoragePath), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldLoadHIDsFromFileWhileInitialization() throws Exception {
        assertEquals(2, mciHealthIdStore.noOfHIDsLeft());
        new File(hidLocalStoragePath).delete();
    }

}