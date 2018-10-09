package com.yangxin.simplerpc.rpcserver;

import com.yangxin.simplerpc.protocol.RequestMessage;
import com.yangxin.simplerpc.protocol.ResponseMessage;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Map;


/**
 * @author leon on 2018/9/26.
 * @version 1.0
 * @Description:
 */
public class RpcHandler extends SimpleChannelInboundHandler<RequestMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);


    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage request) throws Exception {
        RpcServer.submit(() -> {
            LOGGER.info("Receive request: {}", request.toString());
            ResponseMessage response = new ResponseMessage();
            response.setRequestId(request.getRequestId());
            try {
                Object result = handle(request);
                response.setResult(result);
            } catch (Throwable t) {
                LOGGER.error("RPC server Handle request error", t);
                response.setThrowable(t);
            }
            ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    LOGGER.info("Send response for request :{}", request.getRequestId());
                }
            });
        });
    }

    private Object handle(RequestMessage request) throws Throwable{
        String className = request.getClassName();
        LOGGER.info("classname" + className);
        Object serviceBean = handlerMap.get(className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getArguments();

        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName,
                parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }

}
