package com.xiecheng.crawler.constant;

/**
 * @author nijichang
 * @since 2020-10-30 18:04:13
 */
public interface CommonConstants {
       /*表示等待处理*/
       String SOURCE_STATE_WAIT = "WAITTING";
       /*表示已经处理完成*/
       String SOURCE_STATE_PROCESS = "PROCESSED";
       /*JWT 主题*/
       String JWT_SUBJECT = "crawler";
       /*过期时间，设置为一周*/
       long JWT_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;
       /*秘钥*/
       String JWT_APPSECRET = "mzj584wanlhy";
}
