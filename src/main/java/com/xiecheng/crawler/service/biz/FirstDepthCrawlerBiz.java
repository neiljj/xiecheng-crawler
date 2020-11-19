package com.xiecheng.crawler.service.biz;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiecheng.crawler.function.Consumer;
import com.xiecheng.crawler.service.core.service.impl.HotelInfoService;
import com.xiecheng.crawler.entity.po.HotelInfoDO;
import com.xiecheng.crawler.enums.CityEnum;
import com.xiecheng.crawler.enums.StarEnum;
import com.xiecheng.crawler.enums.TypeEnum;
import com.xiecheng.crawler.service.CrawlerService;
import com.xiecheng.crawler.entity.Task;
import com.xiecheng.crawler.service.core.TaskQueue;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.ThreadSafe;
import org.assertj.core.util.Lists;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;


/**
 * 一层采集逻辑
 * @author nijichang
 * @since 2020-10-30 18:57:43
 */
@Service
@Slf4j
public class FirstDepthCrawlerBiz extends AbstractCrawlerBiz{

    @Resource
    private CrawlerService firstDepthCrawlerServiceImpl;

    @Value("${crawler.uri}")
    private String uri ;

    @Resource
    private HotelInfoService hotelnfoService;

    private String urlDomain = "https://hotels.ctrip.com/hotels/detail/?hotelId=";

    private static BitMapBloomFilter bitMapBloomFilter = new BitMapBloomFilter(100);

    /**
     * 线程池逻辑
     */
    @Override
    public void process(){
        ExecutorService service = Executors.newFixedThreadPool(threadNum);
        int i = 0;
        while(!TaskQueue.taskQueue.isEmpty()){
            service.execute(new FirstDepthCrawlerThread());
            //队列只有一个任务时，需要等待将翻页加入队列
            if(i == 0){
                await();
                i++;
            }

            try {
                Thread.sleep(200);
            }catch (InterruptedException e){

            }

        }
        //线程池首先需要shutdown再判断isTerminated
        service.shutdown();
        while(true){
            if(service.isTerminated()){
                log.info("第一层任务采集完毕");
                break;
            }
        }
    }
    /**
     * 爬虫主要逻辑
     * 1.从queue中取任务进行爬取2.将后面的任务加入队列中，第二层任务入队列
     */
    @ThreadSafe
    public class FirstDepthCrawlerThread implements Runnable{
        @Override
        public void run(){
            Map<String, String> headers = getMap();
            Task task = TaskQueue.taskQueue.poll();
            log.info("当前第一层队列任务：{}", TaskQueue.taskQueue.size());

            log.info("参数{}正在执行", task.getParam());
            //异常处理由切面完成
            String jsonResult = firstDepthCrawlerServiceImpl.crawl(uri, task.getParam(), headers, 2000);
            if (StringUtils.isNotEmpty(jsonResult)) {
                //只有初始化的url需要将翻页url加入队列
                if (task.getDepthTag() == 0) {
                    taskToQueue(jsonResult, task.getParam(), task.getParamTag());
                }
                List<HotelInfoDO> infos = getHotelInfo(jsonResult, task.getParam(), task.getParamTag(), task.getDepthTag());
                try {
                    if (task.getParamTag() == 1) {
                        //参数为城市+类型
                        hotelnfoService.insertBrand(infos);
                    } else if (task.getParamTag() == 2) {
                        hotelnfoService.insertType(infos);
                    }else{
                        hotelnfoService.saveBatch(infos);
                    }
                    log.info("{}批量保存成功",task.getParam());
                } catch (Exception e) {
                    log.info("批量保存失败，失败信息{}", e.getMessage());
                }
            }
        }
    }

    private void taskToQueue(String jsonResult,String param,int tag){
        JSONObject object = (JSONObject)JSON.parse(jsonResult);
        String hotelAmount = object.getString("hotelAmount");
        if(StringUtils.isNotEmpty(hotelAmount)) {
            int pageNum = Integer.parseInt(hotelAmount) / 25 + 1;
            log.info("参数【{}】需要采集{}页",param,pageNum);
            for(int i=2;i<=pageNum;i++){
                Task task = new Task();
                task.setParam(param + "&page=" + i);
                task.setDepthTag(1);
                task.setParamTag(tag);
                TaskQueue.addQueue(task);
            }
        }
    }

