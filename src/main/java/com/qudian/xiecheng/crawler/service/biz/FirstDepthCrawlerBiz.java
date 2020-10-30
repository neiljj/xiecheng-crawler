package com.qudian.xiecheng.crawler.service.biz;

import com.qudian.xiecheng.crawler.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author nijichang
 * @since 2020-10-30 18:57:43
 */
@Service
public class FirstDepthCrawlerBiz {

    @Autowired
    @Qualifier("FirstDepthCrawlerServiceImpl")
    private CrawlerService crawlerService;

    @Value("${crawler.cookie}")
    private String COOKIE;

    /**
     * 爬虫主要逻辑 todo
     */
    public void process(){

    }
}
