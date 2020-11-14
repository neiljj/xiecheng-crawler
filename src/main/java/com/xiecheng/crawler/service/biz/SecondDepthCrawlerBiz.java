package com.xiecheng.crawler.service.biz;

import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiecheng.crawler.entity.RoomInfo;
import com.xiecheng.crawler.entity.Task;
import com.xiecheng.crawler.entity.po.DetailInfoDO;
import com.xiecheng.crawler.service.CrawlerService;
import com.xiecheng.crawler.service.core.service.impl.DetailInfoService;
import com.xiecheng.crawler.service.core.TaskQueue;
import com.xiecheng.crawler.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.ThreadSafe;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 二层采集逻辑
 * @author nijichang
 * @since 2020-11-05 14:38:06
 */
@Service
@Slf4j
public class SecondDepthCrawlerBiz extends AbstractCrawlerBiz{

    @Resource
    private CrawlerService secondDepthCrawlerServiceImpl;

    @Resource
    private DetailInfoService detailInfoService;

    private String roomUrl = "https://hotels.ctrip.com/Domestic/tool/AjaxHote1RoomListForDetai1.aspx";

    @Override
    public void process() {
        ExecutorService service = new ThreadPoolExecutor(threadNum,Runtime.getRuntime().availableProcessors()*2,
                5, TimeUnit.SECONDS,new LinkedBlockingQueue<>(),new ThreadPoolExecutor.CallerRunsPolicy());
        List<Future> futures = new ArrayList<>();
        while (!TaskQueue.task2Queue.isEmpty()) {
            Future future = service.submit(new SecondDepthCrawlerThread());
            futures.add(future);
        }
        service.shutdown();
        while(true){
            if(service.isTerminated()){
                log.info("第二层任务采集完毕");
                break;
            }
        }
    }

    @ThreadSafe
    public class SecondDepthCrawlerThread implements Callable<String> {

        @Override
        public String call() {
            Task task = TaskQueue.task2Queue.poll();
            log.info("当前第二层队列任务：{}", TaskQueue.task2Queue.size());

            DetailInfoDO detailInfoDO = new DetailInfoDO();
            String hotelId = ReUtil.getGroup0("([0-9]*)(?=.html)", task.getParam());

            log.info("二层url：{}正在执行", task.getParam());
            String resultHtml = secondDepthCrawlerServiceImpl.crawl(task.getParam(), task.getParam(), null, 2000);
            taskNum.incrementAndGet();

            if (StringUtils.isNotEmpty(resultHtml)) {
                setDetailInfo(detailInfoDO, resultHtml);
            }
            detailInfoDO.setHotelId(hotelId);
            detailInfoDO.setUrl(task.getParam());
            //获取房间信息
            detailInfoDO.setRoomInfo(getRoomInfo(hotelId));
            //数据库插入
            try {
                detailInfoService.save(detailInfoDO);
            } catch (Exception e) {
                log.info("酒店详情保存失败，酒店id:{},失败信息:{}", hotelId, e.getMessage());
            }
            return null;
        }
    }

    public void setDetailInfo(DetailInfoDO detailInfoDO,String resultHtml){
        detailInfoDO.setOpenTime(ReUtil.getGroup0("([0-9]*)(?=年开业)",resultHtml));
        detailInfoDO.setDecorateTime(ReUtil.getGroup0("([0-9]*)(?=年装修)",resultHtml));
        detailInfoDO.setRoomNum(ReUtil.getGroup0("([0-9]*)(?=间房)",resultHtml));
        Document document = Jsoup.parse(resultHtml);
        detailInfoDO.setName(document.select("h2[class=cn_n]").text());
    }

    public String getRoomInfo(String hotelId){
        Map<String,String> headers = getMap(hotelId);
        log.info("酒店id{}正在执行",hotelId);
        String jsonResult = HttpUtils.doGet(roomUrl + "?hotel=" + hotelId,headers,2000);
        if(StringUtils.isNotEmpty(jsonResult)){
            JSONObject object = JSONObject.parseObject(jsonResult);
            String html = object.getString("html");
            List<RoomInfo> roomInfos = parseDom(html);
            return JSONUtil.toJsonStr(roomInfos);
        }else {
            return null;
        }

    }

    public List<RoomInfo> parseDom(String html){
        List<RoomInfo> roomInfos = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements rooms = document.select("tr");
        try {
            for (Element room : rooms) {
                if (room.select("td[class*=room_type]").size() != 0) {
                    RoomInfo roomInfo = new RoomInfo();
                    roomInfo.setName(room.select("td").get(0).select("a[class*=room_unfold]").text().split(" ")[0]);
                    roomInfo.setBedType(room.select("td").get(2).text());
                    roomInfo.setBreakfast(room.select("td").get(3).text());
                    roomInfo.setFacility(room.select("td").get(4).text());
                    roomInfo.setGuestNum(room.select("td[class=col_person]").select("span").attr("title"));
                    roomInfo.setPolicy(room.select("td[class=col_policy]").text());
                    roomInfo.setPrice(room.select("td").get(7).select("span[class=base_txtdiv]").text());
                    roomInfos.add(roomInfo);
                } else {
                    continue;
                }
            }
        }catch (Exception e){
            log.info("dom解析失败，失败信息:{}",e.getMessage());
        }
        return roomInfos;
    }
    public Map<String,String> getMap(String hotelId){
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
        headers.put("Accept","*/*");
        headers.put("Accept-Encoding","gzip, deflate, br");
        headers.put("Cookie",getCookie());
        headers.put("Referer","https://hotels.ctrip.com/hotel/" + hotelId + ".html");
        return headers;
    }
}
