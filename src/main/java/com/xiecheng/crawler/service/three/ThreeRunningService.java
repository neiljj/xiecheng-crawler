package com.xiecheng.crawler.service.three;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.RunningStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 迈点，环球旅讯，酒店高参service
 * @author nijichang
 * @since 2021-06-25 18:22:06
 */
@Service
@Slf4j
public class ThreeRunningService implements RunningStrategy {
    @Autowired
    private List<ThreeCommonCrawlerProcessor> processors;

    @Override
    public void run(Map<CrawlerEnum,List<String>> keywordMap,CountDownLatch countDownLatch){
        ExecutorService service = new ThreadPoolExecutor(3, 3,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(300),
                new ThreadFactoryImpl("threeMainThread_"));
        for (ThreeCommonCrawlerProcessor processor : processors) {
            service.execute(() -> processor.run(keywordMap));
        }
        service.shutdown();
        try {
            while (true) {
                if (service.isTerminated()) {
                    break;
                }
            }
        }finally {
            log.info("三酒店网站采集结束");
            countDownLatch.countDown();
        }
    }
}
