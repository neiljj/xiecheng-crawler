package com.xiecheng.crawler;

import com.xiecheng.crawler.service.RunningService;
import com.xiecheng.crawler.service.biz.FirstDepthCrawlerBiz;
import com.xiecheng.crawler.service.core.TaskQueue;
import com.xiecheng.crawler.service.core.service.impl.CacheService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = XiechengCrawlerApplication.class)
public class XiechengCrawlerApplicationTests {

    @Resource
    private RunningService runningService;


    @Resource
    private FirstDepthCrawlerBiz firstDepthCrawlerBiz;

    @Resource
    private CacheService cacheService;

    @Resource
    private TaskQueue taskQueue;
    @Test
    public void test(){
        runningService.run();
    }

    @Test
    public void testSaveCity(){
        taskQueue.saveCity();
    }

    @Test
    public void testSaveBrand(){
        taskQueue.saveBrand();
    }

    @Test
    public void test3(){
        while(true){
            cacheService.getCookie();
            try {
                Thread.sleep(2000);
            }catch (InterruptedException e){

            }
        }
    }
}
