package org.sharedhealth.mci.web.config;

import com.google.common.cache.CacheBuilder;
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
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Import({MCISecurityConfig.class, MCICassandraConfig.class})
@ComponentScan(basePackages = {"org.sharedhealth.mci.web.config",
        "org.sharedhealth.mci.web.controller",
        "org.sharedhealth.mci.web.exception",
        "org.sharedhealth.mci.web.infrastructure",
        "org.sharedhealth.mci.web.mapper",
        "org.sharedhealth.mci.web.model",
        "org.sharedhealth.mci.web.service",
        "org.sharedhealth.mci.utils",
        "org.sharedhealth.mci.web.handler",
        "org.sharedhealth.mci.validation"})
public class MCIConfig {

    public static final int CACHE_TTL_IN_MINUTES = 15;
    public static final int MASTER_DATA_CACHE_TTL_IN_DAYS = 1;

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
                createConcurrentMapCache("mciSettings", CACHE_TTL_IN_MINUTES, TimeUnit.MINUTES, 10),
                createConcurrentMapCache("masterData", MASTER_DATA_CACHE_TTL_IN_DAYS, TimeUnit.DAYS, 500)
        ));

        return cacheManager;
    }

    private ConcurrentMapCache createConcurrentMapCache(String name, int facilityCacheTtlInMinutes, TimeUnit timeUnit, int size) {
        return new ConcurrentMapCache(name,
                CacheBuilder
                        .newBuilder()
                        .expireAfterWrite(facilityCacheTtlInMinutes, timeUnit)
                        .maximumSize(size).build().asMap(),
                true
        );
    }
}
