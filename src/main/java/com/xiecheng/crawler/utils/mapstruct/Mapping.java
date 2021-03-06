package com.xiecheng.crawler.utils.mapstruct;

import com.xiecheng.crawler.entity.po.CrawlerTaskDO;
import com.xiecheng.crawler.entity.po.CustomerDO;
import com.xiecheng.crawler.entity.vo.CustomerVo;
import com.xiecheng.crawler.entity.vo.req.AddCrawlerTaskReq;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author nijichang
 * @since 2020-09-14 15:16:02
 */
@Mapper
public interface Mapping {

    Mapping instance = Mappers.getMapper(Mapping.class);

    CustomerDO toCustomerDoO(CustomerVo customerVo);

    CrawlerTaskDO toCrawlerTaskDO(AddCrawlerTaskReq vo);
}
