package com.xiecheng.crawler;

import com.xiecheng.crawler.entity.NewsInfoDO;
import com.xiecheng.crawler.entity.Task;
import com.xiecheng.crawler.entity.es.po.NewsInfoEsDO;
import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.CrawlerFactory;
import com.xiecheng.crawler.service.baidu.biz.TiebaCrawler;
import com.xiecheng.crawler.service.baidu.biz.ZhidaoCrawler;
import com.xiecheng.crawler.service.baidu.biz.ZixunCrawler;
import com.xiecheng.crawler.service.common.impl.CommonMethod;
import com.xiecheng.crawler.service.common.impl.NewsInfoEsService;
import com.xiecheng.crawler.service.three.biz.HotelnCrawler;
import com.xiecheng.crawler.service.xiecheng.biz.FirstDepthCrawlerBiz;
import com.xiecheng.crawler.service.xiecheng.core.TaskQueue;
import com.xiecheng.crawler.service.xiecheng.core.service.impl.CacheService;
import com.xiecheng.crawler.service.zhihu.biz.ZhihuCrawler;
import com.xiecheng.crawler.utils.mapstruct.DataMapping;
import org.assertj.core.util.Lists;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.zxp.esclientrhl.repository.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = XiechengCrawlerApplication.class)
public class XiechengCrawlerApplicationTests {

    @Resource
    private FirstDepthCrawlerBiz firstDepthCrawlerBiz;

    @Resource
    private CacheService cacheService;

    @Resource
    private CrawlerFactory crawlerFactory;

    @Resource
    private ZixunCrawler zixunCrawler;

    @Resource
    private ZhihuCrawler zhihuCrawler;

    @Resource
    private HotelnCrawler hotelnCrawler;

    @Resource
    private TaskQueue taskQueue;

    @Resource
    private ZhidaoCrawler zhidaoCrawler;

    @Resource
    private CommonMethod commonMethod;
    @Resource
    private TiebaCrawler tiebaCrawler;
    @Resource
    private NewsInfoEsService newsInfoEsService;
    @Resource
    private DataMapping dataMapping;


    private ElasticsearchTemplate<NewsInfoEsDO,String> elasticsearchTemplate;
    @Test
    public void test(){
        String param = "cityId=58";
        Task task = new Task();
        task.setParamTag(0);
        task.setParam(param);
        task.setDepthTag(0);
        try {
            TaskQueue.taskQueue.put(task);
        }catch (InterruptedException e){

        }
        firstDepthCrawlerBiz.process();
    }

    @Test
    public void testSaveCity(){
        taskQueue.saveCity();
    }

    @Test
    public void testSaveBrand(){
        taskQueue.saveBrand();
    }

    @Test
    public void test3(){
        while(true){
            cacheService.getCookie();
            try {
                Thread.sleep(2000);
            }catch (InterruptedException e){

            }
        }
    }

    @Test
    public void testBaidu(){
        crawlerFactory.factoryBeginWork();
    }

//    @Test
//    public void testOne(){
//        zixunCrawler.run(Lists.newArrayList("新冠疫苗"));
//    }
//
//    @Test
//    public void testzhihu(){
//        zhihuCrawler.run(Lists.newArrayList("nba","新冠疫苗"));
//    }
//
//    @Test
//    public void testHoteln(){
//        hotelnCrawler.run(Lists.newArrayList("酒店"));
//    }
//
//    @Test
//    public void testZhidao(){
//        zhidaoCrawler.run(Lists.newArrayList("新冠疫苗","酒店","季后赛"));
//    }

    @Test
    public void testInit(){
        Map<CrawlerEnum,List<String>> keywordMap = new HashMap<>();
        commonMethod.initKeyword(keywordMap);
    }

    @Test
    public void testTieba(){
        Map<CrawlerEnum,List<String>> map = new HashMap<>();
        map.put(CrawlerEnum.TIEBA,Lists.newArrayList("新冠疫苗"));
        tiebaCrawler.run(map);
    }

    @Test
    public void testEsInsert(){
        String str = "2021-07-22 09:23:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

        NewsInfoEsDO newsInfoEsDO = new NewsInfoEsDO().setTitle("业主与酒店管理公司合作的五大价值")
                .setContent("【酒店高参】业主与管理公司不仅仅是甲方与乙方的关系，在很多方面都可以产生更大的联动价值，本期酒店高参专栏作家杨峻带来了他的见解与看法。")
                .setId(2428L).setUrl("http://www.hoteln.cn/Html/HotelNewsSecond.html?NewsID=861&ClassId=1&SubClassID=107")
                .setSource("酒店高参").setCreate_time(dateTime).setTime(dateTime)
                .setKeyword("酒店")
                ;
        newsInfoEsService.save(newsInfoEsDO);
    }


    @Test
    public  void testEsQry(){
        String sorter = "time";
        Sort.Order order = new Sort.Order(SortOrder.DESC,sorter);
        PageSortHighLight pageSortHighLight = new PageSortHighLight(1,10);
        pageSortHighLight.setSort(new Sort(order));
        Attach attach = new Attach();
        attach.setPageSortHighLight(pageSortHighLight);
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        builder.must(QueryBuilders.fuzzyQuery("source","酒店参高"));
        PageList<NewsInfoEsDO> pageList = newsInfoEsService.searchByPage(builder,attach);
        System.out.println(pageList.getList());
    }

    @Test
    public void testMapping(){
        NewsInfoDO newsInfoDO = new NewsInfoDO().setTitle("业主与酒店管理公司合作的五大价值")
                .setContent("【酒店高参】业主与管理公司不仅仅是甲方与乙方的关系，在很多方面都可以产生更大的联动价值，本期酒店高参专栏作家杨峻带来了他的见解与看法。")
                .setId(2428L).setUrl("http://www.hoteln.cn/Html/HotelNewsSecond.html?NewsID=861&ClassId=1&SubClassID=107")
                .setSource("酒店高参").setCreateTime(LocalDateTime.now()).setTime("2021-01-01 ");
        NewsInfoEsDO newsInfoEsDO = dataMapping.toEsDo(newsInfoDO);
        System.out.println(newsInfoDO);

    }
}
