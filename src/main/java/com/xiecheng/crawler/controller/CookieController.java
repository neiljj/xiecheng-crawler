package com.xiecheng.crawler.controller;

import cn.hutool.json.JSONUtil;
import com.xiecheng.crawler.config.RedisConfig;
import com.xiecheng.crawler.entity.ResponseResult;
import com.xiecheng.crawler.entity.po.CookieDO;
import com.xiecheng.crawler.entity.vo.req.UpdateCookieReq;
import com.xiecheng.crawler.service.xiecheng.core.service.impl.CookieService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Cookie controller
 * @author nijichang
 * @since 2021-06-28 10:33:03
 */
@Controller
@RequestMapping("/manage/cookie")
@Slf4j
public class CookieController {

    @Resource
    private CookieService cookieService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/show_cookie")
    @ResponseBody
    public ResponseResult showCookie(){
        log.info("查询cookie");
        List<CookieDO> cookies = cookieService.list();
        return ResponseResult.success(cookies);
    }

    @RequestMapping("/update_cookie")
    @ResponseBody
    public ResponseResult updateCookie(UpdateCookieReq req){
        if(StringUtils.isEmpty(req.getCookie())){
            return ResponseResult.fail("cookie不能为空");
        }
        log.info("更新cookie入参{}",req);
        CookieDO cookieDO = new CookieDO().setId(req.getId()).setCookie(req.getCookie()).setType(req.getType());
        cookieService.updateById(cookieDO);
        //新增任务发送监听消息
        stringRedisTemplate.convertAndSend(RedisConfig.COOKIE_UPDATE, JSONUtil.toJsonStr(cookieDO));
        return ResponseResult.success();
    }
}