    private List<HotelInfoDO> getHotelInfo(String jsonResult,String param,int paramTag,int depthTag){
        List<HotelInfoDO> infos = new ArrayList<>();
        JSONObject object = (JSONObject)JSON.parse(jsonResult);
        JSONArray array = object.getJSONArray("hotelPositionJSON");
        if(array.size() != 0){
            //获取价格map
            String hotelList = object.getString("hotelList");
            Map<String,String> priceMap = getPrice(hotelList);
            for(int i=0;i<array.size();i++){
                HotelInfoDO hotelInfoDO = new HotelInfoDO();
                JSONObject entity = (JSONObject)array.get(i);
                hotelInfoDO.setHotelName(entity.getString("name"));
                String cityId = ReUtil.getGroup0("(?<=cityId=)[0-9]*",param);
                TaskQueue.citys.forEach((k,v) -> {
                    if(v.equals(cityId))
                        hotelInfoDO.setCity(k);
                });
                hotelInfoDO.setAddress(entity.getString("address"));
                hotelInfoDO.setScore(entity.getString("score"));
                hotelInfoDO.setDpcount(entity.getString("dpscore"));
                hotelInfoDO.setShortName(entity.getString("shortName"));
                hotelInfoDO.setStar(StarEnum.getByCode(entity.getString("star")).map(StarEnum::getDesc).orElse(null));
                hotelInfoDO.setParam(param);
                String hotelId = ReUtil.getGroup0("(?<=\\/)([0-9]*)(?=.html)",entity.getString("url"));
                String url = urlDomain + hotelId;
                hotelInfoDO.setUrl(url);
                hotelInfoDO.setPrice(priceMap.get(entity.getString("name")));
//                //设置type 或brand属性,采用表驱动消除if-else,key存paramTag，depthTag
                if(paramTag != 0) {
                   setTypeAndBrand(hotelInfoDO,param,paramTag,depthTag);
                }
                //将url放入第二层采集队列，采用布隆表去重,需要将url后缀去掉
                if(!bitMapBloomFilter.contains(url)){
                    bitMapBloomFilter.add(url);
                    TaskQueue.addQueue2(url);
                }
                infos.add(hotelInfoDO);
            }
        }
        return infos;
    }

    public void setTypeAndBrand(HotelInfoDO hotelInfoDO,String param,int paramTag,int depthTag){
        Map<List<Integer>, Consumer> actionMap = new HashMap<>(6);
        actionMap.put(Lists.newArrayList(1, 0), () -> hotelInfoDO.setType(TypeEnum.getByCode(ReUtil.getGroup0("(?<=type=)(.*)", param)).map(TypeEnum::getDesc).orElse(null)));
        actionMap.put(Lists.newArrayList(1, 1), () -> hotelInfoDO.setType(TypeEnum.getByCode(ReUtil.getGroup0("(?<=type=)(.*)(?=&)", param)).map(TypeEnum::getDesc).orElse(null)));
        actionMap.put(Lists.newArrayList(2, 0), () ->
                TaskQueue.brands.forEach((k,v) -> {
                    if(v.equals(ReUtil.getGroup0("(?<=brand=)(.*)", param))){
                        hotelInfoDO.setBrand(k);
                    }
                })
        );
        actionMap.put(Lists.newArrayList(2, 1), () ->
                TaskQueue.brands.forEach((k, v) -> {
                    if (v.equals(ReUtil.getGroup0("(?<=brand=)(.*)", param))) {
                        hotelInfoDO.setBrand(k);
                    }
                })
        );
        actionMap.put(Lists.newArrayList(3, 0), () -> {
            TaskQueue.brands.forEach((k, v) -> {
                if (v.equals(ReUtil.getGroup0("(?<=brand=)(.*)", param))) {
                    hotelInfoDO.setBrand(k);
                }
            });
            hotelInfoDO.setType(TypeEnum.getByCode(ReUtil.getGroup0("(?<=type=)(.*?)(?=&)", param)).map(TypeEnum::getDesc).orElse(null));
        });
        actionMap.put(Lists.newArrayList(3, 1), () -> {
            TaskQueue.brands.forEach((k, v) -> {
                if (v.equals(ReUtil.getGroup0("(?<=brand=)(.*)", param))) {
                    hotelInfoDO.setBrand(k);
                }
            });
            hotelInfoDO.setType(TypeEnum.getByCode(ReUtil.getGroup0("(?<=type=)(.*?)(?=&)", param)).map(TypeEnum::getDesc).orElse(null));
        });
        actionMap.get(Lists.newArrayList(paramTag,depthTag)).apply();
    }

    private Map<String,String> getMap(){
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
        headers.put("Accept","*/*");
        headers.put("Accept-Encoding","gzip, deflate, br");
        headers.put("Cookie",getCookie());
        return headers;
    }

    private Map<String,String> getPrice(String hotelList){
        //price 字段需要从hotelList中解析
        Document document = Jsoup.parse(hotelList);
        Map<String,String> priceMap = new LinkedHashMap<>();
        Elements hotels = document.select("ul[class=hotel_item]");
        if(hotels.size() != 0){
            for(Element hotel : hotels){
                String hotelName = hotel.select("li[class=hotel_item_name]").select("a").attr("title");
                String price = hotel.select("li[class=hotel_price_icon]").select("span[class=J_price_lowList]").text();
                priceMap.put(hotelName,price);
            }
        }
        return priceMap;
    }
}
