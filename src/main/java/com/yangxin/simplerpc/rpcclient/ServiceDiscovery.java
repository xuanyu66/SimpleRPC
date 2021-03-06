package com.yangxin.simplerpc.rpcclient;

import com.yangxin.simplerpc.util.Constant;
import com.yangxin.simplerpc.util.ZookeeperUtil;
import io.netty.util.internal.ThreadLocalRandom;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author leon on 2018/9/27.
 * @version 1.0
 * @Description:
 */
public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<>();

    private ZooKeeper zookeeper;
    private String registryAddress;


    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;

        zookeeper = connectServer();
        if (zookeeper != null) {
            watchNode(zookeeper);
        }
    }

    public String discover() {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                LOGGER.debug("using only data: {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("using random data: {}", data);
            }
        }
        return data;
    }

    private ZooKeeper connectServer() {
        return new ZookeeperUtil().setRegistryAddress(registryAddress).connectServer();
    }

    private void watchNode(final ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                                watchNode(zk);
                            }
                        }
                    });
            List<String> dataList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/" +node,
                        false, null);
                dataList.add(new String(bytes));
            }
            LOGGER.debug("node data: {}", dataList);
            this.dataList = dataList;
            LOGGER.debug("Service discovery triggered updating connected server node.");
            UpdateConnectedServer();
        }catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
    }


    private void UpdateConnectedServer() {
        ConnectionManager.getConnectionMananger().updateConnectedServer(this.dataList);
    }

    public void stop(){
        if(zookeeper!=null){
            try {
                zookeeper.close();
            } catch (InterruptedException e) {
                LOGGER.error("", e);
            }
        }
    }
}
