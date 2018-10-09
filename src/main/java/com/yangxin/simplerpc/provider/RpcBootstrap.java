package com.yangxin.simplerpc.provider;


import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * @author leon on 2018/9/25.
 * @version 1.0
 * @Description:
 */
public class RpcBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcBootstrap.class);

    public static void main(String[] args) {


        PropertyConfigurator.configure("src/log4j.properties");
        GenericXmlApplicationContext ac = new GenericXmlApplicationContext();
        ac.setValidating(false);
        ac.load("classpath:spring.xml");
        ac.refresh();
        LOGGER.info("Rpc service start");
    }
}
