package com.xiecheng.crawler.service.zhihu;

import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.RunningStrategy;
import com.xiecheng.crawler.service.zhihu.biz.ZhihuCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 知乎采集service
 * @author nijichang
 * @since 2021-06-24 15:22:04
 */
@Slf4j
@Service
public class ZhihuRunningService implements RunningStrategy {

    @Resource
    private ZhihuCrawler zhihuCrawler;
    @Override
    public void run(Map<CrawlerEnum,List<String>> keywordMap,CountDownLatch countDownLatch) {
        try {
            zhihuCrawler.run(keywordMap);
        } finally {
            log.info("知乎采集结束");
            countDownLatch.countDown();
        }
    }
}
