package com.xiecheng.crawler.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author employee
 * @since 2020-11-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("crawler_task")
public class CrawlerTaskDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 城市
     */
    private String city;

    /**
     * 类型
     */
    private String type;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 0新建1正在采集，2完成
     */
    private Integer status;

    /**
     * 完成请求参数
     */
    private String param;

    private LocalDateTime createTime;


}
