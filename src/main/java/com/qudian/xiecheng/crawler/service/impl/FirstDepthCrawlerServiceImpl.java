package com.qudian.xiecheng.crawler.service.impl;

import cn.hutool.http.HttpUtil;
import com.qudian.xiecheng.crawler.service.CrawlerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author nijichang
 * @since 2020-10-30 18:37:19
 */
@Service
public class FirstDepthCrawlerServiceImpl implements CrawlerService {

    @Override
    public String crawl(String uri, Map<String,Object> paramsMap){
        return HttpUtil.post(uri,paramsMap);
    }
}
