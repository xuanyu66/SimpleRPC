package com.yangxin.simplerpc.rpcclient;

import com.yangxin.simplerpc.protocol.RequestMessage;
import com.yangxin.simplerpc.protocol.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @author leon on 2018/9/28.
 * @version 1.0
 * @Description:
 */
public class RpcProxy {

    public static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String serverAdderss;
    private ServiceDiscovery serviceDiscovery;

    public RpcClientHandler client;

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public RpcProxy(String serverAdderss) {
        this.serverAdderss = serverAdderss;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass) {
        return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RequestMessage request = new RequestMessage();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setArguments(args);

                        if (serviceDiscovery != null) {
                            serverAdderss = serviceDiscovery.discover();
                        }

                        String[] array = serverAdderss.split(":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        if (client == null){
                            client = new RpcClientHandler(host, port);
                        }

                        ResponseMessage response = client.send(request);
                        if (response.getThrowable() != null){
                            throw response.getThrowable();
                        } else {
                            return response.getResult();
                        }
                    }
                });
    }
}
