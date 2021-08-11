该项目已更新为通用爬虫，目前包含的采集来源有百度，微博，知乎，迈点，酒店高参，环球旅讯
可由后台配置需要采集的关键词，爬虫每天9，14，18时会进行采集一次

1.启动XiechengCrawlerApplication，直接访问 http:/localhost:8080/crawler/

- 技术组合：SpringBoot,SpringMvc,mybatis-plus,mapstruct,hutool,Thymeleaf,caffeine，redis,docker,elasticSearch
- 前端框架：Layui

## 后台使用说明
1. 数据库创建xiecheng库，字符集为utf8
2. 运行tables文件，建表
5. 启动XiechengCrawlerApplication，访问localhost:8080/crawler
6. 注册+登录+起任务


##### 特别说明

**本项目设计的内容仅供学习使用，不得用于其他非法用途！！**


知乎爬虫，需要先安装nodejs，安装anaconda，python
参考文档：https://blog.csdn.net/qq_27859693/article/details/113202582

