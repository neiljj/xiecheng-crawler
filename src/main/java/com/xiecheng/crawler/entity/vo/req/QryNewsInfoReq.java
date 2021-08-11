package com.xiecheng.crawler.entity.vo.req;

import lombok.Data;

/**
 * 查询新闻req
 * @author nijichang
 * @since 2021-06-28 10:04:01
 */
@Data
public class QryNewsInfoReq extends BaseReq  {

    private String keyword;

    private String title;

    private String content;

    private String source;


}
