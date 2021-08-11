package com.xiecheng.crawler.service.zhihu.biz;


import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiecheng.crawler.entity.NewsInfoDO;
import com.xiecheng.crawler.entity.es.po.NewsInfoEsDO;
import com.xiecheng.crawler.entity.po.CookieDO;
import com.xiecheng.crawler.enums.CookieTypeEnum;
import com.xiecheng.crawler.enums.CrawlerEnum;
import com.xiecheng.crawler.service.common.impl.NewsInfoEsService;
import com.xiecheng.crawler.service.common.impl.NewsInfoService;
import com.xiecheng.crawler.service.xiecheng.core.service.impl.CookieService;
import com.xiecheng.crawler.utils.HttpUtils;
import com.xiecheng.crawler.utils.Utils;
import com.xiecheng.crawler.utils.mapstruct.DataMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.python.core.*;
import org.python.util.PythonInterpreter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.util.UriEncoder;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.io.IOException;
import java.text.SimpleDateFormat;

import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author nijichang
 * @since 2021-06-24 15:24:16
 */
@Slf4j
@Service
public class ZhihuCrawler {

    @Resource
    private NewsInfoService newsInfoService;
    @Resource
    private CookieService cookieService;

    private static String URL = "https://www.zhihu.com/api/v4/search_v3?t=general&q=@keyword&correction=1&offset=0&limit=20&lc_idx=0&show_all_topics=0&time_zone=a_day";

    private static String X_ZSE_96_ENCODER = "101_3_2.0+@uri+\"@cookie.d_c0\"";

    public static String COOKIE;

    public void run(Map<CrawlerEnum,List<String>> keywordMap){
        List<String> keywordList = keywordMap.get(CrawlerEnum.ZHIHU);
        if(CollectionUtils.isEmpty(keywordList)){
            log.info("知乎关键词为空");
            return;
        }
        ExecutorService service = new ThreadPoolExecutor(2, 2,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(300),
                new ThreadFactoryImpl("zhihuThread_"));
        AtomicInteger no = new AtomicInteger(0);
        for(String keyword : keywordList) {
            log.info("知乎采集关键词：{}",keyword);
            submitTask(keyword,service,no);
        }
        //线程池首先需要shutdown再判断isTerminated
        service.shutdown();
        while(true){
            if(service.isTerminated()){
                break;
            }
        }
        log.info("知乎采集结束，本轮新增新闻{}条",no);
    }

    /**
     * 向线程池提交任务
     * @author nijichang
     * @since 2021/6/23 10:21 AM
     */
    private void submitTask(String keyword,ExecutorService service,AtomicInteger no){
        service.execute(() -> {
            String url = URL.replace("@keyword", UriEncoder.encode(keyword));
            String page = HttpUtils.doGet(url,buildHeader(url,COOKIE),5000,"utf-8");
            if(StringUtils.isNotEmpty(page)) {
                saveNews(page, keyword, no);
            }
        });
    }
    /**
     * 网页解析
     * @author nijichang
     * @since 2021/6/22 6:25 PM
     */
    private void saveNews(String page,String keyword,AtomicInteger no){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject object = JSON.parseObject(page);
        JSONArray data = object.getJSONArray("data");
        if(Objects.nonNull(data)) {
            for (int i = 0; i < data.size(); i++) {
                JSONObject answer = data.getJSONObject(i);
                JSONObject inObject = answer.getJSONObject("object");
                if (!"answer".equals(inObject.getString("type"))) {
                    continue;
                }
                String content = answer.getJSONObject("highlight").getString("description");
                String title = answer.getJSONObject("highlight").getString("title");
                String id = inObject.getString("id");
                String time = inObject.getString("updated_time");
                time = sdf.format(new Date(Long.parseLong(time) * 1000));
                JSONObject question = inObject.getJSONObject("question");
                String questionId = question.getString("id");
                String url = "https://www.zhihu.com/question/" + questionId + "/answer/" + id;
                NewsInfoDO newsInfoDO = new NewsInfoDO().setContent(content)
                        .setKeyword(keyword)
                        .setSource(CrawlerEnum.ZHIHU.getDesc())
                        .setTitle(title).setUrl(url).setTime(time);
                try {
                    newsInfoService.save(newsInfoDO);
                    no.getAndIncrement();
                } catch (DuplicateKeyException | PersistenceException e) {
                    log.error("该条新闻已经存在 ,{}", title);
                }
            }
        }else {
            log.error("知乎关键词：{}页面为空",keyword);
        }

    }
    @PostConstruct
    public void initCookie(){
        LambdaQueryWrapper<CookieDO> wrapper = new LambdaQueryWrapper<CookieDO>()
                .eq(CookieDO::getType, CookieTypeEnum.ZHIHU.getCode());
        List<CookieDO> cookieDos = cookieService.list(wrapper);
        COOKIE = cookieDos.stream().map(CookieDO::getCookie).findFirst().orElse("");
    }

