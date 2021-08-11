package com.xiecheng.crawler.service.weibo;

import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.RunningStrategy;
import com.xiecheng.crawler.service.weibo.biz.WeiboCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 微博采集service
 * 微博采用单线程防止账号被封
 * @author nijichang
 * @since 2021-06-23 17:51:18
 */
@Slf4j
@Service
public class WeiboRunningService implements RunningStrategy {

    @Resource
    private WeiboCrawler weiboCrawler;

    @Override
    public void run(Map<CrawlerEnum,List<String>> keywordMap,CountDownLatch countDownLatch){
        try {
            weiboCrawler.run(keywordMap);
        }finally {
            log.info("微博采集结束");
            countDownLatch.countDown();
        }
    }
}
