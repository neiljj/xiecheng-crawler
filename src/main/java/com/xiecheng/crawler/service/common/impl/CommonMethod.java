package com.xiecheng.crawler.service.common.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiecheng.crawler.entity.KeywordDO;
import com.xiecheng.crawler.entity.KeywordSourceDO;
import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.enums.IsDeleteEnum;
import com.xiecheng.crawler.service.common.impl.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公用方法
 * @author nijichang
 * @since 2021-06-22 18:07:26
 */
@Service
@Slf4j
public class CommonMethod {
    @Resource
    private KeywordService keywordService;

    @Resource
    private KeywordSourceService keywordSourceService;

    /**
     * 初始化关键词map
     * @author nijichang
     * @since 2021/6/22 5:55 PM
     */
    public void initKeyword(List<String> keywordList){
        LambdaQueryWrapper<KeywordDO> wrapper = new LambdaQueryWrapper<KeywordDO>()
                .eq(KeywordDO::getIsDelete, IsDeleteEnum.No.getCode())
                .select(KeywordDO::getId,KeywordDO::getKeyword,KeywordDO::getSource);
        List<KeywordDO> keywordDos = keywordService.list(wrapper);
        List<String> words = keywordDos.stream().map(KeywordDO::getKeyword).collect(Collectors.toList());
        for(String word : words){
            String[] keyword = word.split("\\|");
            keywordList.addAll(Arrays.stream(keyword).collect(Collectors.toList()));
        }
        keywordList.stream().distinct().collect(Collectors.toList());
    }


    public void initKeyword(Map<CrawlerEnum,List<String>> keywordMap){
        for(CrawlerEnum crawlerEnum: CrawlerEnum.values()){
            LambdaQueryWrapper<KeywordSourceDO> wrapper = new LambdaQueryWrapper<KeywordSourceDO>()
                    .select(KeywordSourceDO::getKeyword,KeywordSourceDO::getSource)
                    .eq(KeywordSourceDO::getIsDelete, IsDeleteEnum.No.getCode())
                    .eq(KeywordSourceDO::getSource,crawlerEnum.getDesc());
            List<String> keywordList = new ArrayList<>();
            List<KeywordSourceDO> keywordSourceDos = keywordSourceService.list(wrapper);
            List<String> words = keywordSourceDos.stream().map(KeywordSourceDO::getKeyword).collect(Collectors.toList());
            for(String word : words){
                String[] keyword = word.split("\\|");
                keywordList.addAll(Arrays.stream(keyword).collect(Collectors.toList()));
            }
            keywordMap.put(crawlerEnum,keywordList.stream().distinct().collect(Collectors.toList()));
        }
    }
}
