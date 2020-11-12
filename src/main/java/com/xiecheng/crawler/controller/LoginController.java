package com.xiecheng.crawler.controller;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiecheng.crawler.constant.MessageConstant;
import com.xiecheng.crawler.entity.ResponseResult;
import com.xiecheng.crawler.entity.po.CustomerDO;
import com.xiecheng.crawler.entity.vo.CustomerVo;
import com.xiecheng.crawler.mapstruct.Mapping;
import com.xiecheng.crawler.service.core.service.impl.CustomerService;
import com.xiecheng.crawler.utils.JwtUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nijichang
 * @since 2020-11-10 10:27:01
 */
@Controller
public class LoginController {

    @Resource
    private CustomerService customerService;

    @RequestMapping("/")
    public String index(){
        return "login";
    }
    /**
     * 退出登录
     * @param session session
     * @return  String
     */
    @RequestMapping("logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }

    @RequestMapping(value = "register",method = RequestMethod.GET)
    public String register(){
        return "register";
    }

    /**
     * 注册
     * @param customerVo vo
     * @return  ResponseResult
     */
    @RequestMapping(value = "register",method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult register(CustomerVo customerVo){
        String name = customerVo.getName();
        Wrapper<CustomerDO> wrapper = new QueryWrapper<CustomerDO>()
                .eq(StringUtils.isNotEmpty(name),"name",name);
        if(ObjectUtils.isNotEmpty(customerService.getOne(wrapper))){
            return ResponseResult.fail(MessageConstant.USER_ALREADY_EXIT);
        }
        CustomerDO customerDO =  Mapping.instance.toCustomerDoO(customerVo);
        customerDO.setPassword(SecureUtil.md5(customerDO.getPassword()));
        if(customerService.save(customerDO)){
            return ResponseResult.success(MessageConstant.REGISTER_SUCESS);
        }
        return ResponseResult.fail(MessageConstant.REGISTER_FAIL);
    }



    @RequestMapping("login")
    @ResponseBody
    public ResponseResult login(String username, String password,
                                String code, HttpSession session){
        String randCode = (String) session.getAttribute("code");
        if(!randCode.equalsIgnoreCase(code)){
            return ResponseResult.fail(MessageConstant.CODE_ERROR);
        }
        CustomerDO customerDO = customerService.getOne(new QueryWrapper<CustomerDO>()
                .eq(StringUtils.isNotEmpty(username),"name",username));
        if(ObjectUtils.isEmpty(customerDO)){
            return ResponseResult.fail(MessageConstant.USER_UNEXIT);
        }
        if(!customerDO.getPassword().equals(SecureUtil.md5(password))){
            return ResponseResult.fail(MessageConstant.WRONG_PASSWORD);
        }
        Map<String,String> token = new HashMap();
        String ret = JwtUtils.geneJsonWebToken(customerDO);
        token.put("token",ret);
        session.setAttribute("token",ret);
        return ResponseResult.success(MessageConstant.LOGIN_SUCESS,token);
    }

}
