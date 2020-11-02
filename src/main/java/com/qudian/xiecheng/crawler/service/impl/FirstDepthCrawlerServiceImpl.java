package com.qudian.xiecheng.crawler.service.impl;

import com.qudian.xiecheng.crawler.service.CrawlerService;
import com.qudian.xiecheng.crawler.utils.HttpUtils;
import org.springframework.stereotype.Service;

import java.util.List;
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
