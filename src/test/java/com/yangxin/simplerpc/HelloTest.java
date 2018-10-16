package com.yangxin.simplerpc;

import com.yangxin.simplerpc.rpcclient.RpcFuture;
import com.yangxin.simplerpc.rpcclient.ServiceDiscovery;
import com.yangxin.simplerpc.rpcclient.proxy.FutureProxy;
import com.yangxin.simplerpc.service.HelloService;
import com.yangxin.simplerpc.rpcclient.RpcClient;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;

/**
 * @author leon on 2018/9/28.
 * @version 1.0
 * @Description:
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:springclient.xml")
public class HelloTest {

    @Autowired
    private RpcClient rpcClient;

    public static void main(String[] args) {
        PropertyConfigurator.configure("src/log4j.properties");
        GenericXmlApplicationContext ac = new GenericXmlApplicationContext();
        ac.setValidating(false);
        ac.load("classpath:springclient.xml");
        ac.refresh();
        RpcClient rpcClient = (RpcClient) ac.getBean("rpcClient");
        HelloService helloService = rpcClient.create(HelloService.class);
        String result = helloService.hello("world");
        System.out.println("*******************" + result);
        String result2 = helloService.hello("mom");
        System.out.println("*******************" + result2);
        String result1 = helloService.hello("bob");
        System.out.println("*******************" + result1);
        System.out.println("down");
    }

    @Test
    public void helloFutureTest1() throws ExecutionException, InterruptedException {
        FutureProxy helloService = rpcClient.createAsync(HelloService.class);

        RpcFuture result = helloService.call("hello", "World");
        System.out.println(result.isDone());
        Assert.assertEquals("Hello! World", result.get());

        System.out.println(result.get());
        System.out.println(result.isDone());
        rpcClient.stop();
    }



}
