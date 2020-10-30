package com.qudian.xiecheng.crawler.enums;

/**
 * @author nijichang
 * @since 2020-10-30 15:21:38
 */
public enum StarEnum {
    TWO_STAR(2,"两星及以下"),
    THREE_STAR(3,"三星级"),
    FOUR_STAR(4,"四星级"),
    FIVE_STAR(5,"五星级")
    ;
    private Integer code;

    private String desc;

    StarEnum(Integer code,String desc){this.code = code;this.desc = desc;}

    public StarEnum getByCode(String code){
        for(StarEnum starEnum : values()){
            if(starEnum.code.equals(code)) return starEnum;
        }
        return null;
    }
    public StarEnum getByName(String name){
        for(StarEnum starEnum : values()){
            if(starEnum.desc.equals(name)) return starEnum;
        }
        return null;
    }
}
