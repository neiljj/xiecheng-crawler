package com.xiecheng.crawler.utils;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReUtil;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * 工具
 * @author nijichang
 * @since 2021-06-22 16:39:09
 */
@Slf4j
public class Utils {

    private static WebClient getWebClient() {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_68);
        JavaScriptEngine engine = new JavaScriptEngine(webClient);
        webClient.setJavaScriptEngine(engine);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setTimeout(5000);
        return webClient;
    }

    public static String crawlPage(String url){
        WebClient wc = getWebClient();
        HtmlPage htmlPage = null;
        try {
            htmlPage = wc.getPage(url);
        } catch (FailingHttpStatusCodeException | IOException e) {
            log.error("获取网页出现错误，{}",e);

        }
        return htmlPage.getWebResponse().getContentAsString();
    }

    public static String crawlTextPage(String url){
        WebClient wc = getWebClient();
        TextPage page = null;
        try {
            page = wc.getPage(url);
        } catch (FailingHttpStatusCodeException | IOException e) {
            log.error("获取网页出现错误，{}",e);

        }
        return page.getWebResponse().getContentAsString();
    }
    public static String refFormatNowDate() {
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String retStrFormatNowDate = sdFormatter.format(nowTime);
        return retStrFormatNowDate;
    }

    public static String dateToStamp(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

    public static String dateTransfer(String dataTime){
        String beijingTimeStr = "";
        try {
            // 该pattern中的 E 标识星期，MMM标识月份
            String data = dataTime.replace("GMT", "").replaceAll("\\(.*\\)", "");
            // 将字符串转化为date类型，格式2016-10-12
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
            Date dateTrans = format.parse(data);
            SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 将其转化解析可的日期为：2019-10-12 14:19:40
            beijingTimeStr = formatDate.format(dateTrans);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return beijingTimeStr;
    }

    public static String offsetToDateString(String time){
        String result ;
        if(time.contains("分钟")){
            int minute = Integer.parseInt(ReUtil.getGroup0("\\d*",time));
            result = DateUtil.offset(new Date(), DateField.MINUTE,-minute).toString();
        }else if(time.contains("小时")){
            int hour = Integer.parseInt(ReUtil.getGroup0("\\d*",time));
            result = DateUtil.offset(new Date(), DateField.HOUR,-hour).toString();
        }else if(time.contains("昨天")){
            String hourAndMinute = ReUtil.getGroup0("\\d+:\\d+",time);
            if(StringUtils.isNotEmpty(hourAndMinute)) {
                hourAndMinute = hourAndMinute + ":00";
                LocalDate localDate = LocalDate.now();
                result = localDate.minusDays(1).toString() + " " + hourAndMinute;
            }else {
                result = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        }else if(time.contains("前天")){
            String hourAndMinute = ReUtil.getGroup0("\\d+:\\d+",time);
            if(StringUtils.isNotEmpty(hourAndMinute)) {
                hourAndMinute = hourAndMinute + ":00";
                LocalDate localDate = LocalDate.now();
                result = localDate.minusDays(2).toString() + " " + hourAndMinute;
            }else {
                result = LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        }else if(time.contains("天前")){
            int day = Integer.parseInt(ReUtil.getGroup0("\\d*",time));
            result = LocalDateTime.now().minusDays(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }else if(time.contains("秒")){
            int second = Integer.parseInt(ReUtil.getGroup0("\\d*",time));
            result = DateUtil.offset(new Date(), DateField.SECOND,-second).toString();
        }else {
            result = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return result;
    }

    public static String getMD5String(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8位字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            //一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方）
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args){
        String time = "昨天";
        System.out.println(offsetToDateString(time));
    }
}
