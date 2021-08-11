package com.xiecheng.crawler.service.three.biz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.xiecheng.crawler.entity.NewsInfoDO;
import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.common.impl.NewsInfoEsService;
import com.xiecheng.crawler.service.common.impl.NewsInfoService;
import com.xiecheng.crawler.service.three.ThreeCommonCrawlerProcessor;
import com.xiecheng.crawler.utils.HttpUtils;
import com.xiecheng.crawler.utils.mapstruct.DataMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.util.UriEncoder;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 酒店高参 爬虫
 * @author nijichang
 * @since 2021-06-26 15:11:33
 */
@Service
@Slf4j
public class HotelnCrawler implements ThreeCommonCrawlerProcessor {
    @Resource
    private NewsInfoService newsInfoService;

    private static String URL = "http://www.hoteln.cn/api/QueryNewsListWithPage?Title=@keyword&SubClassID=&pageno=1&pagesize=20";

    @Override
    public void run(Map<CrawlerEnum,List<String>> keywordMap){
        List<String> keywordList = keywordMap.get(CrawlerEnum.JIUDIANGAOCAN);
        if(CollectionUtils.isEmpty(keywordList)){
            log.info("酒店高参关键词为空");
            return;
        }
        ExecutorService service = new ThreadPoolExecutor(3, 3,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(300),
                new ThreadFactoryImpl("hotelnThread_"));
        AtomicInteger no = new AtomicInteger(0);
        for(String keyword : keywordList) {
            submitTask(keyword,service,no);
        }
        // 线程池首先需要shutdown再判断isTerminated
        service.shutdown();
        while(true){
            if(service.isTerminated()){
                log.info("酒店高参采集结束,本轮新增新闻{}条",no);
                break;
            }
        }
    }
    /**
     * 向线程池提交任务
     * @author nijichang
     * @since 2021/6/23 10:21 AM
     */
    private void submitTask(String keyword,ExecutorService service,AtomicInteger no){
        log.info("酒店高参采集关键词，{}",keyword);
        service.execute(() -> {
            String url = URL.replace("@keyword", UriEncoder.encode(UriEncoder.encode(keyword)));
            String page = HttpUtils.doGet(url,null,5000,"utf-8");
            if(StringUtils.isNotEmpty(page)) {
                saveNews(page, keyword, no);
            }
        });
    }
    /**
     * 网页解析
     * @author nijichang
     * @since 2021/6/23 10:48 AM
     */
    private void saveNews(String page,String keyword,AtomicInteger no){
        JSONObject object = JSON.parseObject(page);
        JSONObject data = object.getJSONObject("Data");
        JSONArray dataSet = data.getJSONArray("DataSet");
        if(!Objects.isNull(dataSet)) {
            for (int i = 0; i < dataSet.size(); i++) {
                JSONObject set = dataSet.getJSONObject(i);
                String title = set.getString("Title");
                String content = set.getString("EnTitle");
                String time = set.getString("CreateTime");
                String url = "http://www.hoteln.cn/Html/HotelNewsSecond.html?NewsID="
                        + set.getString("NewsID") + "&ClassId=" + set.getString("ClassID") + "&SubClassID=" + set.getString("SubClassID");
                NewsInfoDO newsInfoDO = new NewsInfoDO().setContent(content)
                        .setKeyword(keyword)
                        .setSource(CrawlerEnum.JIUDIANGAOCAN.getDesc())
                        .setTitle(title).setUrl(url).setTime(time);
                try {
                    newsInfoService.save(newsInfoDO);
                    no.getAndIncrement();
                } catch (DuplicateKeyException | PersistenceException e) {
                    log.error("该条新闻已经存在 ,{}", title);
                }
            }
        }else {
            log.error("酒店高参关键词：{}页面为空",keyword);
        }
    }

    public static void main(String[] args){
        String url = "http://www.hoteln.cn/api/QueryNewsListWithPage?Title="+ UriEncoder.encode(UriEncoder.encode("新冠疫苗")) +"&SubClassID=&pageno=1&pagesize=20";
        String page = HttpUtils.doGet(url,null,5000,"utf-8");
        HotelnCrawler hotelnCrawler = new HotelnCrawler();
        hotelnCrawler.saveNews(page,"",null);
    }
}
