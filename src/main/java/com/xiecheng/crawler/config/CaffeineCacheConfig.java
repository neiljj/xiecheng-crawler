package com.xiecheng.crawler.config;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * @author nijichang
 * @since 2020-09-28 14:46:26
 */
@Configuration
@Slf4j
public class CaffeineCacheConfig {
    /**
     * 配置缓存管理器
     *
     * @return 缓存管理器
     */
    @Bean("caffeineCacheManager")
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
              //  .expireAfterWrite(10, TimeUnit.SECONDS)
                .refreshAfterWrite(1000 * 3600, TimeUnit.SECONDS)
                // 初始的缓存空间大小
                .initialCapacity(100)
                // 缓存的最大条数
                .maximumSize(1000)

        );
        cacheManager.setAllowNullValues(false);
        cacheManager.setCacheLoader(cacheLoader());
        return cacheManager;
    }

    @Bean
    public CacheLoader<Object, Object> cacheLoader() {
        return  new CacheLoader<Object, Object>() {
            //默认的数据加载实现，当调用get取值的时候，如果key没有对应的值，就调用这个方法进行加载
            @Override
            public Object load(Object key) throws Exception {
                return null;
            }
        };
    }

}
