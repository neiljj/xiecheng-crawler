package com.xiecheng.crawler.service.three.biz;



import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.xiecheng.crawler.entity.NewsInfoDO;
import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.common.impl.NewsInfoEsService;
import com.xiecheng.crawler.service.common.impl.NewsInfoService;
import com.xiecheng.crawler.service.three.ThreeCommonCrawlerProcessor;
import com.xiecheng.crawler.utils.HttpUtils;
import com.xiecheng.crawler.utils.Utils;
import com.xiecheng.crawler.utils.mapstruct.DataMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
 * 环球旅讯 爬虫
 * @author nijichang
 * @since 2021-06-26 14:13:12
 */
@Slf4j
@Service
public class TravelDailyCrawler implements ThreeCommonCrawlerProcessor {

    @Resource
    private NewsInfoService newsInfoService;

    private static String ARTICLE_URL = "https://www.traveldaily.cn/search/article/?kw=@keyword&sort=2";

    private static String EXPRESS_URL = "https://www.traveldaily.cn/search/express/?kw=@keyword&sort=2";

    @Override
    public void run(Map<CrawlerEnum,List<String>> keywordMap){

        List<String> keywordList = keywordMap.get(CrawlerEnum.HUANQIULUXUN);
        if(CollectionUtils.isEmpty(keywordList)){
            log.info("环球旅讯关键词为空");
            return;
        }
        ExecutorService service = new ThreadPoolExecutor(3, 3,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(300),
                new ThreadFactoryImpl("travelDailyThread_"));
        AtomicInteger no = new AtomicInteger(0);
        for(String keyword : keywordList) {
            submitTask(keyword,service,no);
        }
        // 线程池首先需要shutdown再判断isTerminated
        service.shutdown();
        while(true){
            if(service.isTerminated()){
                log.info("环球旅讯采集结束,本轮新增新闻{}条",no);
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
        log.info("环球旅讯采集关键词，{}",keyword);
        service.execute(() -> {
            // 文章
            String url = ARTICLE_URL.replace("@keyword", UriEncoder.encode(keyword));
            String page = HttpUtils.doGet(url,null,5000,"utf-8");
            if(StringUtils.isNotEmpty(page)) {
                saveNews(page, keyword, no);
            }
            // 快讯
            String expressUrl = EXPRESS_URL.replace("@keyword", UriEncoder.encode(keyword));
            String expressPage = HttpUtils.doGet(expressUrl,null,5000,"utf-8");
            if (StringUtils.isNotEmpty(expressPage)) {
                saveNews(expressPage, keyword, no);
            }
        });
    }
    /**
     * 网页解析
     * @author nijichang
     * @since 2021/6/23 10:48 AM
     */
    private void saveNews(String page,String keyword,AtomicInteger no){
        Document document = Jsoup.parse(page);
        Elements articleItems = document.select("div.articleItem");
        if(Objects.nonNull(articleItems)) {
            for (Element item : articleItems) {
                String url = "https://www.traveldaily.cn" + item.select("a.articleItemTitleLink").attr("href");
                String title = item.select("a.articleItemTitleLink").text();
                String content = item.select("p.articleItemDesc").text();
                String time = item.select("span.articleItemTime").text();
                NewsInfoDO newsInfoDO = new NewsInfoDO().setContent(content)
                        .setKeyword(keyword)
                        .setSource(CrawlerEnum.HUANQIULUXUN.getDesc())
                        .setTitle(title).setUrl(url).setTime(Utils.offsetToDateString(time));
                try {
                    newsInfoService.save(newsInfoDO);
                    no.getAndIncrement();
                } catch (DuplicateKeyException | PersistenceException e) {
                    log.error("该条新闻已经存在 ,{}",title);
                }
            }
        }else {
            log.error("环球旅讯关键词：{}页面为空",keyword);
        }
    }

    public static void main(String[] args){
        String url = "https://www.traveldaily.cn/search/express/?kw=%E7%BE%8E%E5%9B%A2&page=1&sort=2";
        String page = HttpUtils.doGet(url,null,5000,"utf-8");
        TravelDailyCrawler travelDailyCrawler = new TravelDailyCrawler();
        travelDailyCrawler.saveNews(page,"",null);

    }
}
