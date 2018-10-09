package com.yangxin.simplerpc.service;

/**
 * @author leon on 2018/9/25.
 * @version 1.0
 * @Description:
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }
}
