package com.qudian.xiecheng.crawler.service.biz;

import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.qudian.xiecheng.crawler.dao.HotelnfoServiceImpl;
import com.qudian.xiecheng.crawler.entity.po.HotelInfoDO;
import com.qudian.xiecheng.crawler.enums.CityEnum;
import com.qudian.xiecheng.crawler.enums.StarEnum;
import com.qudian.xiecheng.crawler.service.CrawlerService;
import com.qudian.xiecheng.crawler.service.Task;
import com.qudian.xiecheng.crawler.service.TaskQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;


/**
 * @author nijichang
 * @since 2020-10-30 18:57:43
 */
@Service
@Slf4j
public class FirstDepthCrawlerBiz {

    @Resource
    private CrawlerService firstDepthCrawlerServiceImpl;

    @Value("${crawler.cookie}")
    private String COOKIE;

    @Value("${crawler.uri}")
    private String URI;

    @Value("${retry.num}")
    private Integer num ;

    @Resource
    private HotelnfoServiceImpl hotelnfoService;

    private static StringBuffer URL_DOMAIN = new StringBuffer().append("https://hotels.ctrip.com");
    /**
     * 线程池逻辑
     */
    public void process(){
    }
    /**
     * 爬虫主要逻辑
     * 1.从queue中取任务进行爬取2.将后面的任务加入队列中，第二层任务入队列
     */
    public class FirstDepthCrawlerThread implements Callable<String>{
        @Override
        public String call(){
            Map<String,String> headers = getMap();
            Task task = TaskQueue.taskQueue.poll();
            String jsonResult = "";
            for(int i=0;i<num;i++) {
                log.info("正在执行第{}次爬取任务",i+1);
                try {
                    jsonResult = firstDepthCrawlerServiceImpl.crawl(URI, task.getParam(), headers, 2000);
                    break;
                }catch (Exception e){
                    e.printStackTrace();
                    log.info("爬虫链接超时，正在准备第{}次重试,当前参数: {}", (i + 1), task.getParam());
                    continue;
                }
            }
            /**
             * 只有初始化的url需要将翻页url加入队列
             */
            if(task.getTag() == 1){
                pageToQueue(jsonResult,task.getParam());
            }
            List<HotelInfoDO> infos = getHotelInfo(jsonResult,task.getParam());
            try {
                hotelnfoService.saveBatch(infos);
            }catch (Exception e){
                log.info("批量保存失败，失败原因{}",e.getMessage());
            }
            return null;
        }
    }

    private void pageToQueue(String jsonResult,String param){
        JSONObject object = (JSONObject)JSON.parse(jsonResult);
        String hotelAmount = object.getString("hotelAmount");
        if(StringUtils.isNotEmpty(hotelAmount)) {
            int pageNum = Integer.parseInt(hotelAmount) / 25 + 1;
            for(int i=2;i<=pageNum;i++){
               TaskQueue.addQueue(param + "&page=" + i);
            }
        }
    }

    public List<HotelInfoDO> getHotelInfo(String jsonResult,String param){
        List<HotelInfoDO> infos = new ArrayList<>();
        JSONObject object = (JSONObject)JSON.parse(jsonResult);
        JSONArray array = object.getJSONArray("hotelPositionJSON");
        if(array.size() != 0){
            for(int i=0;i<array.size();i++){
                HotelInfoDO hotelInfoDO = new HotelInfoDO();
                JSONObject entity = (JSONObject)array.get(i);
                hotelInfoDO.setHotelName(entity.getString("name"));
                hotelInfoDO.setCity(CityEnum.getByCode(ReUtil.getGroup0("(?<=cityId=)[0-9]*",param)).getName());
                hotelInfoDO.setAddress(entity.getString("address"));
                hotelInfoDO.setScore(entity.getString("score"));
                hotelInfoDO.setDpcount(entity.getString("dpscore"));
                hotelInfoDO.setShortName(entity.getString("shortName"));
                hotelInfoDO.setStar(StarEnum.getByCode(entity.getString("star")).getDesc());
                hotelInfoDO.setUrl(URL_DOMAIN.append(entity.getString("url")).toString());
                TaskQueue.addQueue2(URL_DOMAIN.append(entity.getString("url")).toString());
                infos.add(hotelInfoDO);
            }
        }
        return infos;
    }

    private Map<String,String> getMap(){
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
        headers.put("Accept","*/*");
        headers.put("Accept-Encoding","gzip, deflate, br");
        headers.put("Cookie",COOKIE);
        return headers;
    }

    public static void main(String[] args){
        String param = "StartTime=2020-12-12&DepTime=2020-12-13&cityId=32";

        String tag = ReUtil.getGroup0("(?<=cityId=)[0-9]*",param);

        System.out.println(tag);
    }
}
