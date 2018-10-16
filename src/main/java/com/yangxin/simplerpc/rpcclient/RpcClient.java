package com.yangxin.simplerpc.rpcclient;

import com.yangxin.simplerpc.rpcclient.proxy.FutureProxy;
import com.yangxin.simplerpc.util.CustomThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.reflect.Proxy;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author leon on 2018/9/28.
 * @version 1.0
 * @Description:
 */
public class RpcClient {

    public static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private static ThreadPoolExecutor threadPoolExecutor = new CustomThreadPoolExecutor("RpcClient")
            .getCustomThreadPoolExecutor();
    private ServiceDiscovery serviceDiscovery;

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass) {
        return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new FutureProxy(interfaceClass));
    }

    public <T> FutureProxy createAsync(Class<?> interfaceClass) {
        return new FutureProxy(interfaceClass);
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    public void stop() {
        serviceDiscovery.stop();
        ConnectionManager.getConnectionMananger().stop();
    }
}
