package com.xiecheng.crawler.service.biz;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiecheng.crawler.entity.Task;
import com.xiecheng.crawler.entity.po.DetailInfoDO;
import com.xiecheng.crawler.entity.po.HotelInfoDO;
import com.xiecheng.crawler.service.CrawlerService;
import com.xiecheng.crawler.service.core.DetailInfoService;
import com.xiecheng.crawler.service.core.HotelnfoService;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

/**
 * 二层采集存在bug，改成直接从数据库读取url访问
 * @author nijichang
 * @since 2020-11-08 14:19:53
 */
@Slf4j
@Service
public class SaveDetailBiz extends AbstractCrawlerBiz{

    public static BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

    private static BitMapBloomFilter bitMapBloomFilter = new BitMapBloomFilter(100);

    @Resource
    private HotelnfoService hotelnfoService;

    @Resource
    private CrawlerService secondDepthCrawlerServiceImpl;

    @Resource
    private SecondDepthCrawlerBiz secondDepthCrawlerBiz;

    @Resource
    private DetailInfoService detailInfoService;

    @Override
    public void process(){
        saveTask2Queue();
        ExecutorService service = new ThreadPoolExecutor(threadNum,Runtime.getRuntime().availableProcessors()*2,
                5, TimeUnit.SECONDS,new LinkedBlockingQueue<>(),new ThreadPoolExecutor.CallerRunsPolicy());
        while(!taskQueue.isEmpty()){
            service.submit(new SaveDetailCrawlerThread());
        }
        service.shutdown();
        while(true){
            if(service.isTerminated()){
                log.info("任务采集完毕");
                break;
            }
        }
    }

    public void saveTask2Queue(){
        Wrapper<HotelInfoDO> wrapper = new QueryWrapper<HotelInfoDO>()
                .le("create_time","2020-11-06 17:05:07")
                .orderByDesc("create_time");
        List<HotelInfoDO> hotelInfos = hotelnfoService.list(wrapper);
        hotelInfos.forEach(t -> {
            String url = ReUtil.getGroup0("(.*)(?=\\?)",t.getUrl());
            Task task = new Task();
            task.setParam(url);
            task.setDepthTag(2);
            try {
                if(!bitMapBloomFilter.contains(url)) {
                    bitMapBloomFilter.add(url);
                    taskQueue.put(task);
                }
            }catch (InterruptedException e){
                log.info("添加队列发生错误，错误信息{}",e.getMessage());
            }
        });
        log.info("队列初始化结束");
    }
    @ThreadSafe
    public class SaveDetailCrawlerThread implements Callable<String> {

        @Override
        public String call(){
            Task task = taskQueue.poll();
            log.info("当前队列任务：{}", taskQueue.size());
            DetailInfoDO detailInfoDO = new DetailInfoDO();
            String hotelId = ReUtil.getGroup0("([0-9]*)(?=.html)", task.getParam());
            log.info("url：{}正在执行", task.getParam());

            String resultHtml = secondDepthCrawlerServiceImpl.crawl(task.getParam(), task.getParam(), null, 2000);
            if (StringUtils.isNotEmpty(resultHtml)) {
                secondDepthCrawlerBiz.setDetailInfo(detailInfoDO, resultHtml);
            }
            detailInfoDO.setHotelId(hotelId);
            detailInfoDO.setUrl(task.getParam());
            //获取房间信息
            detailInfoDO.setRoomInfo(secondDepthCrawlerBiz.getRoomInfo(hotelId));
            //数据库插入
            try {
                detailInfoService.save(detailInfoDO);
            } catch (Exception e) {
                log.info("酒店详情保存失败，酒店id:{},失败信息:{}", hotelId, e.getMessage());
            }
            return null;
        }
    }
}
