package com.xiecheng.crawler.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiecheng.crawler.entity.NewsInfoDO;
import com.xiecheng.crawler.entity.ResponseResult;
import com.xiecheng.crawler.entity.es.po.NewsInfoEsDO;
import com.xiecheng.crawler.entity.vo.req.QryNewsInfoReq;
import com.xiecheng.crawler.service.common.impl.NewsInfoEsService;
import com.xiecheng.crawler.service.common.impl.NewsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zxp.esclientrhl.repository.Attach;
import org.zxp.esclientrhl.repository.PageList;
import org.zxp.esclientrhl.repository.PageSortHighLight;
import org.zxp.esclientrhl.repository.Sort;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 新闻 controller
 * @author nijichang
 * @since 2021-06-28 10:20:06
 */
@Controller
@RequestMapping("/manage/news_info")
@Slf4j
public class NewsInfoController {

    @Resource
    private NewsInfoService newsInfoService;

    @Resource
    private NewsInfoEsService newsInfoEsService;

    @RequestMapping("/show_news_info")
    @ResponseBody
    public ResponseResult showNewsInfo(@RequestBody QryNewsInfoReq req){
        log.info("查询新闻入参{}",req);
        LambdaQueryWrapper<NewsInfoDO> wrapper = new LambdaQueryWrapper<NewsInfoDO>()
                .eq(StringUtils.isNotEmpty(req.getKeyword()),NewsInfoDO::getKeyword,req.getKeyword())
                .eq(StringUtils.isNotEmpty(req.getSource()),NewsInfoDO::getSource,req.getSource())
                .like(StringUtils.isNotEmpty(req.getTitle()),NewsInfoDO::getTitle,req.getTitle())
                .between(StringUtils.isNotEmpty(req.getCreateTimeStart()) && StringUtils.isNotEmpty(req.getCreateTimeEnd()),NewsInfoDO::getTime,req.getCreateTimeStart(),req.getCreateTimeEnd())
                .orderByDesc(NewsInfoDO::getTime)
                ;
        IPage<NewsInfoDO> page = newsInfoService.page(new Page<>(req.getPage(),req.getLimit()),wrapper);
        log.info("查询新闻信息成功{}", JSONUtil.toJsonStr(page));
        return ResponseResult.success(page.getTotal(),page.getRecords());
    }
    @RequestMapping("/show_news_info_es")
    @ResponseBody
    public ResponseResult showNewsInfoEs(@RequestBody QryNewsInfoReq req){
        log.info("查询新闻入参{}",req);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(StringUtils.isNotEmpty(req.getTitle())){
            // 纠错匹配
            boolQueryBuilder.must(QueryBuilders.termQuery("title",req.getTitle()));
        }
        if(StringUtils.isNotEmpty(req.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.fuzzyQuery("keyword",req.getKeyword()));
        }
        if(StringUtils.isNotEmpty(req.getSource())){
            boolQueryBuilder.must(QueryBuilders.fuzzyQuery("source",req.getSource()));
        }
        if(StringUtils.isNotEmpty(req.getContent())){
            // 全文匹配
           boolQueryBuilder.must(QueryBuilders.matchQuery("content",req.getContent()));
        }
        if(StringUtils.isNotEmpty(req.getCreateTimeStart()) && StringUtils.isNotEmpty(req.getCreateTimeEnd())){
            boolQueryBuilder.must(QueryBuilders.rangeQuery("time").gte(req.getCreateTimeStart()).lte(req.getCreateTimeEnd()));
        }
        String sorter = "time";
        Sort.Order order = new Sort.Order(SortOrder.DESC,sorter);
        PageSortHighLight pageSortHighLight = new PageSortHighLight(req.getPage(),req.getLimit());
        pageSortHighLight.setSort(new Sort(order));
        Attach attach = new Attach();
        attach.setPageSortHighLight(pageSortHighLight);
        PageList<NewsInfoEsDO> pageList = newsInfoEsService.searchByPage(boolQueryBuilder,attach);
        log.info("查询新闻信息成功{}",pageList.getList());
        return ResponseResult.success(pageList.getTotalElements(),pageList.getList());
    }

    @RequestMapping("/export_news_info")
    @ResponseBody
    public ResponseResult exportNewsInfo(@RequestBody QryNewsInfoReq req){
        log.info("导出新闻入参{}",req);
        // 默认导出一礼拜
        String start = req.getCreateTimeStart();
        String end = req.getCreateTimeEnd();
        if(StringUtils.isEmpty(req.getCreateTimeStart()) && StringUtils.isEmpty(req.getCreateTimeStart())){
            start = LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
            end = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 24:00:00";
        }
        LambdaQueryWrapper<NewsInfoDO> wrapper = new LambdaQueryWrapper<NewsInfoDO>()
                .eq(StringUtils.isNotEmpty(req.getKeyword()),NewsInfoDO::getKeyword,req.getKeyword())
                .eq(StringUtils.isNotEmpty(req.getSource()),NewsInfoDO::getSource,req.getSource())
                .like(StringUtils.isNotEmpty(req.getTitle()),NewsInfoDO::getTitle,req.getTitle())
                .between(StringUtils.isNotEmpty(start) && StringUtils.isNotEmpty(end),NewsInfoDO::getTime,start,end)
                .orderByDesc(NewsInfoDO::getTime)
                ;
        List<NewsInfoDO> newsInfoDos = newsInfoService.list(wrapper);
        log.info("导出新闻信息成功,新闻数量：{}",newsInfoDos.size());
        return ResponseResult.success(Long.valueOf(newsInfoDos.size()),newsInfoDos);
    }


    @RequestMapping("/export_news_info_es")
    @ResponseBody
    public ResponseResult exportNewsInfoEs(@RequestBody QryNewsInfoReq req){
        log.info("导出新闻es入参{}",req);
        // 默认导出一礼拜
        String start = req.getCreateTimeStart();
        String end = req.getCreateTimeEnd();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(StringUtils.isNotEmpty(req.getTitle())){
            // 纠错匹配
            boolQueryBuilder.must(QueryBuilders.termQuery("title",req.getTitle()));
        }
        if(StringUtils.isNotEmpty(req.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.fuzzyQuery("keyword",req.getKeyword()));
        }
        if(StringUtils.isNotEmpty(req.getSource())){
            boolQueryBuilder.must(QueryBuilders.fuzzyQuery("source",req.getSource()));
        }
        if(StringUtils.isNotEmpty(req.getContent())){
            // 全文匹配
            boolQueryBuilder.must(QueryBuilders.matchQuery("content",req.getContent()));
        }
        if(StringUtils.isNotEmpty(start) && StringUtils.isNotEmpty(end)){
            boolQueryBuilder.must(QueryBuilders.rangeQuery("time").gte(start).lte(end));
        }else{
            start = LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
            end = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 24:00:00";
            boolQueryBuilder.must(QueryBuilders.rangeQuery("time").gte(start).lte(end));
        }
        List<NewsInfoEsDO> newsInfoEsDos = newsInfoEsService.search(boolQueryBuilder);
        log.info("导出新闻信息成功,新闻数量：{}",newsInfoEsDos.size());
        return ResponseResult.success(Long.valueOf(newsInfoEsDos.size()),newsInfoEsDos);
    }
}
