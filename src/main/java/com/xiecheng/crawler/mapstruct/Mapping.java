package com.xiecheng.crawler.mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author nijichang
 * @since 2020-09-14 15:16:02
 */
@Mapper
public interface Mapping {

    Mapping instance = Mappers.getMapper(Mapping.class);

 }
