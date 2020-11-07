package com.xiecheng.crawler.entity.vo.req;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @author nijichang
 * @since 2020-11-07 14:19:11
 */
@Data
public class BaseReq implements Serializable {
    private String createTimeStart;
    private String createTimeEnd;
    private Integer page = 1;
    private Integer per = 10;
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
