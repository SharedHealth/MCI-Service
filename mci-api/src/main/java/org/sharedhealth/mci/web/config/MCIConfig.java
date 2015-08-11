package org.sharedhealth.mci.web.config;

import org.sharedhealth.mci.domain.config.MCICacheConfiguration;
import org.sharedhealth.mci.domain.config.MCICassandraConfig;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

@Configuration
@EnableCaching
@Import({MCICassandraConfig.class,
        MCICacheConfiguration.class,
})
@ComponentScan(basePackages = "org.sharedhealth.mci")
public class MCIConfig {
    public static final String PROFILE_DEFAULT = "default";

    @Autowired
    private MCIProperties mciProperties;

    @Bean(name = "MCIRestTemplate")
    public AsyncRestTemplate mciRestTemplate() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        executor.setCorePoolSize(mciProperties.getRestPoolSize());
        return new AsyncRestTemplate(executor);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    public static List<String> getSupportedRequestUris(String apiVersion, boolean isLatestApiVersion) {
        List<String> mappings = getSupportedServletMappings(apiVersion, isLatestApiVersion);
        List<String> uris = new ArrayList<>();

        for (String mapping : mappings) {
            uris.add(substringBeforeLast(mapping, "/*"));
        }
        return uris;
    }

    public static List<String> getSupportedServletMappings(String apiVersion, boolean isLatestApiVersion) {
        List<String> mappings = new ArrayList<>();

        mappings.add(format("/api/%s/%s/*", apiVersion, PROFILE_DEFAULT));
        mappings.add(format("/api/%s/*", apiVersion));

        if (isLatestApiVersion) {
            mappings.add(format("/api/%s/*", PROFILE_DEFAULT));
            mappings.add("/api/*");
        }

        return mappings;
    }
}
