package com.xiecheng.crawler.entity.vo.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 添加关键词req
 * @author nijichang
 * @since 2021-06-28 10:06:44
 */
@Data
public class AddKeywordReq implements Serializable {
    @NotEmpty
    private String keyword;
    @NotEmpty
    private String source;
}
