package com.xiecheng.crawler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 爬虫枚举
 * @author nijichang
 * @since 2021-06-22 15:58:13
 */
@Getter
@AllArgsConstructor
public enum CrawlerEnum {
    ZIXUN(1,"百度资讯"),
    TIEBA(2,"百度贴吧"),
    ZHIDAO(3,"百度知道"),
    WEIBO(4,"微博"),
    MAIDIAN(5,"迈点"),
    JIUDIANGAOCAN(6,"酒店高参"),
    HUANQIULUXUN(7,"环球旅讯"),
    ZHIHU(8,"知乎");
//    WEIXIN(9,"微信");
    private Integer code;
    private String desc;

    public static Optional<CrawlerEnum> getByCode(String code){
        for(CrawlerEnum crawlerEnum : values()){
            if(crawlerEnum.code.equals(code)) return Optional.of(crawlerEnum);
        }
        return Optional.empty();
    }

    public static Map<String,String> toMap(){
        Map<String, String> map = new LinkedHashMap<>();
        for(CrawlerEnum crawlerEnum : values()){
            map.put(crawlerEnum.getDesc(),String.valueOf(crawlerEnum.getCode()));
        }
        return map;
    }
}
