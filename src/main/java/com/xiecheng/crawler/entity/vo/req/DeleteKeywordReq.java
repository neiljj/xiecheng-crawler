package com.xiecheng.crawler.entity.vo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 删除keyword
 * @author nijichang
 * @since 2021-06-28 10:16:54
 */
@Data
public class DeleteKeywordReq implements Serializable {
    @NotNull
    private Long id;
}
