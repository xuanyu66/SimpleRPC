package com.yangxin.simplerpc.provider;

/**
 * @author leon on 2018/9/25.
 * @version 1.0
 * @Description:
 */
public interface Constant {
    int ZK_SESSION_TIMEOUT = 5000;

    String ZK_REGISTRY_PATH = "/rpcRegistry";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

    int THREADPOOL_COREPOOLSIZE = (int) (Runtime.getRuntime().availableProcessors() * 2);
    int THREADPOOL_MAXINUMPOOLSIZE = 3 * Runtime.getRuntime().availableProcessors() + 1;
    int KEEPALIVE_TIME = 3;
    int BLOCKINGQUEQUE_SIZE = 500;
}
