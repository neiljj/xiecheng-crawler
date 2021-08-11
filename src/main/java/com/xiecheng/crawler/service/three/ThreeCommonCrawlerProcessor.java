package com.xiecheng.crawler.service.three;

import com.xiecheng.crawler.enums.CrawlerEnum;

import java.util.List;
import java.util.Map;

/**
 * 三酒店爬虫Processor
 * @author nijichang
 * @since 2021-06-25 18:24:37
 */
public interface ThreeCommonCrawlerProcessor {
    void run(Map<CrawlerEnum,List<String>> keywordMap);
}
