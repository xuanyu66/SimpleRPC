package com.yangxin.simplerpc.rpcserver;

import com.yangxin.simplerpc.coder.ProtostuffDecoder;
import com.yangxin.simplerpc.coder.ProtostuffEncoder;
import com.yangxin.simplerpc.protocol.RequestMessage;
import com.yangxin.simplerpc.protocol.ResponseMessage;
import com.yangxin.simplerpc.service.RpcService;
import com.yangxin.simplerpc.util.CustomThreadPoolExecutor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author leon on 2018/9/25.
 * @version 1.0
 * @Description:
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serverAddress;
    private ServiceRegistry serviceRegistry;


    private Map<String, Object> handlerMap = new HashMap<>();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    private static class ThreadPoolHolder{
        private static final ThreadPoolExecutor threadPoolExecutor = new CustomThreadPoolExecutor("RpcServer").getCustomThreadPoolExecutor();
    }

    public static ThreadPoolExecutor getThreadPoll() {
        return ThreadPoolHolder.threadPoolExecutor;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public static void submit(Runnable task) {
        getThreadPoll().submit(task);
    }

    public void start() throws Exception {
        if (bossGroup == null && workerGroup == null) {
            try {
                bossGroup = new NioEventLoopGroup();
                workerGroup = new NioEventLoopGroup();

                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                socketChannel.pipeline()
                                        .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                        .addLast(new ProtostuffDecoder(RequestMessage.class))
                                        .addLast(new ProtostuffEncoder(ResponseMessage.class))
                                        .addLast(new RpcServerHandler(handlerMap));
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                String[] array = serverAddress.split(":");
                String host = array[0];
                int port = Integer.parseInt(array[1]);

                ChannelFuture future = bootstrap.bind(host, port).sync();
                LOGGER.info("server started on pory {}", port);

                if (serviceRegistry != null) {
                    serviceRegistry.registry(serverAddress);
                }
                future.channel().closeFuture().sync();
            } finally {
                stop();
            }
        }
    }


    public RpcServer addService(String interfaceName, Object serviceBean) {
        if (!handlerMap.containsKey(interfaceName)) {
            LOGGER.info("Loading service: {}", interfaceName);
            handlerMap.put(interfaceName, serviceBean);
        }

        return this;
    }

    public void stop() {
        if (bossGroup != null){
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap =
                ctx.getBeansWithAnnotation(RpcService.class);
        if (!serviceBeanMap.isEmpty()) {
            for (Object serviceBean : serviceBeanMap.values()){
                String interfaceName = serviceBean.getClass()
                        .getAnnotation(RpcService.class).value().getName();
                handlerMap.put(interfaceName, serviceBean);
            }
        }
    }
}
