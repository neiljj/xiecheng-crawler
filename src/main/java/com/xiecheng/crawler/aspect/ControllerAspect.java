package com.xiecheng.crawler.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/** todo
 * @author nijichang
 * @since 2020-11-09 10:27:49
 */
@Aspect
@Slf4j
@Component
public class ControllerAspect {

//    @Around("execution(* com.xiecheng.crawler.controller..*(..)) " +
//            "&& !execution(* com.xiecheng.crawler.controller.HealthCheckController.*(..))" +
//            " && !execution(* com.xiecheng.crawler.controller.CrawlerController.*(..))")
//    public Object around(ProceedingJoinPoint joinPoint){
//        long start = System.currentTimeMillis();
//        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
//
//        return null;
//    }
}
