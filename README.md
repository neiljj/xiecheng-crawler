# xiecheng-crawler 携程酒店爬虫，会获取酒店类型，品牌，城市作为查询参数
注意：cookie一礼拜会过期，需要更换
爬虫启动有两种方法：

1.启动XiechengCrawlerApplication，直接访问 http:/localhost:8080/crawler/api/run
2.跑单测XiechengCrawlerApplicationTests.test()方法

- 技术组合：SpringBoot,SpringMvc,mybatis-plus,mapstruct,hutool,Thymeleaf,caffeine
- 前端框架：Layui

后续逐步做成任务可配置化，包含前端页面，添加采集任务，采集结果展示等

## 后台使用说明
1. 数据库创建xiecheng库，字符集为utf8
2. 运行tables文件，建表
3. 更新配置里的cookie
4. 运行单测XiechengCrawlerApplicationTests中saveCity()和saveBrand()，初始化数据库数据
5. 启动XiechengCrawlerApplication，访问localhost:8080/crawler
6. 注册+登录+起任务


##### 特别说明

**本项目设计的内容仅供学习使用，不得用于其他非法用途！！**
