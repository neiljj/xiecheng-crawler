package com.qudian.xiecheng.crawler;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@Slf4j
@EnableConfigurationProperties
@MapperScan("com.qudian.xiecheng.crawler.dao")
public class XiechengCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiechengCrawlerApplication.class, args);
        log.info("爬虫程序已启动");
    }

}
