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

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author leon on 2018/9/28.
 * @version 1.0
 * @Description:
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<ResponseMessage> {

    public static final Logger LOGGER = LoggerFactory.getLogger(RpcClientHandler.class);

    private SocketAddress remoteAddress;
    private volatile Channel channel;
    private EventLoopGroup group;

    private ConcurrentHashMap<String, RpcFuture> pendingRPC = new ConcurrentHashMap<>();

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remoteAddress = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseMessage response) throws Exception {
        String requsetId = response.getRequestId();
        RpcFuture rpcFuture = pendingRPC.get(requsetId);

        if (rpcFuture != null) {
            pendingRPC.remove(requsetId);
            rpcFuture.done(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }

    public RpcFuture sendRequest(RequestMessage request) {
        final CountDownLatch latch = new CountDownLatch(1);
        RpcFuture rpcFuture = new RpcFuture(request);
        pendingRPC.put(request.getRequestId(), rpcFuture);
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.error("{}", e);
        }

        return rpcFuture;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        LOGGER.info("channel and EventLoopGroup stopped.");
    }

    public Channel getChannel() {
        return channel;
    }

}
