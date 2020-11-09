package com.xiecheng.crawler.service.biz;

import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiecheng.crawler.entity.RoomInfo;
import com.xiecheng.crawler.entity.Task;
import com.xiecheng.crawler.entity.po.DetailInfoDO;
import com.xiecheng.crawler.service.CrawlerService;
import com.xiecheng.crawler.service.DetailInfoService;
import com.xiecheng.crawler.service.TaskQueue;
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
        ExecutorService service = new ThreadPoolExecutor(threadNum, Runtime.getRuntime().availableProcessors() * 2,
                5, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
        List<Future> futures = new ArrayList<>();

        //todo 这里会有bug
        while (!TaskQueue.task2Queue.isEmpty()) {
            Future future = service.submit(new SecondDepthCrawlerThread());
            futures.add(future);
            await();
            //
            if(service.isTerminated()){
                log.info("第二层任务采集完毕");
                break;
            }
        }
    }

    @ThreadSafe
    public class SecondDepthCrawlerThread implements Callable<String> {

        @Override
        public String call(){
            Task task = TaskQueue.task2Queue.poll();
            log.info("当前第二层队列任务：{}",TaskQueue.task2Queue.size());
            String resultHtml = "";
            DetailInfoDO detailInfoDO = new DetailInfoDO();
            String hotelId = ReUtil.getGroup0("([0-9]*)(?=.html)",task.getParam());
            for(int i=0;i<retryNum;i++) {
                log.info("二层url：{}正在执行第{}次爬取任务",task.getParam(),i+1);
                try {
                    resultHtml = secondDepthCrawlerServiceImpl.crawl(task.getParam(),task.getParam(), null, 2000);
                    taskNum.incrementAndGet();
                    break;
                }catch (Exception e){
                    log.info("爬虫链接超时，正在准备第{}次重试,当前二层url: {}", (i + 1), task.getParam());
                }
            }
            if(StringUtils.isNotEmpty(resultHtml)) {
                setDetailInfo(detailInfoDO, resultHtml);
            }
            detailInfoDO.setHotelId(hotelId);
            detailInfoDO.setUrl(task.getParam());
            //获取房间信息
            detailInfoDO.setRoomInfo(getRoomInfo(hotelId));
            //数据库插入
            try{
                detailInfoService.save(detailInfoDO);
            }catch (Exception e){
                log.info("酒店详情保存失败，酒店id:{},失败信息:{}",hotelId,e.getMessage());
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
        String jsonResult = "";
        for(int i=0;i<retryNum;i++) {
            log.info("酒店id{}正在执行第{}次爬取房间信息任务",hotelId,i+1);
            try {
                jsonResult = HttpUtils.doGet(roomUrl + "?hotel=" + hotelId,headers,2000);
                break;
            }catch (Exception e){
                e.printStackTrace();
                log.info("爬虫链接超时，正在准备第{}次重试,当前二层url酒店id: {}", (i + 1),hotelId);
            }
        }
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
        headers.put("Cookie",cookie);
        headers.put("Referer","https://hotels.ctrip.com/hotel/" + hotelId + ".html");
        return headers;
    }
    public static void main(String[] args){
//        String url = "https://hotels.ctrip.com/Domestic/tool/AjaxHote1RoomListForDetai1.aspx?hotel=5348362";
//        Map<String,String> headers = new HashMap<>();
//        headers.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
//        headers.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
//        headers.put("Accept","*/*");
//        headers.put("Accept-Encoding","gzip, deflate, br");
//        headers.put("Cookie","magicid=w2r1pxBGGKhsJqIqz22NTTl6Wc9YqV5CgpvwkjuPa4zBaLSQv4yIN4/TI76Mhhde; _RSG=kKK7SMJld.71sxn40MscoB; _RDG=2810e708e068d82f0b2cda39d71d2073a6; _RGUID=7fa7bd35-9c1d-452e-bca6-d96004c7bd81; MKT_CKID=1604022007946.fhu0j.cusc; MKT_CKID_LMT=1604022007948; _ga=GA1.2.2123543332.1604022008; _gid=GA1.2.335966543.1604022008; MKT_Pagesource=PC; HotelCityID=223split%E4%B8%9C%E8%8E%9EsplitDongguansplit2020-12-12split2020-12-13split0; ASP.NET_SessionId=oluq2kx1hswkrnctsgnfsn5l; OID_ForOnlineHotel=16040219998642i084u1604022038355102032; StartCity_Pkg=PkgStartCity=25; librauuid=mcqFVNMGqqm32AT0; Session=SmartLinkCode=U155952&SmartLinkKeyWord=&SmartLinkQuary=&SmartLinkHost=&SmartLinkLanguage=zh; Union=OUID=index&AllianceID=4897&SID=155952&SourceID=&createtime=1604043536&Expires=1635586806335; MKT_OrderClick=ASID=4897155952&AID=4897&CSID=155952&OUID=index&CT=1604043536346&CURL=https%3A%2F%2Fwww.ctrip.com%2F%3Fsid%3D155952%26allianceid%3D4897%26ouid%3Dindex&VAL={\"pc_vid\":\"1604021999864.2i084u\"}; HotelDomesticVisitedHotels1=429531=0,0,4.5,2228,/20031900000166atc8EC9.jpg,&1501200=0,0,4.4,540,/20080k000000c88egC06A.jpg,; _RF1=218.107.211.85; appFloatCnt=36; login_uid=6711FCDDFF228BE264990242CE39227C; login_type=0; cticket=CD73B1D5504C120F982DFC50DF1B0837293ED5A68736CE889794C6377A4191A1; AHeadUserInfo=VipGrade=0&VipGradeName=%C6%D5%CD%A8%BB%E1%D4%B1&UserName=&NoReadMessageCount=0; ticket_ctrip=bJ9RlCHVwlu1ZjyusRi+ypZ7X2r4+yojxZ2jOh6q1sqKMDsFxCZLMUvI/ig54yj1llyvd2reZrD88ThjDzMTiHrPwxLBeKxleFf47qmTKHFhXbkGo5NBBN+qVkHNTWlONjczr57f5O+Y9mNg9nUPWALOmLioxlf/QKNUebAyeIworTZSzWrAeNooVLp1cBpS/eecsCipFlKQqH7OK7OQHw55ELJ6K15IMvTuFJCn4B/JQCZVKrAflu6W+DgwLqSrRMoAidhQ4UcUz4tDbL/IDEN0YEPmi9yRa29qzcg/DAg=; DUID=u=6711FCDDFF228BE264990242CE39227C&v=0; IsNonUser=F; UUID=260D7E706CDF421EBE3537202C650173; IsPersonalizedLogin=F; _bfi=p1%3D102002%26p2%3D102002%26v1%3D56%26v2%3D55; _gat=1; _jzqco=%7C%7C%7C%7C1604022008080%7C1.1942924796.1604022007938.1604047050858.1604048921095.1604047050858.1604048921095.undefined.0.0.42.42; __zpspc=9.7.1604048921.1604048921.1%232%7Cwww.baidu.com%7C%7C%7C%25E6%2590%25BA%25E7%25A8%258B%7C%23; _bfa=1.1604021999864.2i084u.1.1604045876232.1604048917933.6.57; _bfs=1.2; hotelhst=1164390341");
//        headers.put("Referer","https://hotels.ctrip.com/hotel/" + 5348362 + ".html");
//        String jsonResult = HttpUtils.doGet(url,headers,2000);
//        JSONObject object = JSONObject.parseObject(jsonResult);
//        String html = object.getString("html");
//        Document document = Jsoup.parse(html);
//        Elements rooms = document.select("tr");
//        List<RoomInfo> roomInfoList= new ArrayList<>();
//        for(Element room : rooms){
//            if(room.select("td[class*=room_type]").size() != 0){
//                RoomInfo roomInfo = new RoomInfo();
//                roomInfo.setName(room.select("td").get(0).select("a[class*=room_unfold]").text().split(" ")[0]);
//                roomInfo.setBedType(room.select("td").get(2).text());
//                roomInfo.setBreakfast(room.select("td").get(3).text());
//                roomInfo.setFacility(room.select("td").get(4).text());
//                roomInfo.setGuestNum(room.select("td[class=col_person]").select("span").attr("title"));
//                roomInfo.setPolicy(room.select("td[class=col_policy]").text());
//                roomInfo.setPrice(room.select("td").get(7).select("span[class=base_txtdiv]").text());
//                roomInfoList.add(roomInfo);
//            }else{
//                continue;
//            }
//        }
//        System.out.println(JSONUtil.toJsonStr(roomInfoList));
    }
}
