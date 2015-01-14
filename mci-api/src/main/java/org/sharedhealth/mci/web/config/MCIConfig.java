package org.sharedhealth.mci.web.config;

import org.sharedhealth.mci.web.security.WebSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Arrays;

@Configuration
@EnableCaching
@Import({WebSecurityConfig.class, MCICassandraConfig.class})
@ComponentScan(basePackages = {"org.sharedhealth.mci.web.config",
        "org.sharedhealth.mci.web.controller",
        "org.sharedhealth.mci.web.exception",
        "org.sharedhealth.mci.web.infrastructure",
        "org.sharedhealth.mci.web.mapper",
        "org.sharedhealth.mci.web.model",
        "org.sharedhealth.mci.web.service",
        "org.sharedhealth.mci.utils",
        "org.sharedhealth.mci.validation"})
public class MCIConfig {

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

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("facilities"),
                new ConcurrentMapCache("mciSettingsHash"),
                new ConcurrentMapCache("masterData"),
                new ConcurrentMapCache("mciSettings")
        ));
        return cacheManager;
    }
}
