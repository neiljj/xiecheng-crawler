package com.xiecheng.crawler.service.biz;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiecheng.crawler.service.HotelnfoService;
import com.xiecheng.crawler.entity.po.HotelInfoDO;
import com.xiecheng.crawler.enums.CityEnum;
import com.xiecheng.crawler.enums.StarEnum;
import com.xiecheng.crawler.enums.TypeEnum;
import com.xiecheng.crawler.service.CrawlerService;
import com.xiecheng.crawler.entity.Task;
import com.xiecheng.crawler.service.TaskQueue;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


/**
 * @author nijichang
 * @since 2020-10-30 18:57:43
 */
@Service
@Slf4j
public class FirstDepthCrawlerBiz extends AbstractCrawlerBiz{

    @Resource
    private CrawlerService firstDepthCrawlerServiceImpl;

    @Value("${crawler.uri}")
    private String uri;

    @Resource
    private HotelnfoService hotelnfoService;

    private String urlDomain = "https://hotels.ctrip.com";

    private static BitMapBloomFilter bitMapBloomFilter = new BitMapBloomFilter(100);

    /**
     * 线程池逻辑
     */
    @Override
    public void process(){

        ExecutorService service = new ThreadPoolExecutor(threadNum,Runtime.getRuntime().availableProcessors()*2,
                5, TimeUnit.SECONDS,new LinkedBlockingQueue<>(),new ThreadPoolExecutor.CallerRunsPolicy());
        List<Future> futures = new ArrayList<>();
        String task = "";
        while(!TaskQueue.taskQueue.isEmpty()){
            task = TaskQueue.taskQueue.peek().toString();
            Future<String> future = service.submit(new FirstDepthCrawlerThread(task));
            futures.add(future);
            await();
        }
        while(true){
            if(service.isTerminated()){
                log.info("第一层任务采集完毕");
                break;
            }
        }
        service.shutdown();
    }
    /**
     * 爬虫主要逻辑
     * 1.从queue中取任务进行爬取2.将后面的任务加入队列中，第二层任务入队列
     */
    @ThreadSafe
    public class FirstDepthCrawlerThread implements Callable<String>{

        String threadName;

        private FirstDepthCrawlerThread(String tname){this.threadName = tname;}

        @Override
        public String call(){
            Map<String,String> headers = getMap();
            Task task = TaskQueue.taskQueue.poll();
            log.info("当前第一层队列任务：{}",TaskQueue.taskQueue.size());
            String jsonResult = "";
            for(int i=0;i<retryNum;i++) {
                log.info("参数{}正在执行第{}次爬取任务",task.getParam(),i+1);
                try {
                    jsonResult = firstDepthCrawlerServiceImpl.crawl(uri, task.getParam(), headers, 2000);
                    taskNum.incrementAndGet();
                    break;
                }catch (Exception e){
                    e.printStackTrace();
                    log.info("爬虫链接超时，正在准备第{}次重试,当前参数: {}", (i + 1), task.getParam());
                }
            }
            //只有初始化的url需要将翻页url加入队列
            if(task.getDepthTag() == 0){
                taskToQueue(jsonResult,task.getParam(),task.getParamTag());
            }
            List<HotelInfoDO> infos = getHotelInfo(jsonResult,task.getParam(),task.getParamTag(),task.getDepthTag());
            try {
                if (task.getParamTag() == 1) {
                    //参数为城市+类型
                    hotelnfoService.insertBrand(infos);
                }else if(task.getParamTag() == 2){
                    hotelnfoService.insertType(infos);
                }
            }catch (Exception e){
                log.info("批量保存失败，失败信息{}",e.getMessage());
            }
            return null;
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
                hotelInfoDO.setCity(CityEnum.getByCode(ReUtil.getGroup0("(?<=cityId=)[0-9]*",param)).map(CityEnum::getName).orElse(null));
                hotelInfoDO.setAddress(entity.getString("address"));
                hotelInfoDO.setScore(entity.getString("score"));
                hotelInfoDO.setDpcount(entity.getString("dpscore"));
                hotelInfoDO.setShortName(entity.getString("shortName"));
                hotelInfoDO.setStar(StarEnum.getByCode(entity.getString("star")).map(StarEnum::getDesc).orElse(null));
                hotelInfoDO.setParam(param);
                String url = urlDomain + entity.getString("url");
                hotelInfoDO.setUrl(url);
                hotelInfoDO.setPrice(priceMap.get(entity.getString("name")));
                //设置type 或brand属性,采用表驱动消除if-else,key存paramTag，depthTag
                Map<List<Integer>,Consumer> actionMap = new HashMap<>(4);
                actionMap.put(Lists.newArrayList(1,0),t -> hotelInfoDO.setType(TypeEnum.getByCode(ReUtil.getGroup0("(?<=type=)(.*)",param)).map(TypeEnum::getDesc).orElse(null)));
                actionMap.put(Lists.newArrayList(1,1),t -> hotelInfoDO.setType(TypeEnum.getByCode(ReUtil.getGroup0("(?<=type=)(.*)(?=&)",param)).map(TypeEnum::getDesc).orElse(null)));
                actionMap.put(Lists.newArrayList(2,0),t -> hotelInfoDO.setBrand(TaskQueue.brands.get(ReUtil.getGroup0("(?<=brand=)(.*)",param))));
                actionMap.put(Lists.newArrayList(2,1),t -> hotelInfoDO.setBrand(TaskQueue.brands.get(ReUtil.getGroup0("(?<=brand=)(.*)(?=&)",param))));
                actionMap.get(Lists.newArrayList(paramTag,depthTag)).accept(1);
                //将url放入第二层采集队列，采用布隆表去重,需要将url后缀去掉
                String urlFilter = ReUtil.getGroup0("(.*)(?=\\?)",url);
                if(!bitMapBloomFilter.contains(urlFilter)){
                    bitMapBloomFilter.add(urlFilter);
                    TaskQueue.addQueue2(urlFilter);
                }
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
        headers.put("Cookie",cookie);
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
    public static void main(String[] args){
        String url = "https://hotels.ctrip.com/hotel/41796021.html?isFull=F#ctm_ref=hod_sr_map_dl_txt_1";

        System.out.println(ReUtil.getGroup0("(.*)(?=\\?)",url));
    }
}
