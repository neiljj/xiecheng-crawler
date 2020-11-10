package com.xiecheng.crawler.service.core;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiecheng.crawler.mapper.HotelInfoMapper;
import com.xiecheng.crawler.entity.po.HotelInfoDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author nijichang
 * @since 2020-10-30 18:34:03
 */
@Service
@Slf4j
public class HotelnfoService extends ServiceImpl<HotelInfoMapper, HotelInfoDO> {
    @Resource
    private HotelInfoMapper hotelInfoMapper;

    public int insertType(List<HotelInfoDO> list){
        list.forEach(t -> t.setUpdateTime(new Date()));
        return hotelInfoMapper.insertType(list);
    }

    public int insertBrand(List<HotelInfoDO> list){
        list.forEach(t -> t.setUpdateTime(new Date()));
        return hotelInfoMapper.insertType(list);
    }
}
