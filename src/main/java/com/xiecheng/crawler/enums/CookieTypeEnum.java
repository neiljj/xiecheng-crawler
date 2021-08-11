package com.xiecheng.crawler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * cookie类型枚举
 * @author nijichang
 * @since 2021-06-23 15:00:32
 */
@Getter
@AllArgsConstructor
public enum CookieTypeEnum {
    ZHIDAO(1,"百度知道"),
    WEIBO(2,"微博"),
    ZHIHU(3,"知乎");
    private Integer code;
    private String desc;

    public static Map<String,String> toMap(){
        Map<String, String> map = new LinkedHashMap<>();
        for(CookieTypeEnum cookieTypeEnum : values()){
            map.put(cookieTypeEnum.getDesc(),String.valueOf(cookieTypeEnum.getCode()));
        }
        return map;
    }
}
