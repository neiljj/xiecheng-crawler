package com.qudian.xiechengCrawler.generator;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.*;

/**
 * @ClassName MybatisGeneratorNew
 * @Description TODO
 * @Author husheng
 * @Date 2020-09-01 16:42
 */
public class MybatisGeneratorNew {

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    /**
     * RUN THIS
     */
    public static void main(String[] args) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        final String projectPath = System.getProperty("user.dir");
        System.out.println(projectPath);
        gc.setOutputDir(projectPath + "/employee-dao/src/main/java");
        gc.setAuthor("employee");
        gc.setOpen(false);
        gc.setFileOverride(true);
        gc.setEntityName("%sDO");
        mpg.setGlobalConfig(gc);


        // 数据源配置
        /*DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://10.11.26.8:3306/billing?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8");
        dsc.setDriverName("com.mysql.jdbc.Driver");
        dsc.setUsername("dev");
        dsc.setPassword("qufenqi123!@#");
        mpg.setDataSource(dsc);*/

        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://127.0.0.1:3306/xiecheng?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&failOverReadOnly=false&allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2B8&zeroDateTimeBehavior=convertToNull");
        // dsc.setSchemaName("public");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("123456");
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.qudian.xiechengCrawler");

        Map<String, String> pathInfo = new HashMap();
        pathInfo.put("entity_path", projectPath + "/src/main/java/com/qudian/xiechengCrawler/entity/po");
        pathInfo.put("mapper_path", projectPath + "/src/main/java/com/qudian/xiechengCrawler/dao");
        pc.setPathInfo(pathInfo);
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
            }
        };
        List<FileOutConfig> focList = new ArrayList();
        focList.add(new FileOutConfig("/templates/mapper.xml.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return projectPath + "/src/main/resources/mapper"
                        + "/" + tableInfo.getEntityName().replaceAll("DO","") + "Mapper" + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);
        mpg.setTemplate(new TemplateConfig().setXml(null));

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityLombokModel(true);
        strategy.setInclude(scanner("表名"));
        strategy.setTablePrefix(pc.getModuleName() + "_");
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        ConfigBuilder config = new ConfigBuilder(pc, dsc, strategy, new TemplateConfig().setXml(null), gc);
        cfg.setConfig(config);
        mpg.setConfig(config);
        mpg.execute();
    }
}
