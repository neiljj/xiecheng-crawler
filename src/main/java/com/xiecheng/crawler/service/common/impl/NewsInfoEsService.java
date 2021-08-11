package com.xiecheng.crawler.service.common.impl;

import com.alibaba.fastjson.JSON;
import com.xiecheng.crawler.entity.es.po.NewsInfoEsDO;
import com.xiecheng.crawler.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import org.zxp.esclientrhl.repository.Attach;
import org.zxp.esclientrhl.repository.ElasticsearchTemplate;
import org.zxp.esclientrhl.repository.PageList;

import javax.annotation.Resource;
import java.util.List;

/**
 * newsInfo es
 * @author nijichang
 * @since 2021-07-26 14:38:05
 */
@Service
@Slf4j
public class NewsInfoEsService {

    @Resource
    private ElasticsearchTemplate<NewsInfoEsDO,String> elasticsearchTemplate;

    /**
     * 单条查询
     * @author nijichang
     * @since 2021/7/26 2:41 PM
     */
    public NewsInfoEsDO qryNewsInfoEsDO(Long id){
        NewsInfoEsDO newsInfoEsDO ;
        try{
            log.info("[根据精确条件查询新闻信息]调用ES服务查询新闻信息,id:{}",id);
            newsInfoEsDO = elasticsearchTemplate.getById(String.valueOf(id),NewsInfoEsDO.class);
            log.info("[根据精确条件查询新闻信息]调用ES服务查询新闻信息,返回:{}",newsInfoEsDO);
        }catch (Exception e){
            throw new BizException("es查询新闻信息失败，异常信息:" + StringUtils.truncate(e.getMessage(),200));
        }
        return newsInfoEsDO;
    }
    /**
     * 批量查询
     * @author nijichang
     * @since 2021/7/26 2:58 PM
     */
    public List<NewsInfoEsDO> batchQryNewsInfoEsDO(List<Long> ids){
        String[] idArray = ids.toArray(new String[ids.size()]);
        List<NewsInfoEsDO> newsInfoEsDos ;
        try{
            log.info("[批量查询新闻信息]调用ES服务批量查询新闻信息,ids:{}",ids);
            newsInfoEsDos = elasticsearchTemplate.mgetById(idArray,NewsInfoEsDO.class);
            log.info("[批量查询新闻信息]调用ES服务批量查询新闻信息,返回:{}",newsInfoEsDos);
        }catch (Exception e){
            throw new BizException("es查询新闻信息失败，异常信息:" + StringUtils.truncate(e.getMessage(),200));
        }
        return newsInfoEsDos;
    }

    /**
     * 分页查询
     * @author nijichang
     * @since 2021/7/26 2:59 PM
     */
    public PageList<NewsInfoEsDO> searchByPage(BoolQueryBuilder builder, Attach attach){
        PageList<NewsInfoEsDO> page ;
        try {
            log.info("[分页查询新闻信息]调用ES服务查询新闻分页列表,参数:{} ", JSON.toJSONString(builder));
            page = elasticsearchTemplate.search(builder, attach, NewsInfoEsDO.class);
            log.info("[分页查询新闻信息]调用ES服务查询新闻分页列表,返回信息:{}", JSON.toJSONString(page));
        } catch (Exception e) {
            log.error("[分页查询新闻信息]调用ES服务查询新闻分页列表失败,参数:{}  ", JSON.toJSONString(builder), e);
            throw new BizException("[分页查询新闻信息]调用ES查询新闻分页列表失败,异常信息：" + StringUtils.truncate(e.getMessage(), 200), true);
        }
        return page;
    }

    /**
     * 批量查询
     * @author nijichang
     * @since 2021/8/5 10:37 AM
     */
    public List<NewsInfoEsDO> search(QueryBuilder queryBuilder){
        try {
            log.info("[查询ES数据]调用ES服务查询ES数据,入参:{}",JSON.toJSONString(queryBuilder));
            List<NewsInfoEsDO> result = elasticsearchTemplate.search(queryBuilder,NewsInfoEsDO.class);
            log.info("[查询ES数据]调用ES服务查询ES数据成功");
            return result;
        } catch (Exception e) {
            log.error("[查询ES数据]调用ES服务查询ES数据失败,  ", e);
            throw new BizException("[查询ES数据]调用ES服务查询ES数据失败,异常信息：" + StringUtils.truncate(e.getMessage(), 200), true);
        }
    }

    public boolean save(NewsInfoEsDO newsInfoEsDO){
        try {
            log.info("[新增ES数据]调用ES服务新增ES数据,入参:{}",JSON.toJSONString(newsInfoEsDO));
            boolean result = elasticsearchTemplate.save(newsInfoEsDO);
            log.info("[新增ES数据]调用ES服务新增ES数据成功");
            return result;
        } catch (Exception e) {
            log.error("[新增ES数据]调用ES服务新增ES数据失败,id:{}  ", newsInfoEsDO.getId(), e);
            throw new BizException("[新增ES数据]调用ES服务新增ES数据失败,异常信息：" + StringUtils.truncate(e.getMessage(), 200), true);
        }
    }

    public void saveBatch(List<NewsInfoEsDO> newsInfoEsDOs){
        try {
            BulkResponse[] result = elasticsearchTemplate.saveBatch(newsInfoEsDOs);
        } catch (Exception e) {
            log.error("[批量新增ES数据]调用ES服务批量新增ES数据失败");
            throw new BizException("[批量新增ES数据]调用ES服务新增ES数据失败,异常信息：" + StringUtils.truncate(e.getMessage(), 200), true);
        }
    }

    public boolean update(NewsInfoEsDO newsInfoEsDO){
        try {
            log.info("[更新ES数据]调用ES服务更新ES数据,入参:{}",JSON.toJSONString(newsInfoEsDO));
            boolean result = elasticsearchTemplate.updateCover(newsInfoEsDO);
            log.info("[更新ES数据]调用ES服务更新ES数据成功");
            return result;
        } catch (Exception e) {
            log.error("[更新ES数据]调用ES服务更新ES数据失败,id:{}  ", newsInfoEsDO.getId(), e);
            throw new BizException("[更新ES数据]调用ES服务更新ES数据失败,异常信息：" + StringUtils.truncate(e.getMessage(), 200), true);
        }
    }
}
