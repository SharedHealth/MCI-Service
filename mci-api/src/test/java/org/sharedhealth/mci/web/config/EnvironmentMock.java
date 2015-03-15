package org.sharedhealth.mci.web.config;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockPropertySource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class EnvironmentMock implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentMock.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Map<String, String> env = mockPropertySources(applicationContext);
        createEmbeddedCassandra(env);
    }

    private void createEmbeddedCassandra(Map<String, String> env) {
        try {
            logger.debug("Starting embedded cassandra.");
            EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra-template.yaml", 60000);
            new TestMigrations(env).migrate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error starting embedded server.", e);
        }
    }

    private Map<String, String> mockPropertySources(ConfigurableApplicationContext applicationContext) {
        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        MockPropertySource mockEnvVars = new MockPropertySource();
        Map<String, String> env = new HashMap<>();

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
        return env;
    }
}
