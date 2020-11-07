package com.xiecheng.crawler.entity.vo.req;

import lombok.Data;

/**
 * @author nijichang
 * @since 2020-11-07 14:20:57
 */
@Data
public class QryHotelInfoReq extends BaseReq {

    private String city;

    private String brand;

    private String type;

    private String hotelName;

    /**
     * 价格区间
     */
    private String priceBegin;

    private String priceEnd;
}
