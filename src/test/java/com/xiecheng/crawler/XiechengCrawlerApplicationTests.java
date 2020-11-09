package com.xiecheng.crawler;

import com.xiecheng.crawler.service.RunningService;
import com.xiecheng.crawler.service.biz.SaveDetailBiz;
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
    private SaveDetailBiz saveDetailBiz;
    @Test
    public void test(){
        runningService.run();
    }

    @Test
    public void test2(){
        saveDetailBiz.process();
    }
}
