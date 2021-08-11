package com.xiecheng.crawler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author nijichang
 * @since 2021-06-22 17:57:14
 */
@Getter
@AllArgsConstructor
public enum IsDeleteEnum {
    No(0,"否"),
    YES(1,"是");
    private Integer code;
    private String desc;
}
