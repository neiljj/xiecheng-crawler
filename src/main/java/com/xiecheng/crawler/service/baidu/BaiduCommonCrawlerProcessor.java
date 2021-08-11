package com.xiecheng.crawler.service.baidu;


import com.xiecheng.crawler.enums.CrawlerEnum;

import java.util.List;
import java.util.Map;

/**
 * 百度类爬虫Processor
 * @author nijichang
 * @since 2021-06-22 17:22:31
 */
public interface BaiduCommonCrawlerProcessor {
    void run(Map<CrawlerEnum,List<String>> keywordMap);
}
