package com.xiecheng.crawler;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.xiecheng.crawler.*"})
@Slf4j
@EnableConfigurationProperties
@MapperScan("com.xiecheng.crawler.mapper")
@EnableAsync
@EnableScheduling
@EnableCaching
public class XiechengCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiechengCrawlerApplication.class, args);
        log.info("携程数据管理后台已启动");
    }

}
