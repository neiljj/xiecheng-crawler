package com.xiecheng.crawler;

import com.xiecheng.crawler.entity.Task;
import com.xiecheng.crawler.service.RunningStrategyService;
import com.xiecheng.crawler.service.xiecheng.biz.FirstDepthCrawlerBiz;
import com.xiecheng.crawler.service.xiecheng.core.TaskQueue;
import com.xiecheng.crawler.service.xiecheng.core.service.impl.CacheService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = XiechengCrawlerApplication.class)
public class XiechengCrawlerApplicationTests {

    @Resource
    private RunningStrategyService runningService;


    @Resource
    private FirstDepthCrawlerBiz firstDepthCrawlerBiz;

    @Resource
    private CacheService cacheService;

    @Resource
    private TaskQueue taskQueue;
    @Test
    public void test(){
        String param = "cityId=58";
        Task task = new Task();
        task.setParamTag(0);
        task.setParam(param);
        task.setDepthTag(0);
        try {
            TaskQueue.taskQueue.put(task);
        }catch (InterruptedException e){

        }
        firstDepthCrawlerBiz.process();
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
