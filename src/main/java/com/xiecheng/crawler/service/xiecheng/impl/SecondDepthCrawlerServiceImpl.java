package com.xiecheng.crawler.service.xiecheng.impl;

import cn.hutool.http.HttpUtil;
import com.xiecheng.crawler.service.xiecheng.CrawlerService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author nijichang
 * @since 2020-10-30 18:38:28
 */
@Service
public class SecondDepthCrawlerServiceImpl implements CrawlerService {

    @Override
    public String crawl(String uri, String data, Map<String,String> headers, int timeout){
        return HttpUtil.get(uri);
    }
}
