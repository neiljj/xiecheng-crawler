package com.xiecheng.crawler.aspect;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * @author nijichang
 * @since 2020-11-03 10:18:45
 */
@Aspect
@Component
@Slf4j
public class ServiceAspect {

    @Around("execution(* com.xiecheng.crawler.service.impl..*(..))")
    public Object around(ProceedingJoinPoint joinPoint){
        long start = System.currentTimeMillis();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String argsJson = JSONUtil.toJsonStr(joinPoint.getArgs());
        Object result = null;
        try{
            log.info("{} 请求参数: {}",methodSignature, argsJson);
            result = joinPoint.proceed();
            return result;
        }catch (Throwable e){
            log.error(e.getMessage());

        }finally {
            log.info("{} 耗时: {}ms 返回: {}",methodSignature,System.currentTimeMillis() - start, result instanceof String ? result.toString().substring(0,1000):null);
        }
        return result;
    }
}
