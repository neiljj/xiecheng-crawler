package com.xiecheng.crawler.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiecheng.crawler.entity.ResponseResult;
import com.xiecheng.crawler.entity.po.CustomerDO;
import com.xiecheng.crawler.entity.po.DetailInfoDO;
import com.xiecheng.crawler.entity.po.HotelInfoDO;
import com.xiecheng.crawler.entity.vo.req.QryDetailInfoReq;
import com.xiecheng.crawler.entity.vo.req.QryHotelInfoReq;
import com.xiecheng.crawler.service.core.CustomerService;
import com.xiecheng.crawler.service.core.DetailInfoService;
import com.xiecheng.crawler.service.core.HotelnfoService;
import com.xiecheng.crawler.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author nijichang
 * @since 2020-11-07 14:34:43
 */
@Controller
@RequestMapping("/manage")
@Slf4j
public class CrawlerDataController {
    @Resource
    private HotelnfoService hotelnfoService;
    @Resource
    private DetailInfoService detailInfoService;
    @Resource
    private CustomerService customerService;
    @PostMapping("/show_hotel_info")
    @ResponseBody
    public ResponseResult showHotelInfo(QryHotelInfoReq req){
        log.info("查询酒店信息入参{}",req);
        Date start = null;
        Date end = null;
        if (StringUtils.isNotEmpty(req.getCreateTimeStart()) && StringUtils.isNotEmpty(req.getCreateTimeEnd())) {
            start = new Date(Long.valueOf(req.getCreateTimeStart()));
            end = new Date(Long.valueOf(req.getCreateTimeEnd()));
        }
        Wrapper<HotelInfoDO> wrapper = new QueryWrapper<HotelInfoDO>()
                .eq(StringUtils.isNotEmpty(req.getBrand()),"brand",req.getBrand())
                .eq(StringUtils.isNotEmpty(req.getCity()),"city",req.getCity())
                .eq(StringUtils.isNotEmpty(req.getHotelName()),"hotel_name",req.getHotelName())
                .between(StringUtils.isNotEmpty(req.getPriceBegin()) && StringUtils.isNotEmpty(req.getPriceEnd()),"price",req.getPriceBegin(),req.getPriceEnd())
                .between(start != null && end != null,"create_time",start,end)
                ;
        IPage<HotelInfoDO> page = hotelnfoService.page(new Page<>(req.getPage(),req.getPer()),wrapper);
        log.info("分页查询酒店信息成功{}", JSONUtil.toJsonStr(page));
        return ResponseResult.success(page);
    }

    @PostMapping("/show_hotel_detail")
    @ResponseBody
    public ResponseResult showHotelDetail(QryDetailInfoReq req){
        log.info("查询酒店详情入参{}",req);
        Date start = null;
        Date end = null;
        if (StringUtils.isNotEmpty(req.getCreateTimeStart()) && StringUtils.isNotEmpty(req.getCreateTimeEnd())) {
            start = new Date(Long.valueOf(req.getCreateTimeStart()));
            end = new Date(Long.valueOf(req.getCreateTimeEnd()));
        }
        Wrapper<DetailInfoDO> wrapper = new QueryWrapper<DetailInfoDO>()
                .eq(StringUtils.isNotEmpty(req.getCity()),"city",req.getCity())
                .eq(StringUtils.isNotEmpty(req.getHotelName()),"name",req.getHotelName())
                .between(start != null && end != null,"create_time",start,end)
                ;
        IPage<DetailInfoDO> page = detailInfoService.page(new Page<>(req.getPage(),req.getPer()),wrapper);
        log.info("分页查询酒店详情成功{}", JSONUtil.toJsonStr(page));
        return ResponseResult.success(page);
    }

    @RequestMapping("/center")
    public String index(String token, Model model){
        Claims claims = JwtUtils.checkJWT(token);
        Integer customerId = (Integer) claims.get("id");
        CustomerDO customerDO = customerService.getById(customerId);
        customerDO.setPassword(null);
        model.addAttribute("customer",customerDO);
        return "manage_center";
    }
}
