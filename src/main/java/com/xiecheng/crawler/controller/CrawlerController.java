package com.xiecheng.crawler.controller;

import com.xiecheng.crawler.entity.ResponseResult;
import com.xiecheng.crawler.service.RunningService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 爬虫启动
 * @author nijichang
 * @since 2020-11-07 14:01:51
 */
@Controller
@RequestMapping("/api")
public class CrawlerController {

    @Resource
    private RunningService runningService ;

    @RequestMapping("/run")
    @ResponseBody
    public ResponseResult crawlerStart(){
        runningService.run();
        return ResponseResult.success();
    }
}
