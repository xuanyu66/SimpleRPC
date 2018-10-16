package com.yangxin.simplerpc.rpcclient;

import com.yangxin.simplerpc.coder.ProtostuffDecoder;
import com.yangxin.simplerpc.coder.ProtostuffEncoder;
import com.yangxin.simplerpc.protocol.RequestMessage;
import com.yangxin.simplerpc.protocol.ResponseMessage;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author leon on 2018/10/12.
 * @version 1.0
 * @description:
 */
public class RpcClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                .addLast(new ProtostuffEncoder(RequestMessage.class))
                .addLast(new ProtostuffDecoder(ResponseMessage.class))
                .addLast(new RpcClientHandler());
    }
}
