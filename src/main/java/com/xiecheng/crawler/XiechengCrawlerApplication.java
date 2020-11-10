package com.xiecheng.crawler;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.xiecheng.crawler.*"})
@Slf4j
@EnableConfigurationProperties
@MapperScan("com.xiecheng.crawler.mapper")
@EnableAsync
public class XiechengCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiechengCrawlerApplication.class, args);
        log.info("爬虫程序已启动");
    }

}
