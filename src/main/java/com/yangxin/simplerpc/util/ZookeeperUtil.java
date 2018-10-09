package com.yangxin.simplerpc.util;

import com.yangxin.simplerpc.provider.Constant;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author leon on 2018/9/27.
 * @version 1.0
 * @Description:
 */
public class ZookeeperUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperUtil.class);

    private String registryAddress;

    private CountDownLatch latch = new CountDownLatch(1);

    public String getRegistryAddress() {
        return registryAddress;
    }

    public ZookeeperUtil setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
        return this;
    }

    public ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent watchedEvent) {
                            if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                                latch.countDown();
                            }
                        }
                    });
            latch.await();
        }catch (IOException | InterruptedException e){
            LOGGER.error("connect to zookeeper failed {}", e);
        }
        return zk;
    }
}
