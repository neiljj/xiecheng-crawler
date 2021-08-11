package com.xiecheng.crawler.service.weixin.biz;

import com.xiecheng.crawler.service.common.impl.NewsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Properties;

/**
 * @author nijichang
 * @since 2021-07-03 10:00:36
 */
@Slf4j
@Service
public class WeixinCrawler {

    @Resource
    private NewsInfoService newsInfoService;


    public static void main(String[] args){
        String keyword = "ktv";
        Properties props = new Properties();
        props.put("python.import.site", "false");
        Properties preprops = System.getProperties();
        PythonInterpreter.initialize(preprops, props, new String[0]);
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec("import sys");
        interpreter.exec("sys.path.append('/Users/qudian/opt/anaconda3/lib/python3.8/site-packages')");
        interpreter.exec("sys.path.append('/Users/qudian/opt/anaconda3/lib/python3.8/site-packages/requests')");
//        interpreter.exec("sys.path.append('/Users/qudian/opt/anaconda3/envs/py2/lib/python2.7/site-packages/urllib')");
//        interpreter.exec("sys.path.append('/Users/qudian/opt/anaconda3/envs/py2/lib/python2.7/site-packages/lxml')");
        interpreter.execfile("/Users/qudian/Downloads/sougou_weixin.py");
        PyFunction func = (PyFunction)interpreter.get("crawler",PyFunction.class);
        //调用函数，如果函数需要参数，在Java中必须先将参数转化为对应的“Python类型”
        PyObject pyobj = func.__call__(new PyString(keyword),new PyInteger(1));
        System.out.println("the anwser is: " + pyobj);
    }
}
