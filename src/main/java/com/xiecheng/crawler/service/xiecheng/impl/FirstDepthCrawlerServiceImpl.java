package com.xiecheng.crawler.service.xiecheng.impl;

import com.xiecheng.crawler.service.xiecheng.CrawlerService;
import com.xiecheng.crawler.utils.HttpUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author nijichang
 * @since 2020-10-30 18:37:19
 */
@Service
public class FirstDepthCrawlerServiceImpl implements CrawlerService {

    @Override
    public String crawl(String uri, String data, Map<String,String> headers, int timepout){
        return HttpUtils.doPost(uri,data,headers,timepout);
    }
}
