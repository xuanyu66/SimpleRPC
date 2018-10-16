package com.yangxin.simplerpc.rpcclient;

import com.yangxin.simplerpc.util.CustomThreadPoolExecutor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author leon on 2018/10/15.
 * @version 1.0
 * @description:
 */
public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private static ThreadPoolExecutor threadPoolExecutor = new CustomThreadPoolExecutor("ConnectionManager")
            .getCustomThreadPoolExecutor();

    private CopyOnWriteArrayList<RpcClientHandler> connectedHandlers = new CopyOnWriteArrayList<>();
    private Map<InetSocketAddress, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();

    private AtomicInteger roundRobin = new AtomicInteger(0);
    private volatile boolean running = true;
    private CountDownLatch latch = new CountDownLatch(1);

    private static class ManagerHolder {
        private static final ConnectionManager CONNECTION_MANAGER = new ConnectionManager();
    }

    public static ConnectionManager getConnectionMananger() {
        return ManagerHolder.CONNECTION_MANAGER;
    }

    public void updateConnectedServer(List<String> allServerAddress) {
        if (allServerAddress != null) {
            if (allServerAddress.size() > 0) {
                HashSet<InetSocketAddress> serverNodes = new HashSet<>();
                for (String serverAddress : allServerAddress) {
                    String[] array = serverAddress.split(":");
                    if (array.length == 2) {
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        final InetSocketAddress remoteServer = new InetSocketAddress(host, port);
                        serverNodes.add(remoteServer);
                    } else {
                        LOGGER.error("Invalid serverAddress : {}", serverAddress);
                    }
                }

                for (final InetSocketAddress inetSocketAddress :serverNodes) {
                    if (!connectedServerNodes.containsKey(inetSocketAddress)) {
                        connectServerNode(inetSocketAddress);
                    }
                }

                /**Close and remove invalid server nodes*/
                for (RpcClientHandler handler : connectedHandlers) {
                    SocketAddress address = handler.getRemoteAddress();
                    if (!serverNodes.contains(address)) {
                        LOGGER.info("Remove invalid server node " + address);
                        RpcClientHandler temp = connectedServerNodes.get(address);
                        if (temp != null){
                            temp.close();
                        }
                        connectedHandlers.remove(handler);
                        connectedServerNodes.remove(address);
                    }
                }

            }else {
                /** No available server node ( All server nodes are down )*/
                LOGGER.error("No available server node. All server nodes are down !!!");
                for (final RpcClientHandler connectedServerHandler : connectedHandlers) {
                    SocketAddress remotePeer = connectedServerHandler.getRemoteAddress();
                    RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                    handler.close();
                    connectedServerNodes.remove(connectedServerHandler);
                }
                connectedHandlers.clear();
            }
        }
    }

    private void connectServerNode(final InetSocketAddress remotePeer) {
        threadPoolExecutor.submit(()->{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcClientChannelInitializer());
            ChannelFuture channelFuture = bootstrap.connect(remotePeer);
            channelFuture.addListener( (ChannelFutureListener) channelFutureListener -> {
                if (channelFutureListener.isSuccess()){
                    LOGGER.info("Successfully connect to remote server. remote peer = " + remotePeer);
                    RpcClientHandler handler = channelFutureListener.channel()
                            .pipeline().get(RpcClientHandler.class);
                    addHandler(handler);
                    }
            });
        });
    }

    private void addHandler(RpcClientHandler handler) {
        connectedHandlers.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();
        connectedServerNodes.put(remoteAddress, handler);
        latch.countDown();
    }

    /**返回指定的handler，若不存在默认返回列表中第一位的handler*/
    public RpcClientHandler getHandler(InetSocketAddress address) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!running) {
            return null;
        }
        return connectedServerNodes.getOrDefault(address, connectedHandlers.get(0));
    }

    public RpcClientHandler chooseHandler() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int size = connectedHandlers.size();
        int index = (roundRobin.getAndAdd(1) + size) % size;
        if (!running) {
            return null;
        }
        return connectedHandlers.get(index);
    }

    public void stop() {
        running = false;
        for (RpcClientHandler handler : connectedHandlers) {
            handler.close();
        }
        threadPoolExecutor.shutdown();
    }

}
