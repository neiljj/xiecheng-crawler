package com.xiecheng.crawler.service.biz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author nijichang
 * @since 2020-11-07 10:32:19
 */
@Slf4j
public abstract class AbstractCrawlerBiz {

    @Value("${crawler.cookie}")
    protected String cookie;

    @Value("${retry.num}")
    protected Integer retryNum ;

    @Value("${thread.num}")
    protected Integer threadNum;

    protected AtomicInteger taskNum = new AtomicInteger(0);

    public void await(){
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){
            log.error(e.getMessage());
        }
    }

    protected abstract void process();

}
