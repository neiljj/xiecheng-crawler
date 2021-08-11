package com.xiecheng.crawler.service;

import com.xiecheng.crawler.enums.CrawlerEnum;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 爬虫采集策略逻辑接口
 * @author nijichang
 * @since 2020-11-06 15:10:53
 */
public interface RunningStrategy {
    /**
     * 爬虫启动
     */
    void run(Map<CrawlerEnum, List<String>> keywordMap,CountDownLatch countDownLatch);
}
