package com.qudian.xiecheng.crawler.enums;

/**
 * @author nijichang
 * @since 2020-10-30 14:46:41
 */
public enum  CityEnum {

    GUANGZHOU("32","广州"),
    DONGGUAN("223","东莞"),
    BEIJING("1","北京"),
    SHANGHAI("2","上海"),
    SHENZHENG("30","深圳"),
    CHENGDU("28","成都"),
    HANGZHOU("17","杭州"),
    CHONGQIN("4","重庆"),
    WUHAN("477","武汉"),
    LANZHOU("100","兰州"),
    WENZHOU("491","温州"),
    KUNMING("34","昆明"),
    HAERBIN("5","哈尔滨"),
    SHANTOU("447","汕头"),
    XINING("124","西宁"),
    YINCHUAN("99","银川"),
    YICHANG("515","宜昌"),
    YANAN("110","延安"),
    SANYA("43","三亚")
        ;
    private String code;

    private String name;
    CityEnum(String code,String name){this.code = code;this.name = name;}

    public CityEnum getByCode(String code){
        for(CityEnum cityEnum : values()){
            if(cityEnum.code.equals(code)) return cityEnum;
        }
        return null;
    }
    public CityEnum getByName(String name){
        for(CityEnum cityEnum : values()){
            if(cityEnum.name.equals(name)) return cityEnum;
        }
        return null;
    }
}
