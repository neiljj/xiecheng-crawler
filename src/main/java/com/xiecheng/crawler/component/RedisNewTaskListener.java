package com.xiecheng.crawler.component;

import com.xiecheng.crawler.service.xiecheng.core.TaskQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author nijichang
 * @since 2021-04-23 10:55:20
 */
@Component
@Slf4j
public class RedisNewTaskListener implements MessageListener {

    @Resource
    private TaskQueue taskQueue;

    @Override
    public void onMessage(Message message, byte[] pattern){
        log.info("收到新增采集任务，消息【{}】",message);
        if(ObjectUtils.isEmpty(message.getBody()) || ObjectUtils.isEmpty(message.getChannel())){
            return;
        }
        final String body = RedisSerializer.string().deserialize(message.getBody());

        if(StringUtils.isNotEmpty(body)){
            Long taskId = Long.parseLong(body);
            taskQueue.addOneToQueue(taskId);
        }
    }
}
