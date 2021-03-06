package com.xiecheng.crawler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

/**
 * @author nijichang
 * @since 2020-10-30 15:21:38
 */
@Getter
@AllArgsConstructor
public enum StarEnum {
    TWO_STAR("hotel_diamond02","两星及以下"),
    THREE_STAR("hotel_diamond03","三星级"),
    FOUR_STAR("hotel_diamond04","四星级"),
    FIVE_STAR("hotel_diamond05","五星级")
    ;

    private String code;

    private String desc;


    public static Optional<StarEnum> getByCode(String code){
        for(StarEnum starEnum : values()){
            if(starEnum.code.equals(code)) return Optional.of(starEnum);
        }
        return Optional.empty();
    }
    public static Optional<StarEnum> getByName(String name){
        for(StarEnum starEnum : values()){
            if(starEnum.desc.equals(name)) return Optional.of(starEnum);
        }
        return Optional.empty();
    }
}
