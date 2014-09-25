package org.sharedhealth.mci.web.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sharedhealth.mci.web.launch.migration.Migrations;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;


@Configuration
public class EnvironmentMock implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        MockPropertySource mockEnvVars = new MockPropertySource();
        Map<String, String> env = new HashMap<String, String>();

        try {
            InputStream inputStream = this.getClass().getResourceAsStream("/test.properties");
            Properties properties = new Properties();
            properties.load(inputStream);

            for (Object property : properties.keySet()) {
                mockEnvVars.setProperty(property.toString(), properties.getProperty(property.toString()));
                env.put(property.toString(), properties.getProperty(property.toString()));
            }
            propertySources.replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, mockEnvVars);
        } catch (Exception ignored) {
            System.out.print("Error ignored!");
        }

        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
            new Migrations(env).migrate();
        } catch (Exception e) {
            throw new RuntimeException("Error starting embedded server..", e);
        }

    }
}
