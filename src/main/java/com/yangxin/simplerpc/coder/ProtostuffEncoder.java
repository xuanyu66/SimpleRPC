package com.yangxin.simplerpc.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author leon on 2018/9/26.
 * @version 1.0
 * @Description:
 */
public class ProtostuffEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public ProtostuffEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            byte[] data = ProtostuffUtil.serializer(o);
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }
    }
}
