package com.yangxin.simplerpc.rpcclient;

import com.yangxin.simplerpc.coder.ProtostuffDecoder;
import com.yangxin.simplerpc.coder.ProtostuffEncoder;
import com.yangxin.simplerpc.protocol.RequestMessage;
import com.yangxin.simplerpc.protocol.ResponseMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author leon on 2018/9/28.
 * @version 1.0
 * @Description:
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<ResponseMessage> {

    public static final Logger LOGGER = LoggerFactory.getLogger(RpcClientHandler.class);

    private String host;
    private int port;

    private ResponseMessage response;


    private volatile Channel channel;
    private EventLoopGroup group;

    private final Object obj = new Object();

    public RpcClientHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseMessage response) throws Exception {
        this.response = response;

        synchronized (obj) {
            obj.notifyAll();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }


    public ResponseMessage send(RequestMessage request) throws Exception{
        if (group == null) {
            group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                    .addLast(new ProtostuffEncoder(RequestMessage.class))
                                    .addLast(new ProtostuffDecoder(ResponseMessage.class))
                                    .addLast(RpcClientHandler.this);
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            bootstrap.connect(host, port).sync();
        }

        channel.writeAndFlush(request).sync();

        synchronized (obj){
            obj.wait();
        }

        return response;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        group.shutdownGracefully();
        LOGGER.info("channel and EventLoopGroup stopped.");
    }

    public Channel getChannel() {
        return channel;
    }

}
