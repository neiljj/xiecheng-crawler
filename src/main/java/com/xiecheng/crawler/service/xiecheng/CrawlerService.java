package com.xiecheng.crawler.service.xiecheng;


import java.util.Map;

/**
 * @author nijichang
 * @since 2020-10-30 18:10:19
 */
public interface CrawlerService {

    /**
     * @param uri 链接
     */
    String crawl(String uri, String data, Map<String,String> headers, int timeout);
}
