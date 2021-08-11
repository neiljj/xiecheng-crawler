package com.xiecheng.crawler.entity.vo.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新cookie
 * @author nijichang
 * @since 2021-06-28 10:38:50
 */
@Data
public class UpdateCookieReq implements Serializable {
    private Integer id;
    private String cookie;
    private Integer type;
}
