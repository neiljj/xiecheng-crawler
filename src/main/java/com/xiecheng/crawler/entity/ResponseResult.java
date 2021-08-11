package com.xiecheng.crawler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回结果
 * @author nijichang
 * @since 2021/6/28 9:56 AM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseResult {

    /*响应码 0表示成功，1表示失败*/
    private Integer code;
    /*响应消息*/
    private String msg;

    private Long count;
    /*响应数据*/
    private Object data;

    public ResponseResult(Integer code,String msg,Object data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static ResponseResult success(){
        return new ResponseResult(0,"success",null);
    }

    public static ResponseResult success(String msg){
        return new ResponseResult(0,msg,null);
    }

    public static ResponseResult success(String msg,Object data){
        return new ResponseResult(0,msg,data);
    }

    public static ResponseResult success(Object data){
        return new ResponseResult(0,"success",data);
    }

    public static ResponseResult success(Long count,Object data){
        return new ResponseResult(0,"success",count,data);
    }

    public static ResponseResult fail(){
        return new ResponseResult(1,"fail",null);
    }

    public static ResponseResult fail(String msg){
        return new ResponseResult(1,msg,null);
    }
}
