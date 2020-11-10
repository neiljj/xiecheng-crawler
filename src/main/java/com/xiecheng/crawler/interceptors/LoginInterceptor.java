package com.xiecheng.crawler.interceptors;

import com.xiecheng.crawler.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 * @author nijichang
 * @since 2020-11-10 11:12:22
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = (String) request.getSession().getAttribute("token");
        if (token != null) {
            Claims claims = JwtUtils.checkJWT(token);
            if (claims != null) {
                return true;
            }
        }
        //用户信息不存在，则拦截，重定向到后台登陆界面
        response.sendRedirect("/crawler");
        return false;
    }
}
