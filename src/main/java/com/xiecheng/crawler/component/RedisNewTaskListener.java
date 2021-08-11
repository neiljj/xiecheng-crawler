package com.xiecheng.crawler.component;

import com.alibaba.fastjson.JSON;
import com.xiecheng.crawler.config.RedisConfig;
import com.xiecheng.crawler.entity.po.CookieDO;
import com.xiecheng.crawler.enums.CookieTypeEnum;
import com.xiecheng.crawler.service.baidu.biz.ZhidaoCrawler;
import com.xiecheng.crawler.service.xiecheng.core.TaskQueue;
import com.xiecheng.crawler.service.zhihu.biz.ZhihuCrawler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Objects;

/**
 * redis 消息队列
 * @author nijichang
 * @since 2021-04-23 10:55:20
 */
@Component
@Slf4j
public class RedisNewTaskListener implements MessageListener {

    @Resource
    private TaskQueue taskQueue;

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern){
        if (ObjectUtils.isEmpty(message.getBody()) || ObjectUtils.isEmpty(message.getChannel())) {
            return;
        }
        if(RedisConfig.NEW_CRAWLER_TASK.equals(message.getChannel())) {
            log.info("收到新增采集任务，消息【{}】", message);

            final String body = RedisSerializer.string().deserialize(message.getBody());

            if (StringUtils.isNotEmpty(body)) {
                Long taskId = Long.parseLong(body);
                taskQueue.addOneToQueue(taskId);
            }
        }else {
            log.info("收到更新cookie，消息【{}】", message);
            final CookieDO cookieDO = JSON.parseObject(RedisSerializer.string().deserialize(message.getBody()),CookieDO.class);
            if(Objects.nonNull(cookieDO)) {
                if (cookieDO.getType().equals(CookieTypeEnum.ZHIDAO.getCode())) {
                    ZhidaoCrawler.COOKIE = cookieDO.getCookie();
                } else if (cookieDO.getType().equals(CookieTypeEnum.ZHIHU.getCode())) {
                    ZhihuCrawler.COOKIE = cookieDO.getCookie();
                }
            }
        }
    }
}
