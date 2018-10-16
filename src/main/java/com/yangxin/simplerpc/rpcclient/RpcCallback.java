package com.yangxin.simplerpc.rpcclient;

/**
 * @author leon on 2018/10/16.
 * @version 1.0
 * @description:
 */
public interface RpcCallback {

    void success(Object result);

    void fail(Exception e);
}
