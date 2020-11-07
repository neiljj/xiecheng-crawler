package com.xiecheng.crawler.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author employee
 * @since 2020-10-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hotel_info")
public class HotelInfoDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 城市
     */
    private String city;

    private String hotelName;

    private String url;

    private String address;

    private String score;

    private String dpcount;

    private String shortName ;

    private String star;

    private String brand;

    private String type;

    private String price;

    private String param;

    private Date updateTime;

    private Date createTime;
}
