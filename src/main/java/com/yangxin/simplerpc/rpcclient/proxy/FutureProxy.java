package com.yangxin.simplerpc.rpcclient.proxy;

import com.yangxin.simplerpc.protocol.RequestMessage;
import com.yangxin.simplerpc.protocol.ResponseMessage;
import com.yangxin.simplerpc.rpcclient.ConnectionManager;
import com.yangxin.simplerpc.rpcclient.RpcClientHandler;
import com.yangxin.simplerpc.rpcclient.RpcFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author leon on 2018/10/15.
 * @version 1.0
 * @description:
 */
public class FutureProxy implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FutureProxy.class);

    private Class<?> interfaceName;

    public FutureProxy(Class<?> interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RequestMessage request = new RequestMessage();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setArguments(args);

        LOGGER.debug(method.getDeclaringClass().getName());
        LOGGER.debug(method.getName());
        for (int i = 0; i < method.getParameterTypes().length; ++i) {
            LOGGER.debug(method.getParameterTypes()[i].getName());
        }
        for (int i = 0; i < args.length; ++i) {
            LOGGER.debug(args[i].toString());
        }

        RpcClientHandler client = ConnectionManager.getConnectionMananger().chooseHandler();
        RpcFuture future = client.sendRequest(request);
        return future.get();
    }

    public RpcFuture call(String funcName, Object... args) {
        RpcClientHandler handler = ConnectionManager.getConnectionMananger().chooseHandler();
        RequestMessage request = createRequest(this.interfaceName.getName(), funcName, args);
        RpcFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture;
    }

    private RequestMessage createRequest(String className, String methodName, Object[] args) {
        RequestMessage request = new RequestMessage();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setArguments(args);

        Class[] parameterTypes = new Class[args.length];
        /**Get the right class type*/
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(parameterTypes);

        LOGGER.debug(className);
        LOGGER.debug(methodName);
        for (Class parameterType : parameterTypes) {
            LOGGER.debug(parameterType.getName());
        }
        for (Object arg : args) {
            LOGGER.debug(arg.toString());
        }

        return request;
    }


    private Class<?> getClassType(Object obj) {
        Class<?> classType = obj.getClass();
        String typeName = classType.getName();
        switch (typeName) {
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;

                default:
                    return classType;
        }
    }

}
