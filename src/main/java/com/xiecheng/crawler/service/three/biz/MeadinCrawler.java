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
 * 迈点爬虫
 * @author nijichang
 * @since 2021-06-25 18:26:46
 */
@Service
@Slf4j
public class MeadinCrawler implements ThreeCommonCrawlerProcessor {
    @Resource
    private NewsInfoService newsInfoService;
    //    @Value("${baidu.tieba.url}")
    private static String URL = "https://api.meadin.com/rest/v1/common/info_search?key_word=@keyword&pageCount.currentPage=1&pageCount.showCount=30";

    @Override
    public void run(Map<CrawlerEnum,List<String>> keywordMap){
        List<String> keywordList = keywordMap.get(CrawlerEnum.MAIDIAN);
        if(CollectionUtils.isEmpty(keywordList)){
            log.info("迈点关键词为空");
            return;
        }
        ExecutorService service = new ThreadPoolExecutor(3, 3,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(300),
                new ThreadFactoryImpl("meadinThread_"));
        AtomicInteger no = new AtomicInteger(0);
        for(String keyword : keywordList) {
            submitTask(keyword,service,no);
        }
        // 线程池首先需要shutdown再判断isTerminated
        service.shutdown();
        while(true){
            if(service.isTerminated()){
                log.info("迈点采集结束,本轮新增新闻{}条",no);
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
        log.info("迈点采集关键词，{}",keyword);
        service.execute(() -> {
            String url = URL.replace("@keyword", UriEncoder.encode(keyword));
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
        JSONObject data = object.getJSONObject("data");
        JSONArray list = data.getJSONArray("list");
        if(!Objects.isNull(list)) {
            for (int i = 0; i < list.size(); i++) {
                JSONObject info = list.getJSONObject(i);
                String title = info.getString("title");
                String time = info.getString("publishDate");
                String content = info.getString("metaDescription");
                String url = "https://www.meadin.com/jd/" + info.getString("id") + ".html";
                NewsInfoDO newsInfoDO = new NewsInfoDO().setContent(content)
                        .setKeyword(keyword)
                        .setSource(CrawlerEnum.MAIDIAN.getDesc())
                        .setTitle(title).setUrl(url).setTime(time);
                try {
                    newsInfoService.save(newsInfoDO);
                    no.getAndIncrement();
                } catch (DuplicateKeyException | PersistenceException e) {
                    log.error("该条新闻已经存在 ,{}", title);
                }
            }
        }else {
            log.error("迈点关键词：{}页面为空",keyword);
        }
    }

    public static void main(String[] args){
        String url = "https://api.meadin.com/rest/v1/common/info_search?key_word=ktv&pageCount.currentPage=1&pageCount.showCount=30";
        String page = HttpUtils.doGet(url,null,5000,"utf-8");
        MeadinCrawler meadinCrawler = new MeadinCrawler();
        meadinCrawler.saveNews(page,"",null);
    }
}
