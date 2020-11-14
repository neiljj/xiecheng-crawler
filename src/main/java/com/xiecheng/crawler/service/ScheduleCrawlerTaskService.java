package com.xiecheng.crawler.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiecheng.crawler.entity.po.CrawlerTaskDO;
import com.xiecheng.crawler.service.core.TaskQueue;
import com.xiecheng.crawler.service.core.service.impl.CrawlerTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

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

    @Resource
    private CrawlerTaskService crawlerTaskService;

    @Scheduled(cron = "${crawler.cron}")
    public void scheduleRunning(){
        log.info("定时采集任务开启");
        //需要判断当前是否有任务在采集
        Wrapper<CrawlerTaskDO> wrapper = new QueryWrapper<CrawlerTaskDO>().eq("status",1);
        List<CrawlerTaskDO> taskDoingList = crawlerTaskService.list(wrapper);

        if(!TaskQueue.taskQueue.isEmpty() && CollectionUtils.isEmpty(taskDoingList)){
            runningService.run();

            //任务状态更新为已完成
        }
    }
}
