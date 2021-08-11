package com.xiecheng.crawler.service.baidu.biz;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiecheng.crawler.entity.NewsInfoDO;
import com.xiecheng.crawler.entity.es.po.NewsInfoEsDO;
import com.xiecheng.crawler.entity.po.CookieDO;
import com.xiecheng.crawler.enums.CookieTypeEnum;
import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.baidu.BaiduCommonCrawlerProcessor;
import com.xiecheng.crawler.service.common.impl.NewsInfoEsService;
import com.xiecheng.crawler.service.common.impl.NewsInfoService;
import com.xiecheng.crawler.service.xiecheng.core.service.impl.CookieService;
import com.xiecheng.crawler.utils.HttpUtils;
import com.xiecheng.crawler.utils.mapstruct.DataMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.util.UriEncoder;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 百度知道 爬虫
 * @author nijichang
 * @since 2021-06-23 10:40:56
 */
@Slf4j
@Service
public class ZhidaoCrawler implements BaiduCommonCrawlerProcessor {
    @Resource
    private NewsInfoService newsInfoService;
    @Resource
    private CookieService cookieService;

    private static String URL = "https://zhidao.baidu.com/search?word=@keyword&lm=0&site=-1&sites=0&date=2&ie=gbk";

    public static String COOKIE;

    @Override
    public void run(Map<CrawlerEnum,List<String>> keywordMap){
        List<String> keywordList = keywordMap.get(CrawlerEnum.ZHIDAO);
        if(CollectionUtils.isEmpty(keywordList)){
            log.info("知道关键词为空");
            return;
        }
        AtomicInteger no = new AtomicInteger(0);
        ExecutorService service = new ThreadPoolExecutor(3, 3,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(300),
                new ThreadFactoryImpl("zhidaoThread_"));

        for(String keyword : keywordList) {
            submitTask(keyword,service,no);
        }
        //线程池首先需要shutdown再判断isTerminated
        service.shutdown();
        while(true){
            if(service.isTerminated()){
                log.info("百度知道采集结束,本轮新增新闻{}条",no);
                break;
            }
        }
    }

    @PostConstruct
    public void initCookie(){
        LambdaQueryWrapper<CookieDO> wrapper = new LambdaQueryWrapper<CookieDO>()
                .eq(CookieDO::getType, CookieTypeEnum.ZHIDAO.getCode());
        List<CookieDO> cookieDos = cookieService.list(wrapper);
        COOKIE = cookieDos.stream().map(CookieDO::getCookie).findFirst().orElse("");
    }

    /**
     * 向线程池提交任务
     * @author nijichang
     * @since 2021/6/23 10:21 AM
     */
    private void submitTask(String keyword,ExecutorService service,AtomicInteger no){
        log.info("百度知道采集关键词，{}",keyword);
        service.execute(() -> {
            String url = URL.replace("@keyword", UriEncoder.encode(keyword));
            Map<String, String> headers = buildHeader(keyword,COOKIE);
            String page = HttpUtils.doGet(url,headers,5000,"gbk");
            if(StringUtils.isNotEmpty(page)) {
                saveNews(page, keyword, no);
            }
        });
    }
    /**
     * 网页解析
     * @author nijichang
     * @since 2021/6/23 10:48 AM
     */
    private void saveNews(String page,String keyword,AtomicInteger no){
        Document document = Jsoup.parse(page);
        Elements dls = document.select("dl.dl");
        if(Objects.nonNull(dls)) {
            for (Element dl : dls) {
                Element dt = dl.selectFirst("dt.dt.mb-3.line");
                String url = dt.select("a").attr("href");
                String title = dt.select("a").text();
                String content = dl.select("dd.dd.answer").text();
                String time = dl.select("dd.dd.explain.f-lighter").select("span").first().text();
                if (StringUtils.isNotEmpty(time)) {
                    time += " 00:00:00";
                }
                NewsInfoDO newsInfoDO = new NewsInfoDO().setContent(content)
                        .setKeyword(keyword)
                        .setSource(CrawlerEnum.ZHIDAO.getDesc())
                        .setTitle(title).setUrl(url).setTime(time);
                try {
                    newsInfoService.save(newsInfoDO);
                    no.getAndIncrement();
                } catch (DuplicateKeyException | PersistenceException e) {
                    log.error("该条新闻已经存在 ,{}", title);
                }
            }
        }else {
            log.error("百度知道关键词：{}页面为空",keyword);
        }
    }

    private Map<String, String> buildHeader(String keyword,String cookie){
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
        headers.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Accept-Encoding","gzip, deflate, br");
        headers.put("Cookie",cookie);
        headers.put("Connection","keep-alive");
        headers.put("Referer","https://zhidao.baidu.com/search?ct=17&pn=0&tn=ikaslist&rn=10&fr=wwwt&ie=utf-8&word=" + UriEncoder.encode(keyword));
        return headers;
    }