    /**
     * 调用python代码加密code串
     * code格式 : x-zse-93+url+cookie.d_c0
     * @author nijichang
     * @since 2021/6/25 4:38 PM
     */
    private String encodeXzse96(String url,String cookie){
        String encoder ;
        String dC0 = ReUtil.getGroup0("(?<=d_c0=\")(.*?)(\")",cookie);
        dC0 = dC0.substring(0,dC0.length()-1);
        url = ReUtil.getGroup0("(?<=com)(.*)",url);
        encoder = X_ZSE_96_ENCODER.replace("@uri",url).replace("@cookie.d_c0",dC0);
        encoder = Utils.getMD5String(encoder);
        return pythonEncode(encoder);
    }
    private String pythonEncode(String code){
        Properties props = new Properties();
        props.put("python.import.site", "false");
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec("import sys");
        interpreter.exec("sys.path.append('/Users/qudian/opt/anaconda3/envs/py2/lib/python2.7/site-packages')");
        interpreter.exec("sys.path.append('/Users/qudian/opt/anaconda3/envs/py2/lib/python2.7/site-packages/execjs')");
        interpreter.execfile("/Users/qudian/Downloads/encoder.py");
        PyFunction func = interpreter.get("encoder",PyFunction.class);
        //调用函数，如果函数需要参数，在Java中必须先将参数转化为对应的“Python类型”
        PyObject pyobj = func.__call__(new PyString(code));
        return pyobj.toString();
    }

    /**
     * 请求头构造
     * @author nijichang
     * @since 2021/6/25 5:00 PM
     */
    private Map<String, String> buildHeader(String url, String cookie){
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
        headers.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Accept-Encoding","gzip, deflate, br");
        headers.put("Cookie",cookie);
        headers.put("x-zse-93","101_3_2.0");
        headers.put("x-zse-96","2.0_" + encodeXzse96(url,cookie));
        return headers;
    }

    /**
     * 测试 知乎反爬x-zse-96逆向破解
     * @author nijichang
     * @since 2021/6/25 4:22 PM
     */
    public static void main(String[] args){
        String text = "101_3_2.0+/api/v4/search_v3?t=general&q=nba&correction=1&offset=0&limit=20&lc_idx=0&show_all_topics=0+\"AMAcbHxh-hKPTjZdfHv2wJUhP4cD0Bj-vRU=|1618820416\"";
        String md5 = Utils.getMD5String(text);
        System.out.println(md5);
        Properties props = new Properties();
        props.put("python.import.site", "false");

        Properties preprops = System.getProperties();
//        PySystemState sys = Py.getSystemState();
//        System.out.println(sys.path.toString());
//        sys.path.add("/Users/qudian/.m2/repository/org/python/jython-standalone/2.7.0/Lib");
//        System.out.println(sys.path.toString());
        PythonInterpreter.initialize(preprops, props, new String[0]);
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec("import sys");
        interpreter.exec("sys.path.append('/Users/qudian/opt/anaconda3/envs/py2/lib/python2.7/site-packages')");
        interpreter.exec("sys.path.append('/Users/qudian/opt/anaconda3/envs/py2/lib/python2.7/site-packages/execjs')");
        interpreter.execfile("/Users/qudian/Downloads/encoder.py");
        PyFunction func = interpreter.get("encoder",PyFunction.class);
        //调用函数，如果函数需要参数，在Java中必须先将参数转化为对应的“Python类型”
        PyObject pyobj = func.__call__(new PyString(md5));
        System.out.println("the anwser is: " + pyobj);
    }
}
