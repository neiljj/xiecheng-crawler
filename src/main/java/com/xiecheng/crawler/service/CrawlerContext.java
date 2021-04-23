package com.xiecheng.crawler.service;

import org.springframework.stereotype.Service;

/**
 * 采集策略上下文，通过策略模式兼容其他爬虫，采集逻辑写在service-[爬虫名]包下
 * @author nijichang
 * @since 2020-11-23 10:02:20
 */
@Service
public class CrawlerContext {
//
//    private RunningStrategyService runningService;
//
//    public CrawlerContext(RunningStrategyService runningService){this.runningService = runningService;}
//
//    /**
//     * 启动采集策略
//     */
//    public void doCrawl(){
//        runningService.run();
//    }
}
