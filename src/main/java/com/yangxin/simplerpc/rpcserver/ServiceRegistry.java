package com.yangxin.simplerpc.rpcserver;

import com.yangxin.simplerpc.util.Constant;
import com.yangxin.simplerpc.util.ZookeeperUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author leon on 2018/9/25.
 * @version 1.0
 * @Description:
 */
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void registry (String data) {
        if (data != null) {
            ZooKeeper zk = connectServer();
            if (zk != null) {
                addRootNode(zk);
                createNode(zk, data);
            }
        }
    }

    private void addRootNode(ZooKeeper zk) {
        try {
            Stat stat = zk.exists(Constant.ZK_REGISTRY_PATH, false);
            if (stat == null) {
                zk.create(Constant.ZK_REGISTRY_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        } catch (InterruptedException |KeeperException e) {
            LOGGER.error(e.toString());
        }
    }

    private ZooKeeper connectServer() {
        return new ZookeeperUtil().setRegistryAddress(registryAddress).connectServer();
    }

    private void createNode(ZooKeeper zk, String data) {
        try {
            byte[] bytes = data.getBytes();
            String path = zk.create(Constant.ZK_DATA_PATH, bytes,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.info("create zookeeper node ({} => {})", path, data);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("Failed! create zookeeper node data :{}, /n{}", data, e);
        }
    }


}
