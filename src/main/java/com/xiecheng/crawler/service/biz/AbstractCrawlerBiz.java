package com.xiecheng.crawler.service.biz;

import com.xiecheng.crawler.service.core.CookieService;
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

    @Value("${crawler.cookie}")
    protected String cookie;

    @Value("${thread.num}")
    protected Integer threadNum;

    @Resource
    private CookieService cookieService;

    protected AtomicInteger taskNum = new AtomicInteger(0);

    public void await(){
        try {
            Thread.sleep(3000);
        }catch (InterruptedException e){
            log.error(e.getMessage());
        }
    }

    /**
     * 从数据库中获取cookie
     */
    @Bean
    protected void getCookie(){
    }
    protected abstract void process();

}
