package com.xiecheng.crawler.service;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiecheng.crawler.entity.NewsInfoDO;
import com.xiecheng.crawler.entity.es.po.NewsInfoEsDO;
import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.common.impl.CommonMethod;
import com.xiecheng.crawler.service.common.impl.NewsInfoEsService;
import com.xiecheng.crawler.service.common.impl.NewsInfoService;
import com.xiecheng.crawler.utils.mapstruct.DataMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 * 爬虫工厂
 * @author nijichang
 * @since 2020-11-23 10:02:20
 */
@Service
@Slf4j
public class CrawlerFactory{
    @Resource
    private List<RunningStrategy> runningStrategys;

    @Resource
    private CommonMethod commonMethod;

    private static Map<CrawlerEnum,List<String>> keywordMap = new HashMap<>();

    @Resource
    private NewsInfoEsService newsInfoEsService;

    @Resource
    private NewsInfoService newsInfoService;

    @Resource
    private DataMapping dataMapping;

    private ExecutorService service = new ThreadPoolExecutor(5, 5,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(300),
            new ThreadFactoryImpl("factoryThread_"));
    /**
     * 爬虫工厂开始运作
     * @author nijichang
     * @since 2021/6/22 4:11 PM
     */
    @Scheduled(cron = "0 0 9,14,18 * * ?")
    public void factoryBeginWork(){
        // 初始化关键词map
        commonMethod.initKeyword(keywordMap);
        CountDownLatch countDownLatch = new CountDownLatch(runningStrategys.size());
        long start = System.currentTimeMillis();
        LocalDateTime createTimeStart = LocalDateTime.now();
        log.info("爬虫工厂开始运作,开始时间:{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        try {
            for (RunningStrategy runningStrategy : runningStrategys) {
                service.execute(() -> runningStrategy.run(keywordMap,countDownLatch));
            }
        }catch (Exception e){
            log.error("爬虫工厂出现问题,{}",e.getMessage());
        }
        try {
            countDownLatch.await();
        }catch (InterruptedException e){
            log.error("InterruptedException" + e);
        }
        // 该批数据入es
        LambdaQueryWrapper<NewsInfoDO> wrapper  = new LambdaQueryWrapper<NewsInfoDO>()
                .between(NewsInfoDO::getCreateTime,createTimeStart,LocalDateTime.now());
        List<NewsInfoDO> newsInfoDos = newsInfoService.list(wrapper);
        List<NewsInfoEsDO> esDos = newsInfoDos.stream().map(t -> dataMapping.toEsDo(t)).collect(Collectors.toList());
        try{
            newsInfoEsService.saveBatch(esDos);
        }catch (Exception e){
            log.info("插入es数据出现错误，{}",e);
        }
        log.info("爬虫工厂一轮采集任务结束，结束时间：{},耗时：{}ms",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),System.currentTimeMillis() - start);
    }
}
