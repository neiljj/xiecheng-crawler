package com.xiecheng.crawler.utils.mapstruct;

import com.xiecheng.crawler.entity.NewsInfoDO;
import com.xiecheng.crawler.entity.es.po.NewsInfoEsDO;
import com.xiecheng.crawler.entity.po.CrawlerTaskDO;
import com.xiecheng.crawler.entity.po.CustomerDO;
import com.xiecheng.crawler.entity.vo.CustomerVo;
import com.xiecheng.crawler.entity.vo.req.AddCrawlerTaskReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;

/**
 * @author nijichang
 * @since 2020-09-14 15:16:02
 */
@Mapper(componentModel = "spring",uses = DateMapper.class)
public interface DataMapping {

    CustomerDO toCustomerDoO(CustomerVo customerVo);

    CrawlerTaskDO toCrawlerTaskDO(AddCrawlerTaskReq vo);

    @Mappings({
           @org.mapstruct.Mapping(source = "createTime",target = "create_time"),
           @org.mapstruct.Mapping(source = "time",target = "time")
    })
    NewsInfoEsDO toEsDo(NewsInfoDO newsInfoDO);
}
