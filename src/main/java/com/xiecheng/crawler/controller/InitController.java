package com.xiecheng.crawler.controller;

import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiecheng.crawler.constant.MessageConstant;
import com.xiecheng.crawler.entity.ResponseResult;
import com.xiecheng.crawler.enums.TypeEnum;
import com.xiecheng.crawler.service.core.TaskQueue;
import com.xiecheng.crawler.service.core.service.impl.CookieService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nijichang
 * @since 2020-11-11 18:29:24
 */
@Controller
@RequestMapping("/manage")
public class InitController {

    @Resource
    private CookieService cookieService;

    private static Map<String, Object> DATA_MAP = new HashMap<>();

    @RequestMapping(value = "/init", method = {RequestMethod.GET})
    @ResponseBody
    public ResponseResult init(){
        DATA_MAP.put("city", TaskQueue.citys);
        DATA_MAP.put("brand", TaskQueue.brands);
        DATA_MAP.put("type", TypeEnum.toMap());
        return ResponseResult.success(DATA_MAP);
    }

    @RequestMapping(value = "/cookieCheck", method = {RequestMethod.GET})
    @ResponseBody
    public ResponseResult cookieCheck(){
        String cookie = cookieService.getById(1).getCookie();
        String cookieDate = ReUtil.getGroup0("(?<=Expires=)(\\d*)",cookie);
        if(StringUtils.isNotEmpty(cookieDate)) {
            if ((System.currentTimeMillis() - Long.valueOf(cookieDate)) / (1000 * 3600 * 24) >= 6) {
                return ResponseResult.fail(MessageConstant.COOKIE_EXPIRES);
            }
        }
        return ResponseResult.success();
    }


}
