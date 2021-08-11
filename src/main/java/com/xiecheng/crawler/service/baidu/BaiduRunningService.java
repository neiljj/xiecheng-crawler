package com.xiecheng.crawler.service.baidu;

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
 * 百度类的采集service
 * @author nijichang
 * @since 2021-06-22 17:20:36
 */
@Service
@Slf4j
public class BaiduRunningService implements RunningStrategy {

    @Autowired
    private List<BaiduCommonCrawlerProcessor> processors;

    @Override
    public void run(Map<CrawlerEnum,List<String>> keywordMap,CountDownLatch countDownLatch){
        ExecutorService service = new ThreadPoolExecutor(3, 3,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(300),
                new ThreadFactoryImpl("baiduMainThread_"));
        for (BaiduCommonCrawlerProcessor processor : processors) {
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
            log.info("百度采集结束");
            countDownLatch.countDown();
        }
    }
}
