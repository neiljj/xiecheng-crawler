package com.xiecheng.crawler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @author nijichang
 * @since 2020-11-03 15:42:09
 */
@Getter
@AllArgsConstructor
public enum TypeEnum {
    QUICK_CHAIN("0","快捷连锁"),
    MIDDLE_CHAIN("8","中端连锁"),
    HIGH_CHAIN("1","高端连锁"),
    HOTEL("&feature=486","酒店"),
    HOMESTAY("&feature=489","民宿"),
    HOTEL_APARTMENT("&feature=487","酒店公寓"),
    INN("&feature=488","客栈"),
    SPECIAL_HOTEL("&feature=494","特色住宿"),
    FARM_HOTEL("&feature=493","农家乐"),
    YOUTH_HOTEL("&feature=491","青年旅社"),
    VILLA("&feature=492","别墅")
    ;

    private String code;

    private String desc;

    public static Optional<TypeEnum> getByCode(String code){
        for(TypeEnum typeEnum : values()){
            if(typeEnum.code.equals(code)) return Optional.of(typeEnum);
        }
        return Optional.empty();
    }
    public static Optional<TypeEnum> getByName(String name){
        for(TypeEnum typeEnum : values()){
            if(typeEnum.desc.equals(name)) return Optional.of(typeEnum);
        }
        return Optional.empty();
    }

    public static List<String> toList(){
        List<String> codes = new ArrayList<>();
        for(TypeEnum typeEnum : values()){
            codes.add(typeEnum.code);
        }
        return codes;
    }

    public static Map<String,String> toMap(){
        Map<String, String> map = new LinkedHashMap<>();
        for(TypeEnum typeEnum : values()){
            map.put(typeEnum.getDesc(),typeEnum.getCode());
        }
        return map;
    }
}
