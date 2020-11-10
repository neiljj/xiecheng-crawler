package com.xiecheng.crawler.mapper;

import com.xiecheng.crawler.entity.po.HotelInfoDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author employee
 * @since 2020-10-30
 */
public interface HotelInfoMapper extends BaseMapper<HotelInfoDO> {

    int insertType(@Param("list") List<HotelInfoDO> list);

    int insertBrand(@Param("list") List<HotelInfoDO> list);
}
