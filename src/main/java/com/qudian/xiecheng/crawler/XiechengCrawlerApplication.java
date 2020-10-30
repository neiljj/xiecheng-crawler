package com.qudian.xiecheng.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class XiechengCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiechengCrawlerApplication.class, args);
        log.info("爬虫程序已启动");
    }

}
