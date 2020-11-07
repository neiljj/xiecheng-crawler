package com.xiecheng.crawler.entity.vo.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * @author nijichang
 * @since 2020-11-07 14:20:17
 */
@Data
public class BaseResp implements Serializable {
    private Integer code;
    private String msg;
}
