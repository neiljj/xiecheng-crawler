package com.xiecheng.crawler.service.xiecheng.core.service.impl;

import com.xiecheng.crawler.entity.po.CookieDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author nijichang
 * @since 2020-11-14 15:06:47
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "caffeineCacheManager")
public class CacheService {

    @Resource
    private CookieService cookieService;

    @Cacheable(value = "cookie")
    public CookieDO getCookie(){
        log.info("缓存中未查询到cookie数据，需要查询数据库");
        return cookieService.getById(1);
    }
}
