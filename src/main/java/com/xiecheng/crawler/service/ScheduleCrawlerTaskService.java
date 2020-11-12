package com.xiecheng.crawler.service;

import com.xiecheng.crawler.service.core.TaskQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 定时采集任务入口
 * @author nijichang
 * @since 2020-11-12 15:58:52
 */
@Service
@Slf4j
public class ScheduleCrawlerTaskService {

    @Resource
    private RunningService runningService;

    @Scheduled(cron = "${crawler.cron}")
    public void scheduleRunning(){
        log.info("定时采集任务开启");
        if(!TaskQueue.taskQueue.isEmpty()){
            runningService.run();
        }
    }
}
