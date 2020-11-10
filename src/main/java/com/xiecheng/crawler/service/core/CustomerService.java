package com.xiecheng.crawler.service.core;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiecheng.crawler.entity.po.CustomerDO;
import com.xiecheng.crawler.mapper.CustomerMapper;
import org.springframework.stereotype.Service;

/**
 * @author nijichang
 * @since 2020-11-10 10:24:34
 */
@Service
public class CustomerService extends ServiceImpl<CustomerMapper, CustomerDO> {
}
