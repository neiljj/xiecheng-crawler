package com.xiecheng.crawler.entity.vo.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author nijichang
 * @since 2020-11-11 17:40:54
 */
@Data
public class AddCrawlerTaskReq implements Serializable {

    @NotEmpty
    private String city;

    private String brand;

    private String type;

    private Integer status;
}
