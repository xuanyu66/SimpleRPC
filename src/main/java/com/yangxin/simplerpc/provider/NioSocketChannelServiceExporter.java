package com.yangxin.simplerpc.provider;

import com.yangxin.simplerpc.core.RequestMessage;
import com.yangxin.simplerpc.core.ResponseMessage;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.jboss.netty.handler.codec.frame.FrameDecoder;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author leon on 2018/8/7.
 * @version 1.0
 * @Description:
 */
public class NioSocketChannelServiceExporter implements ServiceExporter {

    private final int servicePort;

    private Channel channel;
    private InetSocketAddress localAddress;
    private ProviderManager providerManager;

    /***
     * @Description: 使用一个本地服务端口构造exporter，将通过指定端口提供服务。
     * @Param: [localPort]
     * @return:
     */
    public NioSocketChannelServiceExporter(int localPort){
        this.servicePort = localPort;
    }

    @Override
    public void export() {
        Objects.requireNonNull(providerManager, "please set providerManager non null " +
                "before export services");
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap.group(group).
                    channel(NioServerSocketChannel.class).
                    localAddress(new InetSocketAddress(servicePort)).
                    childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new FrameDecoder())
                                    .addLast(new ProtostuffDecoder<>(RequestMessage.class))
                                    .addLast(new ProtostuffEncoder<>(ResponseMessage.class))
                                    .addLast(new RequestMessageHandler(providerManager));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind().sync();
            this.channel = channelFuture.channel();
            this.localAddress = (InetSocketAddress)this.channel.localAddress();
            exportCompleted(providerManager);
            channelFuture.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully();
        }
    }


    protected void exportCompleted(ProviderManager providerManager){
        //export完成后置处理，未来可用于注册服务到注册中心等。
    }

    @Override
    public void setProviderManager() {

    }
}
