package com.xiecheng.crawler.config;

import com.xiecheng.crawler.component.RedisNewTaskListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * redis配置
 *
 * @author nijichang
 * @since 2021-04-23 10:50:18
 */
@Configuration
public class RedisConfig {

    public static final String NEW_CRAWLER_TASK = "newTask";

    /**
     * redis消息监听器
     *
     * @author nijichang
     * @since 2021/4/23 4:57 PM
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory, RedisNewTaskListener redisNewTaskListener){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(redisNewTaskListener, ChannelTopic.of(NEW_CRAWLER_TASK));
        return container;
    }
}
