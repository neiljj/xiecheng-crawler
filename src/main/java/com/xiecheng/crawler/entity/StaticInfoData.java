package com.xiecheng.crawler.entity;

import lombok.Data;

/**
 * @author nijichang
 * @since 2020-11-19 11:24:18
 */
@Data
public class StaticInfoData {

    private String masterHotelId;

    private head head = new head();

    @Data
    public static class head{
        private String Locale = "zh-CN";
        private String Currency = "CNY";
    }
}
