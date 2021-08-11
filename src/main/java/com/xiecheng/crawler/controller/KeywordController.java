package com.xiecheng.crawler.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiecheng.crawler.entity.KeywordDO;
import com.xiecheng.crawler.entity.KeywordSourceDO;
import com.xiecheng.crawler.entity.ResponseResult;
import com.xiecheng.crawler.entity.StartFactoryLogDO;
import com.xiecheng.crawler.entity.vo.req.AddKeywordReq;
import com.xiecheng.crawler.entity.vo.req.BaseReq;
import com.xiecheng.crawler.entity.vo.req.DeleteKeywordReq;
import com.xiecheng.crawler.service.CrawlerFactory;
import com.xiecheng.crawler.service.common.impl.KeywordService;
import com.xiecheng.crawler.service.common.impl.KeywordSourceService;
import com.xiecheng.crawler.service.common.impl.StartFactoryLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 关键词 controller
 * @author nijichang
 * @since 2021-06-28 09:50:57
 */
@Controller
@RequestMapping("/manage/keyword")
@Slf4j
public class KeywordController {

    @Resource
    private KeywordService keywordService;

    @Resource
    private KeywordSourceService keywordSourceService;

    @Resource
    private CrawlerFactory crawlerFactory;

    @Resource
    private StartFactoryLogService startFactoryLogService;

    @RequestMapping("/show_keyword")
    @ResponseBody
    public ResponseResult showKeyword(@RequestBody BaseReq req){
        log.info("分页查询关键词入参{}",req);
        IPage<KeywordDO> page = keywordService.page(new Page<>(req.getPage(),req.getLimit()));
        log.info("分页查询关键词成功{}",JSONUtil.toJsonStr(page));
        return ResponseResult.success(page.getTotal(),page.getRecords());
    }

    @RequestMapping("/add_keyword")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult addKeyword(@Validated AddKeywordReq req){
        log.info("添加关键词入参{}",req);
        KeywordDO keywordDO = new KeywordDO().setKeyword(req.getKeyword()).setSource(req.getSource()).setUserId(1L).setIsDelete(0);
        keywordService.save(keywordDO);
        List<String> sources = JSONUtil.parseArray(req.getSource()).toList(String.class);
        List<KeywordSourceDO> keywordSourceDos = new ArrayList<>();
        for(String source : sources){
            KeywordSourceDO keywordSourceDO = new KeywordSourceDO().setKeywordId(keywordDO.getId()).setKeyword(req.getKeyword()).setSource(source).setIsDelete(0);
            keywordSourceDos.add(keywordSourceDO);
        }
        keywordSourceService.saveBatch(keywordSourceDos);
        return ResponseResult.success();
    }

    @RequestMapping("/delete_keyword")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult deleteKeyword(@Validated DeleteKeywordReq req){
        log.info("删除关键词入参{}",req);
        KeywordDO keywordDO = new KeywordDO().setId(req.getId()).setIsDelete(1);
        keywordService.updateById(keywordDO);
        LambdaUpdateWrapper<KeywordSourceDO> wrapper = new LambdaUpdateWrapper<KeywordSourceDO>()
                .eq(KeywordSourceDO::getKeywordId,req.getId())
                .set(KeywordSourceDO::getIsDelete,1);
        keywordSourceService.update(wrapper);
        return ResponseResult.success();
    }

    @RequestMapping("/start_factory_once")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult startFactoryOnce(){
        // 先查询是否存在未完成任务
        LambdaQueryWrapper<StartFactoryLogDO> wrapper = new LambdaQueryWrapper<StartFactoryLogDO>()
                .eq(StartFactoryLogDO::getStatus,0);
        List<StartFactoryLogDO> startFactoryLog = startFactoryLogService.list(wrapper);
        if(startFactoryLog.size() > 0){
            return ResponseResult.fail("任务未完成，请勿重复启动");
        }
        StartFactoryLogDO startFactoryLogDO = new StartFactoryLogDO().setStatus(0).setTimestrap(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        startFactoryLogService.save(startFactoryLogDO);
        // 爬虫工厂开始
        CompletableFuture.runAsync(()-> crawlerFactory.factoryBeginWork()).thenAccept(t -> {
            // 状态更新
            LambdaUpdateWrapper<StartFactoryLogDO> updateWrapper = new LambdaUpdateWrapper<StartFactoryLogDO>()
                    .eq(StartFactoryLogDO::getId,startFactoryLogDO.getId())
                    .set(StartFactoryLogDO::getStatus,1);
            startFactoryLogService.update(updateWrapper);
        });
        return ResponseResult.success();
    }

}
