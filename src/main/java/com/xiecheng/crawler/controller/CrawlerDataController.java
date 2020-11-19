package com.xiecheng.crawler.controller;

import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiecheng.crawler.constant.MessageConstant;
import com.xiecheng.crawler.entity.ResponseResult;
import com.xiecheng.crawler.entity.po.*;
import com.xiecheng.crawler.entity.vo.req.AddCrawlerTaskReq;
import com.xiecheng.crawler.entity.vo.req.QryCrawlerTaskReq;
import com.xiecheng.crawler.entity.vo.req.QryDetailInfoReq;
import com.xiecheng.crawler.entity.vo.req.QryHotelInfoReq;
import com.xiecheng.crawler.service.core.service.impl.*;
import com.xiecheng.crawler.utils.JwtUtils;
import com.xiecheng.crawler.utils.mapstruct.Mapping;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
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
    private HotelInfoService hotelnfoService;

    @Resource
    private DetailInfoService detailInfoService;

    @Resource
    private CustomerService customerService;

    @Resource
    private CrawlerTaskService crawlerTaskService;

    @Resource
    private CookieService cookieService;

    @RequestMapping("/show_hotel_info")
    @ResponseBody
    public ResponseResult showHotelInfo(@RequestBody QryHotelInfoReq req){
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
                .eq(StringUtils.isNotEmpty(req.getType()),"type",req.getType())
                .like(StringUtils.isNotEmpty(req.getHotelName()),"hotel_name",req.getHotelName())
                .between(StringUtils.isNotEmpty(req.getPriceBegin()) && StringUtils.isNotEmpty(req.getPriceEnd()),"price",req.getPriceBegin(),req.getPriceEnd())
                .between(start != null && end != null,"create_time",start,end)
                .orderByDesc("create_time")
                ;
        IPage<HotelInfoDO> page = hotelnfoService.page(new Page<>(req.getPage(),req.getLimit()),wrapper);
        log.info("分页查询酒店信息成功{}", JSONUtil.toJsonStr(page));
        return ResponseResult.success(page.getTotal(),page.getRecords());
    }

    @RequestMapping("/show_hotel_detail")
    @ResponseBody
    public ResponseResult showHotelDetail(@RequestBody QryDetailInfoReq req){
        log.info("查询酒店详情入参{}",req);
        Date start = null;
        Date end = null;
        if (StringUtils.isNotEmpty(req.getCreateTimeStart()) && StringUtils.isNotEmpty(req.getCreateTimeEnd())) {
            start = new Date(Long.valueOf(req.getCreateTimeStart()));
            end = new Date(Long.valueOf(req.getCreateTimeEnd()));
        }
        Wrapper<DetailInfoDO> wrapper = new QueryWrapper<DetailInfoDO>()
                .like(StringUtils.isNotEmpty(req.getHotelName()),"name",req.getHotelName())
                .between(start != null && end != null,"create_time",start,end)
                .orderByDesc("create_time")
                ;
        IPage<DetailInfoDO> page = detailInfoService.page(new Page<>(req.getPage(),req.getLimit()),wrapper);
        log.info("分页查询酒店详情成功{}", JSONUtil.toJsonStr(page));
        return ResponseResult.success(page.getTotal(),page.getRecords());
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



    @RequestMapping("/show_crawler_task")
    @ResponseBody
    public ResponseResult showCrawlerTask(QryCrawlerTaskReq req){
        log.info("分页查询采集任务入参{}",req);
        IPage<CrawlerTaskDO> page = crawlerTaskService.page(new Page<>(req.getPage(),req.getLimit()));
        log.info("分页查询采集任务成功{}",JSONUtil.toJsonStr(page));
        return ResponseResult.success(page.getTotal(),page.getRecords());
    }

    @RequestMapping(value = "/add_crawler_task")
    @ResponseBody
    public ResponseResult addCrawlerTask(@RequestBody AddCrawlerTaskReq req){
        log.info("新增采集任务入参{}",req);
        Wrapper<CrawlerTaskDO> wrapper = null;
        //首先需要查询重复任务
        if(StringUtils.isEmpty(req.getBrand()) && StringUtils.isEmpty(req.getType())){
            wrapper = new QueryWrapper<CrawlerTaskDO>()
                    .eq("city",req.getCity())
            ;
        }
        if(!StringUtils.isEmpty(req.getBrand()) && StringUtils.isEmpty(req.getType())){
            wrapper = new QueryWrapper<CrawlerTaskDO>()
                    .eq("city",req.getCity())
                    .eq("brand",req.getBrand())
            ;
        }
        if(StringUtils.isEmpty(req.getBrand()) && !StringUtils.isEmpty(req.getType())){
            wrapper = new QueryWrapper<CrawlerTaskDO>()
                    .eq("city",req.getCity())
                    .eq("type",req.getType())
            ;
        }
        if(!StringUtils.isEmpty(req.getBrand()) && !StringUtils.isEmpty(req.getType())){
            wrapper = new QueryWrapper<CrawlerTaskDO>()
                    .eq("city",req.getCity())
                    .eq("type",req.getType())
                    .eq("brand",req.getBrand())
            ;
        }
        if(!CollectionUtils.isEmpty(crawlerTaskService.list(wrapper))){
            return ResponseResult.fail(MessageConstant.TASK_EXIT);
        }
        req.setStatus(0);
        CrawlerTaskDO crawlerTaskDO = Mapping.instance.toCrawlerTaskDO(req);
        crawlerTaskDO.setCreateTime(new Date());
        crawlerTaskService.save(crawlerTaskDO);
        log.info("采集任务{}新建成功",req);
        return ResponseResult.success();
    }

    @RequestMapping(value = "/cookieUpdate")
    @ResponseBody
    public ResponseResult updateCookie(@RequestBody String cookie){
        //cookie格式校验 2020-11-14 发现携程对cookie进行了改造。。。。
        if(!cookie.contains("Expires")){
            return ResponseResult.fail();
        }
        String getCookie = ReUtil.getGroup0("(?<=:\")(.*)(?=\\\"})",cookie);
        CookieDO cookieDO = new CookieDO();
        cookieDO.setId(1);
        cookieDO.setCookie(getCookie);
        cookieService.updateById(cookieDO);
        log.info("cookie更新成功");
        return ResponseResult.success();
    }

    @RequestMapping("/crawler_task")
    public String crawlerTask(){
        return "crawler_task";
    }

    @RequestMapping("/hotel_info")
    public String hotelInfo(){
        return "hotel_info";
    }

    @RequestMapping("/detail_info")
    public String detailInfo(){
        return "detail_info";
    }
}
