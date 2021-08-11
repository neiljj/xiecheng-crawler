package com.xiecheng.crawler.service.baidu.biz;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.xiecheng.crawler.entity.NewsInfoDO;
import com.xiecheng.crawler.entity.es.po.NewsInfoEsDO;
import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.baidu.BaiduCommonCrawlerProcessor;
import com.xiecheng.crawler.service.common.impl.NewsInfoEsService;
import com.xiecheng.crawler.utils.Utils;
import com.xiecheng.crawler.service.common.impl.NewsInfoService;
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
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 百度搜索爬虫
 * @author nijichang
 * @since 2021-06-22 17:26:47
 */
@Service
@Slf4j
public class ZixunCrawler implements BaiduCommonCrawlerProcessor {
    @Resource
    private NewsInfoService newsInfoService;
    // 按时间排序
    private static String URL="https://www.baidu.com/s?tn=news&rtt=4&bsst=1&cl=2&wd=@keyword&medium=0";

    // 按焦点排序
    private static String HOTURL = "https://www.baidu.com/s?tn=news&rtt=1&bsst=1&cl=2&wd=@keyword&medium=0";

    private static Integer pageNum = 2;

    @Override
    public void run(Map<CrawlerEnum,List<String>> keywordMap){
        List<String> keywordList = keywordMap.get(CrawlerEnum.ZIXUN);
        if(CollectionUtils.isEmpty(keywordList)){
            log.info("资讯关键词为空");
            return;
        }
        ExecutorService service = new ThreadPoolExecutor(5, 5,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(300),
                new ThreadFactoryImpl("zixunThread_"));
        AtomicInteger no = new AtomicInteger(0);
        for(String keyword : keywordList) {
            submitTask(keyword,service,no);
        }
        // 线程池首先需要shutdown再判断isTerminated
        service.shutdown();
        while(true){
            if(service.isTerminated()){
                log.info("百度资讯采集结束,本轮新增新闻{}条",no);
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
        log.info("百度资讯采集关键词，{}",keyword);
        service.execute(() -> {
            String url = URL.replace("@keyword", UriEncoder.encode(keyword));
            String page = Utils.crawlPage(url);
            if(StringUtils.isNotEmpty(page)) {
                saveNews(page, keyword, no);
            }
            // 翻页
            for(int i=1;i<pageNum;i++){
                url = url + "&pn=" + i*10;
                page = Utils.crawlPage(url);
                if(StringUtils.isNotEmpty(page)) {
                    saveNews(page, keyword, no);
                }
            }
            // 焦点排序
            String hotUrl = HOTURL.replace("@keyword", UriEncoder.encode(keyword));
            String hotPage = Utils.crawlPage(hotUrl);
            if(StringUtils.isNotEmpty(hotPage)) {
                saveNews(hotPage, keyword, no);
            }
            // 翻页
            for(int i=1;i<pageNum;i++){
                hotUrl = hotUrl + "&pn=" + i*10;
                hotPage = Utils.crawlPage(hotUrl);
                if(StringUtils.isNotEmpty(hotPage)) {
                    saveNews(hotPage, keyword, no);
                }
            }
        });
    }
    /**
     * 网页解析
     * @author nijichang
     * @since 2021/6/22 6:25 PM
     */

    private void saveNews(String page,String keyword,AtomicInteger no){
        Document document = Jsoup.parse(page);
        Elements divs = document.select("div.result-op.c-container.xpath-log.new-pmd");
        if(Objects.nonNull(divs)) {
            for (Element div : divs) {
                // url + title
                Element h3 = div.select("h3.news-title_1YtI1").first();
                String url = h3.select("a").attr("href");
                String title = h3.select("a").text();
                // time + content
                Element iDiv = div.selectFirst("div.c-row.c-gap-top-small");
                String time = iDiv.select("span.c-color-gray2.c-font-normal").text();
                String source = iDiv.select("span.c-color-gray.c-font-normal.c-gap-right").text();
                String content = source + " " + iDiv.select("span.c-font-normal.c-color-text").text();
                NewsInfoDO newsInfoDO = new NewsInfoDO().setContent(content)
                        .setKeyword(keyword)
                        .setSource(CrawlerEnum.ZIXUN.getDesc())
                        .setTitle(title).setUrl(url).setTime(Utils.offsetToDateString(time));
                try {
                    newsInfoService.save(newsInfoDO);
                    no.getAndIncrement();
                } catch (DuplicateKeyException | PersistenceException e) {
                    log.error("该条新闻已经存在 ,{}", title);
                }
                // es
//                try{
//                    NewsInfoEsDO newsInfoEsDO = dataMapping.toEsDo(newsInfoDO);
//                    newsInfoEsService.save(newsInfoEsDO);
//                }catch (DateTimeParseException e){
//                    log.error("日期格式有误");
//                }
            }
        }else {
            log.error("百度资讯关键词：{}页面为空",keyword);
        }
    }

    public static void main(String[] args){
        String url = "https://www.baidu.com/s?tn=news&rtt=4&bsst=1&cl=2&wd="+UriEncoder.encode("新冠疫苗")+"&medium=0&pn=20";
        String page = Utils.crawlPage(url);
        ZixunCrawler zixunCrawler = new ZixunCrawler();
        zixunCrawler.saveNews(page,"",null);
    }
}
