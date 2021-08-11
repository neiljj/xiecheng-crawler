package com.xiecheng.crawler.service.weibo.biz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiecheng.crawler.entity.NewsInfoDO;
import com.xiecheng.crawler.entity.es.po.NewsInfoEsDO;
import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.common.impl.NewsInfoEsService;
import com.xiecheng.crawler.utils.Utils;
import com.xiecheng.crawler.service.common.impl.NewsInfoService;
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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 微博爬虫
 * @author nijichang
 * @since 2021-06-23 17:54:05
 */
@Service
@Slf4j
public class WeiboCrawler {
    @Resource
    private NewsInfoService newsInfoService;

    private static String URL = "https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D61%26q%3D@keyword%26t%3D0&page_type=searchall";

    public void run(Map<CrawlerEnum,List<String>> keywordMap){
        List<String> keywordList = keywordMap.get(CrawlerEnum.WEIBO);
        if(CollectionUtils.isEmpty(keywordList)){
            log.info("微博关键词为空");
            return;
        }
        AtomicInteger no = new AtomicInteger(0);
        for(String keyword : keywordList) {
            log.info("微博采集关键词：{}",keyword);
            String url = URL.replace("@keyword", UriEncoder.encode(keyword));
            String page = HttpUtils.doGet(url,null,5000,"utf-8");
            if(StringUtils.isNotEmpty(page)) {
                saveNews(page, keyword, no);
            }
            try{
                Thread.sleep(5000);
            }catch (InterruptedException e){
                log.error("weibo crawler InterruptedException ");
            }
        }
        log.info("微博采集结束,本轮新增新闻{}条",no);
    }

    /**
     * 网页解析
     * @author nijichang
     * @since 2021/6/22 6:25 PM
     */
    private void saveNews(String page,String keyword,AtomicInteger no){
        JSONObject object = JSON.parseObject(page);
        JSONArray cards = object.getJSONObject("data").getJSONArray("cards");
        if(Objects.nonNull(cards)) {
            for (int i = 0; i < cards.size(); i++) {
                String url = cards.getJSONObject(i).getString("scheme");
                String content = cards.getJSONObject(i).getJSONObject("mblog").getString("text");
                String time = cards.getJSONObject(i).getJSONObject("mblog").getString("created_at");
                time = Utils.dateTransfer(time);
                NewsInfoDO newsInfoDO = new NewsInfoDO().setContent(content)
                        .setKeyword(keyword)
                        .setSource(CrawlerEnum.WEIBO.getDesc())
                        .setTitle(StringUtils.EMPTY).setUrl(url).setTime(time);
                try {
                    newsInfoService.save(newsInfoDO);
                    no.getAndIncrement();
                } catch (DuplicateKeyException | PersistenceException e) {
                    log.error("该条新闻已经存在 ,{}", content);
                }
            }
        }else {
            log.error("微博关键词：{}页面为空",keyword);
        }
    }

    public static void main(String[] args){
        //https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D61%26q%3Dktv%26t%3D0&page_type=searchall
        while(true) {
            String url = "https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D61%26q%3D@keyword%26t%3D0&page_type=searchall";
            String keyword = UriEncoder.encode("新冠疫苗");
            url = url.replace("@keyword", keyword);
            String page = HttpUtils.doGet(url, null, 5000, "utf-8");
            WeiboCrawler weiboCrawler = new WeiboCrawler();
//            weiboCrawler.saveNews(page, "");
            JSONObject object = JSON.parseObject(page);
            JSONArray cards = object.getJSONObject("data").getJSONArray("cards");
            System.out.println(cards.size());
            try{
                Thread.sleep(5000);
            }catch (InterruptedException e){
                log.error("weibo crawler InterruptedException ");
            }
        }
    }


}