    /**
     * 测试
     * @author nijichang
     * @since 2021/6/23 2:34 PM
     */
    public static void main(String[] args){
        String url = "https://zhidao.baidu.com/search?word=%D0%C2%B9%DA%D2%DF%C3%E7&lm=0&site=-1&sites=0&date=2&ie=gbk";
//        List<Header> headers = new ArrayList<>();
//        headers.add(new Header().setName("Host").setValue("zhidao.baidu.com"));
//        headers.add(new Header().setName("Referer").setValue("https://zhidao.baidu.com/search?ct=17&pn=0&tn=ikaslist&rn=10&fr=wwwt&ie=utf-8&word=%E6%96%B0%E5%86%A0%E7%96%AB%E8%8B%97"));
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
        headers.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Accept-Encoding","gzip, deflate, br");
        headers.put("Cookie","PSTM=1618802853; BAIDUID=9BA2C2947640D015C8B924BA0CBBBA7B:FG=1; BIDUPSID=EEAB2D66EA3916EFDB62B72AE64DEA12; __yjs_duid=1_1d8d8a155bc75f877f426a46ac9b46f61618836206023; BDUSS=dSQU1NWmpDRFVhb2dLSzJCR1NRflNwbTJiRWNVelBPMUxNaHgxN21GclBUTEJnSUFBQUFBJCQAAAAAAAAAAAEAAAAhJfDTbmVpbGprYwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAM-~iGDPv4hgUF; BDUSS_BFESS=dSQU1NWmpDRFVhb2dLSzJCR1NRflNwbTJiRWNVelBPMUxNaHgxN21GclBUTEJnSUFBQUFBJCQAAAAAAAAAAAEAAAAhJfDTbmVpbGprYwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAM-~iGDPv4hgUF; BDORZ=B490B5EBF6F3CD402E515D22BCDA1598; BDSFRCVID=zruOJexroG0YszJeFJWKrnzuXn7-bPnTDYLtOwXPsp3LGJLVgaSTEG0Ptt_Gv5I-2ZlgogKK3mOTHR8F_2uxOjjg8UtVJeC6EG0Ptf8g0M5; H_BDCLCKID_SF=tbutoI_-fC03qnkxDCI-ejvBDN_qJ5LtHD7XV-nP0l7keq8CDRAa3607hUoxW4rZKDQABhvKaCjTDfb2y5jHhUrBMROCtIr9BgDfBnjGbUJpsIJMQtDWbT8U5f5m-lROaKviaKOEBMb1VCnDBT5h2M4qMxtOLR3pWDTm_q5TtUJMeCnTD-Dhe6byjH-DJj-s55TKWJcVK6raqjrnhPF3Kq8rXP6-35KHLDJL0Cbt54P2KJr4hR6hWhQBXl3zBq37JD6y3qjj2MA-8tnY3nuVhboWhPoxJpO7BRbMopvaHRnUbKnvbURvDPug3-7NqU5dtjTO2bc_5KnlfMQ_bf--QfbQ0hOhqP-jBRIE_D0yJD_hhIvPKITD-tFO5eT22-ustg8O2hcHMPoosIJXKn6kbfKpDgcH2JTtyI_fsRoxafbUoqRmXnJi0btQDPvxBf7p5K6g-p5TtUJMfM7j56ozqjDlhMJyKMnitKj9-pn4fpQrh459XP68bTkA5bjZKxtq3mkjbPbDfn028DKuj6tWj6j0DNR22Pc22-oKWnTXbRu_Hn7zeUjhDM4pbq7H2M-jy67EBJ5IWDOmqRru5q7_jIuV0MTn0pcr3C3q_I5nMITWVh5V34JH0p0kQN3T-n3d35Fta6C-yJRJDn3oyTbVXp0n0G7ly5jtMgOBBJ0yQ4b4OR5JjxonDh83bG7MJUutfJFfVIL-JC83H48k-4QEbbQH-UnLqbTHbmOZ04n-ah02otOuy4oPyl-UKxTE-lvXJ-jabnrm3UTdfh76Wh35K5tTQP6rLtbKLan4KKJxbPbrsPLGyDFKW4AshUJiB5JLBan7bDnIXKohJh7FM4tW3J0ZyxomtfQxtNRJ0DnjtpChbRO4-TFhj65-Dx5; BDSFRCVID_BFESS=zruOJexroG0YszJeFJWKrnzuXn7-bPnTDYLtOwXPsp3LGJLVgaSTEG0Ptt_Gv5I-2ZlgogKK3mOTHR8F_2uxOjjg8UtVJeC6EG0Ptf8g0M5; H_BDCLCKID_SF_BFESS=tbutoI_-fC03qnkxDCI-ejvBDN_qJ5LtHD7XV-nP0l7keq8CDRAa3607hUoxW4rZKDQABhvKaCjTDfb2y5jHhUrBMROCtIr9BgDfBnjGbUJpsIJMQtDWbT8U5f5m-lROaKviaKOEBMb1VCnDBT5h2M4qMxtOLR3pWDTm_q5TtUJMeCnTD-Dhe6byjH-DJj-s55TKWJcVK6raqjrnhPF3Kq8rXP6-35KHLDJL0Cbt54P2KJr4hR6hWhQBXl3zBq37JD6y3qjj2MA-8tnY3nuVhboWhPoxJpO7BRbMopvaHRnUbKnvbURvDPug3-7NqU5dtjTO2bc_5KnlfMQ_bf--QfbQ0hOhqP-jBRIE_D0yJD_hhIvPKITD-tFO5eT22-ustg8O2hcHMPoosIJXKn6kbfKpDgcH2JTtyI_fsRoxafbUoqRmXnJi0btQDPvxBf7p5K6g-p5TtUJMfM7j56ozqjDlhMJyKMnitKj9-pn4fpQrh459XP68bTkA5bjZKxtq3mkjbPbDfn028DKuj6tWj6j0DNR22Pc22-oKWnTXbRu_Hn7zeUjhDM4pbq7H2M-jy67EBJ5IWDOmqRru5q7_jIuV0MTn0pcr3C3q_I5nMITWVh5V34JH0p0kQN3T-n3d35Fta6C-yJRJDn3oyTbVXp0n0G7ly5jtMgOBBJ0yQ4b4OR5JjxonDh83bG7MJUutfJFfVIL-JC83H48k-4QEbbQH-UnLqbTHbmOZ04n-ah02otOuy4oPyl-UKxTE-lvXJ-jabnrm3UTdfh76Wh35K5tTQP6rLtbKLan4KKJxbPbrsPLGyDFKW4AshUJiB5JLBan7bDnIXKohJh7FM4tW3J0ZyxomtfQxtNRJ0DnjtpChbRO4-TFhj65-Dx5; delPer=0; BAIDUID_BFESS=9BA2C2947640D015C8B924BA0CBBBA7B:FG=1; BDRCVFR[feWj1Vr5u3D]=I67x6TjHwwYf0; BDRCVFR[C0p6oIjvx-c]=mk3SLVN4HKm; shitong_key_id=2; H_PS_PSSID=34130_34100_33967_31660_33848_33607_34106_34134_26350_22159; PSINO=6; BA_HECTOR=2485ag0gal2h852hrc1gd591f0q; ZD_ENTRY=baidu; Hm_lvt_6859ce5aaf00fb00387e6434e4fcc925=1622079783,1624244707,1624359607,1624417330; Hm_lpvt_6859ce5aaf00fb00387e6434e4fcc925=1624417341; ab_sr=1.0.1_MjVhODVlZGU3YTBlZDE5NGI2ODM1MTQ0Mjg4ODIxMmMzMGI4ZWM5Nzg0NWY5NzhmZWYxZDlhMzA2MjY3YmExN2EzN2NkZmNlMDM5ZDRhM2VkMjlmMDliYzRlODFhMGJjZGNiZjFjYThlMzY1NmViMTg3ZTA3OGYzMTEyMjk4YzE2OTdjZTE0NGNmYTVjY2ZhNTYzYWQ3NWFmZTlhZmRiZjRmNTYzNDE4YTIzZTUyZDI4YjAyMGY5MjI3ZDU3MDY0; shitong_data=f4ab07d56c6166339c4a69ba87b788cdfcf806527b61fb7e59bbb120fc6f8fe4beadb51401bc5f6a15e4ad303123ffdfc46d848a40025414647aa6ec2fe27ff0bbb9dbcc1c27987dadf3646c8172da892a30ff370aaeabf95ff27dd96f12d29bb042b9032f0865a70f43c7171110899a97df493bc2fe161f7963a8f8d72f9816; shitong_sign=82df462e");
    //    headers.put("Host","zhidao.baidu.com");
        headers.put("Connection","keep-alive");
        headers.put("Referer","https://zhidao.baidu.com/search?ct=17&pn=0&tn=ikaslist&rn=10&fr=wwwt&ie=utf-8&word=%E6%96%B0%E5%86%A0%E7%96%AB%E8%8B%97");
        String page = HttpUtils.doGet(url,headers,5000,"gbk");
        ZhidaoCrawler zhidaoCrawler = new ZhidaoCrawler();
        zhidaoCrawler.saveNews(page,"",null);
    }
}
