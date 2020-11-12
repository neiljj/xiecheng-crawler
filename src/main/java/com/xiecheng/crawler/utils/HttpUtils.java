package com.xiecheng.crawler.utils;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 封装http get post
 */
@Slf4j
public class HttpUtils {

    private static Integer retryNum = 2;

    private static  final Gson gson = new Gson();
    /**
     * get方法
     * @param url
     * @return
     */
    public static String doGet(String url,Map<String,String> headers, int timeout){

        Map<String,Object> map = new HashMap<>();
        CloseableHttpClient httpClient =  HttpClients.createDefault();
        //设置参数
        RequestConfig requestConfig =  RequestConfig.custom().setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .setRedirectsEnabled(true)
                .build();

        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        headers.forEach((k,v) -> httpGet.addHeader(k,v));
        for(int i=0;i<retryNum;i++){
            try{
               // 获取请求响应结果
               HttpResponse httpResponse = httpClient.execute(httpGet);
               if(httpResponse.getStatusLine().getStatusCode() == 200){
                   //这里注意设置默认字节编码格式 为 utf-8
                   String result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
                   //解决乱码
                   return result;
               }
               break;
            }catch (Exception e){
                log.info("爬虫链接:{}超时，正在准备第{}次重试", url,(i + 1));
                continue;
            }
        }
        try {
            httpClient.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 封装post
     * @return
     */
    public static String doPost(String url, String data, Map<String,String> headers, int timeout){
        CloseableHttpClient httpClient =  HttpClients.createDefault();

        //超时设置
        RequestConfig requestConfig =  RequestConfig.custom().setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .setRedirectsEnabled(true)
                .build();

        HttpPost httpPost  = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        headers.forEach((k,v) -> httpPost.addHeader(k,v));
        if(data != null && data instanceof  String){
            StringEntity stringEntity = new StringEntity(data,"UTF-8");
            httpPost.setEntity(stringEntity);
        }
        for(int i=0;i<retryNum;i++) {
            try {
                CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    String result = EntityUtils.toString(httpEntity, "utf-8");
                    return result;
                }
                break;
            } catch (Exception e) {
                log.info("爬虫链接:{}超时，正在准备第{}次重试", url,(i + 1));
                continue;
            }
        }
        try {
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args){
        String uri = "https://hotels.ctrip.com/hotel/beijing1#ctm_ref=hod_hp_sb_lst";
        String html = HttpUtil.get(uri);
        Document document = Jsoup.parse(html);
        Elements divs = document.select("div[id=J_BrandFilterList]").select("div[class=optionList-item  ]");
        System.out.println(divs.size());
        Map<String,String> brands = new LinkedHashMap<>();
        for(Element div : divs){
            if("0".equals(div.attr("data-value")) || "8".equals(div.attr("data-value")) || "1".equals(div.attr("data-value")))continue;
            brands.put(div.attr("data-value"),div.attr("title"));
        }
        System.out.println(brands.size());
        System.out.println(brands);
    }
}

