package com.qudian.xiecheng.crawler.service;

import com.qudian.xiecheng.crawler.enums.CityEnum;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author nijichang
 * @since 2020-11-02 11:36:31
 */
@Slf4j
public class TaskQueue {

    public static BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

    /**
     * 第二层采集队列
     */
    public static BlockingQueue<Task> task2Queue = new LinkedBlockingQueue<>();

    private static StringBuffer PARAM_STRING = new StringBuffer().append("StartTime=2020-12-12&DepTime=2020-12-13&cityId=");


    /**
     *初始化对列
     */
    @PostConstruct
    public void initQueue(){
        List<String> codes = CityEnum.toList();
        codes.forEach(t -> {
            Task task = new Task();
            task.setParam(PARAM_STRING.append(t).toString());
            task.setTag(1);
            try {
                taskQueue.put(task);
            }catch (InterruptedException e){
                log.error("添加队列发生错误{}",e.getMessage());
            }
        });
    }

    public static void addQueue(String param){
        Task task = new Task();
        task.setParam(param);
        task.setTag(2);
        try {
            taskQueue.put(task);
        }catch (InterruptedException e){
            log.error("添加队列发生错误{}",e.getMessage());
        }
    }

    public static void addQueue2(String url){
        Task task = new Task();
        task.setParam(url);
        task.setTag(3);
        try {
            task2Queue.put(task);
        }catch (InterruptedException e){
            log.error("添加二层队列发生错误{}",e.getMessage());
        }
    }

}
