package com.xiecheng.crawler.service;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import com.xiecheng.crawler.entity.Task;
import com.xiecheng.crawler.enums.CityEnum;
import com.xiecheng.crawler.enums.TypeEnum;
import com.xiecheng.crawler.service.core.CityService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author nijichang
 * @since 2020-11-02 11:36:31
 */
@Slf4j
@Component
public class TaskQueue {

    @Resource
    private CityService cityService;
    /**
     * 第一层采集队列
     */
    public static BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

    /**
     * 第二层采集队列
     */
    public static BlockingQueue<Task> task2Queue = new LinkedBlockingQueue<>();

    /**
     * 品牌信息map
     */
    public static Map<String,String> brands = new LinkedHashMap<>();

    /**
     * 请求参数，城市为必填项
     */
    private static String PARAM_STRING = "cityId=";

    private final String URL = "https://hotels.ctrip.com/hotel/beijing1#ctm_ref=hod_hp_sb_lst";


    /**
     *初始化对列
     * 初始队列中包含两种参数：1.城市+类型；2.城市+品牌
     */
    @PostConstruct
    public void initQueue(){
        List<String> cityCodes = CityEnum.toList();
        List<String> typeCodes = TypeEnum.toList();
        Set<String> brandCodes = getBrandMap().keySet();

        cityCodes.forEach(city -> {
            //1.城市+类型
            typeCodes.forEach(type -> {
                Task task = new Task();
                task.setParamTag(1);
                task.setDepthTag(0);
                task.setParam(PARAM_STRING + city + "&type=" + type);
                try {
                    taskQueue.put(task);
                }catch (InterruptedException e){
                    log.error("添加队列发生错误{}",e.getMessage());
                }
            });
            //1.城市+品牌
            brandCodes.forEach(brand -> {
                Task task = new Task();
                task.setParamTag(2);
                task.setDepthTag(0);
                task.setParam(PARAM_STRING + city + "&brand=" + brand);
                try {
                    taskQueue.put(task);
                }catch (InterruptedException e){
                    log.error("添加队列发生错误{}",e.getMessage());
                }
            });

        });
    }



    public static void addQueue(Task task){
        try {
            taskQueue.put(task);
        }catch (InterruptedException e){
            log.error("添加队列发生错误{}",e.getMessage());
        }
    }

    public static void addQueue2(String url){
        Task task = new Task();
        task.setParam(url);
        task.setDepthTag(2);
        try {
            task2Queue.put(task);
        }catch (InterruptedException e){
            log.error("添加二层队列发生错误{}",e.getMessage());
        }
    }

    /**
     * 品牌信息太多需要自动获取，
     * 保存成map
     * @return Map<String,String>
     */
    private Map<String,String> getBrandMap(){
        try {
            String html = HttpUtil.get(URL);
            Document document = Jsoup.parse(html);
            Elements divs = document.select("div[id=J_BrandFilterList]").select("div[class=optionList-item  ]");
            if(divs.size() > 0){
                for(Element div : divs){
                    if("0".equals(div.attr("data-value")) || "8".equals(div.attr("data-value")) || "1".equals(div.attr("data-value")))continue;
                    brands.put(div.attr("data-value"),div.attr("title"));
                }
            }
        }catch (Exception e) {
            log.info("获取品牌信息错误:{}", e.getMessage());
        }
        return brands;
    }

    public void saveCity(){
        String html = HttpUtil.get("https://hotels.ctrip.com/Domestic/Tool/AjaxGetCitySuggestion.aspx");
        List<String> citys = ReUtil.findAll("(?<=\\{)(.*?)(?=\\})",html,1);

    }
    public static void main(String[] args){
        String html = HttpUtil.get("https://hotels.ctrip.com/Domestic/Tool/AjaxGetCitySuggestion.aspx");
        List<String> citys = ReUtil.findAll("(?<=\\{)(.*?)(?=\\})",html,1);
        citys.forEach(t -> {
            String city = ReUtil.getGroup0("(?<=display:\")(.*?)(?=\")",t);
            String cityId = ReUtil.getGroup0("[0-9]*(?=\",group)",t);
            System.out.println(city + " "+cityId);

        });

    }

}
