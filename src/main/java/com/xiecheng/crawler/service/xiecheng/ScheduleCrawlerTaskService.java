package com.xiecheng.crawler.service.xiecheng;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 定时采集任务入口
 * @author nijichang
 * @since 2020-11-12 15:58:52
 */
@Service
@Slf4j
public class ScheduleCrawlerTaskService {

//    @Resource
//    private RunningStrategy runningService;
//
//    @Resource
//    private CrawlerTaskService crawlerTaskService;

//    @Scheduled(fixedDelay = 60 * 1000)
//    public void scheduleRunning(){
//        log.info("定时采集任务开启");
//        //需要判断当前是否有任务在采集
//        Wrapper<CrawlerTaskDO> wrapper = new QueryWrapper<CrawlerTaskDO>().eq("status",1);
//        List<CrawlerTaskDO> taskDoingList = crawlerTaskService.list(wrapper);
//
//        if(!TaskQueue.taskQueue.isEmpty()){
//            runningService.run();
//            //任务状态更新为已完成
//            taskDoingList.forEach(t -> t.setStatus(2));
//            crawlerTaskService.updateBatchById(taskDoingList);
//        }
//    }
}
