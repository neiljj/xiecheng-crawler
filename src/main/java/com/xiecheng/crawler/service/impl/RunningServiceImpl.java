package com.xiecheng.crawler.service.impl;

import com.xiecheng.crawler.service.RunningService;
import com.xiecheng.crawler.service.biz.FirstDepthCrawlerBiz;
import com.xiecheng.crawler.service.biz.SecondDepthCrawlerBiz;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author nijichang
 * @since 2020-11-06 15:11:42
 */
@Service
@Slf4j
public class RunningServiceImpl implements RunningService {

    @Resource
    private FirstDepthCrawlerBiz firstDepthCrawlerBiz;
    @Resource
    private SecondDepthCrawlerBiz secondDepthCrawlerBiz;

    @Override
    public void run(){
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.execute(() -> firstDepthCrawlerBiz.process());
        try {
            Thread.sleep(60 * 1000);
        }catch (InterruptedException e){
        }
        //一分钟后再启动第二次采集任务
        service.execute(() -> secondDepthCrawlerBiz.process());
        service.shutdown();
        while(true){
            if(service.isTerminated()){
                log.info("所有采集任务完毕");
                break;
            }
        }

    }
}
