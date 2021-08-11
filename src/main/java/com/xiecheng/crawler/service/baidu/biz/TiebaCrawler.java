package com.xiecheng.crawler.service.baidu.biz;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.xiecheng.crawler.entity.NewsInfoDO;
import com.xiecheng.crawler.entity.es.po.NewsInfoEsDO;
import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.baidu.BaiduCommonCrawlerProcessor;
import com.xiecheng.crawler.service.common.impl.NewsInfoEsService;
import com.xiecheng.crawler.service.common.impl.NewsInfoService;
import com.xiecheng.crawler.utils.HttpUtils;
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
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 百度贴吧 爬虫
 * @author nijichang
 * @since 2021-06-23 14:32:55
 */
@Service
@Slf4j
public class TiebaCrawler implements BaiduCommonCrawlerProcessor {
    @Resource
    private NewsInfoService newsInfoService;

    private static String URL = "https://tieba.baidu.com/f/search/res?ie=utf-8&qw=@keyword";

    @Override
    public void run(Map<CrawlerEnum,List<String>> keywordMap){
        List<String> keywordList = keywordMap.get(CrawlerEnum.TIEBA);
        if(CollectionUtils.isEmpty(keywordList)){
            log.info("贴吧关键词为空");
            return;
        }
        AtomicInteger no = new AtomicInteger(0);
        ExecutorService service = new ThreadPoolExecutor(5, 5,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(300),
                new ThreadFactoryImpl("tiebaThread_"));
        for(String keyword : keywordList) {
            submitTask(keyword,service,no);
        }
        //线程池首先需要shutdown再判断isTerminated
        service.shutdown();
        while(true){
            if(service.isTerminated()){
                log.info("百度贴吧采集结束，本轮新增新闻{}条",no);
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
        log.info("贴吧采集关键词，{}",keyword);
        service.execute(() -> {
            String url = URL.replace("@keyword", UriEncoder.encode(keyword));
            String page = HttpUtils.doGet(url,null,5000,"gbk");
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
        Document document = Jsoup.parse(page);
        Elements divs = document.select("div.s_post");
        if(Objects.nonNull(divs)) {
            for (Element div : divs) {
                String href = div.select("span.p_title").select("a").attr("href");
                if (StringUtils.isEmpty(href)) {
                    continue;
                }
                String url = "https://tieba.baidu.com" + href;
                String title = div.select("span.p_title").select("a").text();
                String content = div.select("div.p_content").text();
                String time = div.select("font.p_green.p_date").text();
                if (StringUtils.isNotEmpty(time)) {
                    time += ":00";
                }
                NewsInfoDO newsInfoDO = new NewsInfoDO().setContent(content)
                        .setKeyword(keyword)
                        .setSource(CrawlerEnum.TIEBA.getDesc())
                        .setTitle(title).setUrl(url).setTime(time);
                try {
                    newsInfoService.save(newsInfoDO);
                    no.getAndIncrement();
                } catch (DuplicateKeyException | PersistenceException e) {
                    log.error("该条新闻已经存在 ,{}", title);
                }
            }
        }else {
            log.error("百度贴吧关键词：{}页面为空",keyword);
        }
    }
    public static void main(String[] args){
        String url = "https://tieba.baidu.com/f/search/res?ie=utf-8&qw=%E6%96%B0%E5%86%A0%E7%96%AB%E8%8B%97";
        String page = HttpUtils.doGet(url,null,5000,"gbk");
        TiebaCrawler tiebaCrawler = new TiebaCrawler();
        tiebaCrawler.saveNews(page,"",null);
    }
}
