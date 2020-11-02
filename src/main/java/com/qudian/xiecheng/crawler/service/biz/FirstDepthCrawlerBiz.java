package com.qudian.xiecheng.crawler.service.biz;

import com.qudian.xiecheng.crawler.service.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nijichang
 * @since 2020-10-30 18:57:43
 */
@Service
@Slf4j
public class FirstDepthCrawlerBiz {

    @Resource
    private CrawlerService firstDepthCrawlerServiceImpl;

    @Value("${crawler.cookie}")
    private String COOKIE;

    @Value("${crawler.uri}")
    private String URI;
    /**
     * 爬虫主要逻辑 todo
     */
    public void process(){
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        paramMap.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
        paramMap.put("Accept","*/*");
        paramMap.put("Accept-Encoding","gzip, deflate, br");
        paramMap.put("Cookie",COOKIE);
        String jsonResult = firstDepthCrawlerServiceImpl.crawl(URI,paramMap);
    }


}
