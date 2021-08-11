package com.xiecheng.crawler.utils.mapstruct;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * mapstruct String转LocalDateTime
 * @author nijichang
 * @since 2021-07-28 15:23:12
 */
@Component
public class DateMapper {
    public LocalDateTime asLocalDateTime(String str){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(str, formatter);
    }
}
