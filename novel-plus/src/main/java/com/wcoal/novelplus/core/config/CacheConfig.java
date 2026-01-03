package com.wcoal.novelplus.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.wcoal.novelplus.core.common.constant.CacheConsts;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 * 缓存配置类
 * </p>
 *
 * @author wcoal
 * @since 2025-09-30
 */
@Configuration
public class CacheConfig {

    /**
     * caffeine缓存管理器
     * @return caffeine缓存管理器
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<CaffeineCache> caches = new ArrayList<>(CacheConsts.CacheEnum.values().length);

        for (var c : CacheConsts.CacheEnum.values()) {
            if (c.isLocal()) {
                Caffeine<Object, Object> caffeine = Caffeine.newBuilder().recordStats()
                        .maximumSize(c.getMaxSize());
                if (c.getTtl() > 0) {
                    caffeine.expireAfterWrite(Duration.ofSeconds(c.getTtl()));
                }
                caches.add(new CaffeineCache(c.getName(), caffeine.build()));
            }
        }
        cacheManager.setCaches(caches);
        return cacheManager;
    }

    /**
     * redis缓存管理器
     * @return redis缓存管理器
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);

        //作用：设置默认缓存配置
        //disableCachingNullValues：禁用缓存null值
        //prefixCacheNameWith：为缓存名称添加前缀
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues().prefixCacheNameWith(CacheConsts.REDIS_CACHE_PREFIX);

        LinkedHashMap<String, RedisCacheConfiguration> cacheMap = new LinkedHashMap<>(CacheConsts.CacheEnum.values().length);//初始化缓存映射表
        for (var c : CacheConsts.CacheEnum.values()) {
            if(c.isRemote()) {
                if (c.getTtl() > 0) {//如果缓存过期时间大于0，设置缓存过期时间
                    cacheMap.put(c.getName(),
                            RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues()
                                    .prefixCacheNameWith(CacheConsts.REDIS_CACHE_PREFIX)
                                    .entryTtl(Duration.ofSeconds(c.getTtl())));
                } else {//如果缓存过期时间不大于0，使用默认缓存配置
                    cacheMap.put(c.getName(),
                            RedisCacheConfiguration.defaultCacheConfig()
                                    .prefixCacheNameWith(CacheConsts.REDIS_CACHE_PREFIX));
                }
            }
        }
        RedisCacheManager redisCacheManager = new RedisCacheManager(redisCacheWriter, defaultCacheConfig, cacheMap);//初始化redis缓存管理器
        redisCacheManager.setTransactionAware(true);//设置事务感知，开启事务时，会将缓存操作放入事务中
        redisCacheManager.initializeCaches();//初始化缓存
        return redisCacheManager;
    }
}
