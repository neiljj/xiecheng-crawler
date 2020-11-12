package com.xiecheng.crawler.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.LineCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 生成验证码接口
 * @author nijichang
 * @since 2020-11-10 11:12:22
 */
@Controller
@Slf4j
@RequestMapping("/code")
public class VerifyCodeController {

    @RequestMapping(value = "/getCodeImage",method = RequestMethod.GET)
    public void getCodeImage(HttpServletRequest request, HttpServletResponse response) {
        CircleCaptcha lineCaptcha = CaptchaUtil.createCircleCaptcha(70,38);
        //创建一个session，将生成的随机验证码放到session中
        HttpSession session = request.getSession();
        session.setAttribute("code",lineCaptcha.getCode());

        // 禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", -1);
        response.setContentType("image/jpeg");
        try {
            //将生成的验证图标写入到输出流中，显示验证码
            ImageIO.write(lineCaptcha.getImage(),"jpeg",response.getOutputStream());
        } catch (IOException e) {
            log.info("验证码生成失败，失败信息：{}",e.getMessage());
        }
    }

    public static void main (String[] args){
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200,100);
        lineCaptcha.write("/Users/qudian/Downloads/line.jpeg");
        System.out.println(lineCaptcha.getCode());
    }
}