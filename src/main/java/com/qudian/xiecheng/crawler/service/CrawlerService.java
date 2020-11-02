package com.qudian.xiecheng.crawler.service;


import java.util.Map;

/**
 * @author nijichang
 * @since 2020-10-30 18:10:19
 */
public interface CrawlerService {

    /**
     * 采集逻辑
     * @param uri 链接
     */
    String crawl(String uri, String data, Map<String,String> headers, int timeout);
}
