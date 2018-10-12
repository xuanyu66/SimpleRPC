package com.yangxin.simplerpc.util;

/**
 * @author leon on 2018/9/25.
 * @version 1.0
 * @Description:
 */
public interface Constant {
    int ZK_SESSION_TIMEOUT = 5000;

    String ZK_REGISTRY_PATH = "/rpcRegistry";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

    int THREAD_POOL_CORE_POOL_SIZE = (int) (Runtime.getRuntime().availableProcessors() * 2);
    int THREAD_POOL_MAXIMUM_POOL_SIZE = 3 * Runtime.getRuntime().availableProcessors() + 1;
    int KEEP_ALIVE_TIME = 3;
    int BLOCKING_QUEUE_SIZE = 500;
}
