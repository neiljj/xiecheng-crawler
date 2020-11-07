package com.xiecheng.crawler.entity;

import lombok.Data;

/**
 * @author nijichang
 * @since 2020-11-02 15:02:18
 */
@Data
public class Task {

    private String param;

    /**
     * 参数类型，1表示城市+类型，2表示城市+品牌
     */
    private Integer paramTag;

    /**
     * 任务类型，0表示初始化，1表示翻页，2表示二层
     */
    private Integer depthTag;
}
