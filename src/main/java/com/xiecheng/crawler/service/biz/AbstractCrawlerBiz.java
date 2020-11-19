package com.xiecheng.crawler.service.biz;

import com.xiecheng.crawler.service.core.service.impl.CacheService;
import com.xiecheng.crawler.service.core.service.impl.CookieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author nijichang
 * @since 2020-11-07 10:32:19
 */
@Slf4j
public abstract class AbstractCrawlerBiz {
    @Value("${thread.num}")
    protected Integer threadNum;

    @Resource
    private CacheService cacheService;

    protected AtomicInteger taskNum = new AtomicInteger(0);

    public void await(){
        try {
            Thread.sleep(5000);
        }catch (InterruptedException e){
            log.error(e.getMessage());
        }
    }
    /**
     * cookie由本地缓存中读取，缓存更新时间为1小时
     */
    protected String getCookie(){
        return cacheService.getCookie().getCookie();
    }
    protected abstract void process();

}
