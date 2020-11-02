package com.qudian.xiecheng.crawler;

import com.qudian.xiecheng.crawler.service.biz.FirstDepthCrawlerBiz;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class XiechengCrawlerApplicationTests {

    @Resource
    private FirstDepthCrawlerBiz firstDepthCrawlerBiz;

    @Test
    void contextLoads() {
    }

    @Test
    public void test(){
        firstDepthCrawlerBiz.process();
    }
}
