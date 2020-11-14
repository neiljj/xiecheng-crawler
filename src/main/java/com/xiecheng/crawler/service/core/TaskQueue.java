package com.xiecheng.crawler.service.core;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiecheng.crawler.constant.MessageConstant;
import com.xiecheng.crawler.entity.Task;
import com.xiecheng.crawler.entity.po.BrandDO;
import com.xiecheng.crawler.entity.po.CityDO;
import com.xiecheng.crawler.entity.po.CrawlerTaskDO;
import com.xiecheng.crawler.entity.po.HotelInfoDO;
import com.xiecheng.crawler.enums.TypeEnum;
import com.xiecheng.crawler.function.Consumer;
import com.xiecheng.crawler.service.core.service.impl.BrandService;
import com.xiecheng.crawler.service.core.service.impl.CityService;
import com.xiecheng.crawler.service.core.service.impl.CrawlerTaskService;
import com.xiecheng.crawler.service.core.service.impl.HotelInfoService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
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

    @Resource
    private CrawlerTaskService crawlerTaskService;

    @Resource
    private HotelInfoService hotelInfoService;

    @Resource
    private BrandService brandService;
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
     * 城市信息
     */
    public static Map<String,String> citys = new LinkedHashMap<>();

    @Scheduled(cron = "${crawler.cron}")
    public void saveTaskToQueue(){
        //查询所有待采集任务入队列
        log.info("定时读取待采集任务");
        Wrapper<CrawlerTaskDO> wrapper = new QueryWrapper<CrawlerTaskDO>().eq("status",0);
        List<CrawlerTaskDO> tasks = crawlerTaskService.list(wrapper);
        tasks.forEach(t -> {
            Assert.isTrue(StringUtils.isNotEmpty(t.getCity()), MessageConstant.CITY_NOT_EMPTY);
            //根据city查询city code
            String cityCode = citys.get(t.getCity());
            String param = "cityId=" + cityCode;
            if(StringUtils.isNotEmpty(t.getType())){
                param += "&type=" + TypeEnum.getByName(t.getType()).map(TypeEnum::getCode).orElse(null);
            }
            if(StringUtils.isNotEmpty(t.getBrand())){
                param += "&brand=" + brands.get(t.getBrand());
            }

            Task task = new Task();
            task.setParam(param);
            task.setDepthTag(0);

            //参数类型改为匹配
            Map<List<Boolean>, Consumer> actionMap = new HashMap<>(4);
            actionMap.put(Lists.newArrayList(true,true), () -> task.setParamTag(3));
            actionMap.put(Lists.newArrayList(true,false),() -> task.setParamTag(2));
            actionMap.put(Lists.newArrayList(false,true),() -> task.setParamTag(1));
            actionMap.put(Lists.newArrayList(false,false),() -> task.setParamTag(0));
            actionMap.get(Lists.newArrayList(StringUtils.isNotEmpty(t.getBrand()),StringUtils.isNotEmpty(t.getType()))).apply();
            try {
                taskQueue.put(task);
            } catch (InterruptedException e) {
                log.error("添加队列发生错误{}", e.getMessage());
            }
            //更改任务状态
            t.setStatus(1);
        });
        if(CollectionUtils.isNotEmpty(tasks)) {
            crawlerTaskService.updateBatchById(tasks, 20);
        }
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


    @PostConstruct
    private void getCityAndBrand(){
        List<CityDO> cityList = cityService.list();
        cityList.forEach(t -> citys.put(t.getName(),t.getCode()));

        List<BrandDO> brandList = brandService.list();
        brandList.forEach(t -> brands.put(t.getName(),t.getCode()));
    }

    /**
     *
     * 城市-城市code 持久化
     */
    public void saveCity(){
        String html = HttpUtil.get("https://hotels.ctrip.com/Domestic/Tool/AjaxGetCitySuggestion.aspx");
        List<String> citys = ReUtil.findAll("(?<=\\{)(.*?)(?=\\})",html,1);
        List<CityDO> cityList = new ArrayList<>();
        citys.forEach(t -> {
            String city = ReUtil.getGroup0("(?<=display:\")(.*?)(?=\")",t);
            String cityId = ReUtil.getGroup0("[0-9]*(?=\",group)",t);
            if(StringUtils.isNotEmpty(city)){
                CityDO cityDO = new CityDO();
                cityDO.setCode(cityId);
                cityDO.setName(city);
                cityList.add(cityDO);
            }
        });
        cityService.saveBatch(cityList);
    }

    /**
     * 品牌信息持久化
     * 保存成map
     * @return Map<String,String>
     */
    public void saveBrand(){
        List<HotelInfoDO> hotelInfoDOList = hotelInfoService.list();
        BitMapBloomFilter bitMapBloomFilter = new BitMapBloomFilter(10);
        List<BrandDO> brands = new ArrayList<>();
        hotelInfoDOList.forEach(t -> {
            if(StringUtils.isNotEmpty(t.getBrand()) && !t.getParam().contains("page")) {
                String brand = t.getBrand();
                String brandCode = ReUtil.getGroup0("(?<=brand=)(.*)",t.getParam());
                if(!bitMapBloomFilter.contains(brandCode)){
                    BrandDO brandDO = new BrandDO();
                    brandDO.setName(brand);
                    brandDO.setCode(brandCode);
                    brands.add(brandDO);
                    bitMapBloomFilter.add(brandCode);
                }
            }
        });
        brandService.saveBatch(brands);
    }
    /**
     *初始化对列
     * 初始队列中包含两种参数：1.城市+类型；2.城市+品牌
     */
//    @PostConstruct
//    public void initQueue(){
//        List<String> cityCodes = CityEnum.toList();
//        List<String> typeCodes = TypeEnum.toList();
//        Set<String> brandCodes = getBrandMap().keySet();
//
//        cityCodes.forEach(city -> {
//            //1.城市+类型
//            typeCodes.forEach(type -> {
//                Task task = new Task();
//                task.setParamTag(1);
//                task.setDepthTag(0);
//                task.setParam(PARAM_STRING + city + "&type=" + type);
//                try {
//                    taskQueue.put(task);
//                }catch (InterruptedException e){
//                    log.error("添加队列发生错误{}",e.getMessage());
//                }
//            });
//            //1.城市+品牌
//            brandCodes.forEach(brand -> {
//                Task task = new Task();
//                task.setParamTag(2);
//                task.setDepthTag(0);
//                task.setParam(PARAM_STRING + city + "&brand=" + brand);
//                try {
//                    taskQueue.put(task);
//                }catch (InterruptedException e){
//                    log.error("添加队列发生错误{}",e.getMessage());
//                }
//            });
//
//        });
//    }
}
