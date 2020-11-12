package com.xiecheng.crawler.interceptors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author nijichang
 * @since 2020-11-10 11:12:22
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/api/**")
                .addPathPatterns("/manage/center")
                .excludePathPatterns("/crawler")
                .excludePathPatterns("/login")
                .excludePathPatterns("/static/**")
                .excludePathPatterns("/health/check")
                .excludePathPatterns("/code/getCodeImage")
        ;
    }
}
