package com.xiecheng.crawler.service.biz;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiecheng.crawler.entity.Task;
import com.xiecheng.crawler.entity.po.DetailInfoDO;
import com.xiecheng.crawler.entity.po.HotelInfoDO;
import com.xiecheng.crawler.service.CrawlerService;
import com.xiecheng.crawler.service.DetailInfoService;
import com.xiecheng.crawler.service.HotelnfoService;
import com.xiecheng.crawler.service.TaskQueue;
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
        ExecutorService service = new ThreadPoolExecutor(threadNum*2,Runtime.getRuntime().availableProcessors()*4,
                5, TimeUnit.SECONDS,new LinkedBlockingQueue<>(100000),new ThreadPoolExecutor.CallerRunsPolicy());
        while(!taskQueue.isEmpty()){
            service.submit(new SaveDetailCrawlerThread());
            await();

        }
        while(true){
            if(service.isTerminated()){
                log.info("任务采集完毕");
                break;
            }
        }
        service.shutdown();
    }

    public void saveTask2Queue(){
        Wrapper<HotelInfoDO> wrapper = new QueryWrapper<HotelInfoDO>().orderByDesc("create_time");
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
            log.info("当前队列任务：{}",taskQueue.size());
            String resultHtml = "";
            DetailInfoDO detailInfoDO = new DetailInfoDO();
            String hotelId = ReUtil.getGroup0("([0-9]*)(?=.html)",task.getParam());
            for(int i=0;i<retryNum;i++) {
                log.info("url：{}正在执行第{}次爬取任务",task.getParam(),i+1);
                try {
                    resultHtml = secondDepthCrawlerServiceImpl.crawl(task.getParam(),task.getParam(), null, 2000);
                    break;
                }catch (Exception e){
                    log.info("爬虫链接超时，正在准备第{}次重试,当前二层url: {}", (i + 1), task.getParam());
                }
            }
            secondDepthCrawlerBiz.setDetailInfo(detailInfoDO,resultHtml);
            detailInfoDO.setHotelId(hotelId);
            detailInfoDO.setUrl(task.getParam());
            //获取房间信息
            detailInfoDO.setRoomInfo(secondDepthCrawlerBiz.getRoomInfo(hotelId));
            //数据库插入
            try{
                detailInfoService.save(detailInfoDO);
            }catch (Exception e){
                log.info("酒店详情保存失败，酒店id:{},失败信息:{}",hotelId,e.getMessage());
            }
            return null;
        }
    }
}
