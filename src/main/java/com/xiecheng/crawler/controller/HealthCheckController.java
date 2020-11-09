package com.xiecheng.crawler.controller;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author nijichang
 * @since 2020-11-09 10:31:18
 */
@Controller
@Slf4j
@RequestMapping("/")
public class HealthCheckController {

    @RequestMapping("health/check")
    public String healthCheck(){
        return "crawler server is alive,current time:" + DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss");
    }
}
