package org.sharedhealth.mci.web.config;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static net.sf.ehcache.CacheManager.newInstance;

@Configuration
@EnableCaching(proxyTargetClass = true)
public class MCICacheConfiguration implements CachingConfigurer {

    public static final int CACHE_TTL_IN_MINUTES = 15;
    public static final int MASTER_DATA_CACHE_TTL_IN_DAYS = 1;
    private static final int PROVIDER_DATA_CACHE_TTL_IN_DAYS = 15;

    public static final String PROVIDER_CACHE = "PROVIDER_CACHE";
    public static final String SETTINGS_CACHE = "SETTINGS_CACHE";
    public static final String APPROVAL_FIELDS_CACHE = "APPROVAL_FIELDS_CACHE";
    public static final String MASTER_DATA_CACHE = "MASTER_DATA_CACHE";
    public static final String IDENTITY_CACHE = "identityCache";
    public static final String CACHE_EVICTION_POLICY = "LRU";


    @Bean(destroyMethod = "shutdown", name = "ehCacheManager")
    public net.sf.ehcache.CacheManager ehCacheManager() {

        net.sf.ehcache.config.Configuration ehCacheConfig = new net.sf.ehcache.config.Configuration();
        ehCacheConfig.addCache(getMasterDataCacheConfiguration());
        ehCacheConfig.addCache(getProviderCacheConfiguration());
        ehCacheConfig.addCache(getSettingsCacheConfiguration());
        ehCacheConfig.addCache(getApprovedFieldsCacheConfiguration());
        ehCacheConfig.addCache(getIdentityCacheConfiguration());
        return newInstance(ehCacheConfig);
    }

    private CacheConfiguration getMasterDataCacheConfiguration() {
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setName(MASTER_DATA_CACHE);
        cacheConfig.setMemoryStoreEvictionPolicy(CACHE_EVICTION_POLICY);
        cacheConfig.setMaxEntriesLocalHeap(500);
        cacheConfig.setTimeToLiveSeconds(MASTER_DATA_CACHE_TTL_IN_DAYS * 86400);
        cacheConfig.persistence(getPersistenceConfiguration());
        return cacheConfig;
    }

    private CacheConfiguration getProviderCacheConfiguration() {
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setName(PROVIDER_CACHE);
        cacheConfig.setMemoryStoreEvictionPolicy(CACHE_EVICTION_POLICY);
        cacheConfig.setMaxEntriesLocalHeap(500);
        cacheConfig.setTimeToLiveSeconds(PROVIDER_DATA_CACHE_TTL_IN_DAYS * 86400);
        cacheConfig.persistence(getPersistenceConfiguration());
        return cacheConfig;
    }

    private CacheConfiguration getSettingsCacheConfiguration() {
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setName(SETTINGS_CACHE);
        cacheConfig.setMemoryStoreEvictionPolicy(CACHE_EVICTION_POLICY);
        cacheConfig.setMaxEntriesLocalHeap(10);
        cacheConfig.setTimeToLiveSeconds(CACHE_TTL_IN_MINUTES * 60);
        cacheConfig.persistence(getPersistenceConfiguration());
        return cacheConfig;
    }

    private CacheConfiguration getApprovedFieldsCacheConfiguration() {
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setName(APPROVAL_FIELDS_CACHE);
        cacheConfig.setMemoryStoreEvictionPolicy(CACHE_EVICTION_POLICY);
        cacheConfig.setMaxEntriesLocalHeap(50);
        cacheConfig.setTimeToLiveSeconds(CACHE_TTL_IN_MINUTES * 60);
        cacheConfig.persistence(getPersistenceConfiguration());
        return cacheConfig;
    }

    private CacheConfiguration getIdentityCacheConfiguration() {
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setName(IDENTITY_CACHE);
        cacheConfig.setMemoryStoreEvictionPolicy(CACHE_EVICTION_POLICY);
        cacheConfig.setMaxEntriesLocalHeap(500);
        cacheConfig.setTimeToLiveSeconds(2 * 60);
        cacheConfig.persistence(getPersistenceConfiguration());
        return cacheConfig;
    }

    private PersistenceConfiguration getPersistenceConfiguration() {
        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
        persistenceConfiguration.setStrategy("NONE");
        return persistenceConfiguration;
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }

    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(cacheManager());
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }

}
