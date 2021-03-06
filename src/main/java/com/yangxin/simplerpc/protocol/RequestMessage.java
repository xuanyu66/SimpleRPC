package com.yangxin.simplerpc.protocol;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author leon on 2018/8/8.
 * @version 1.0
 * @Description: 定义默认的请求消息对象格式
 */
public class RequestMessage implements Serializable, Request {

    private static final long serialVersionUID = 18511803622L;

    private String requestId;
    private String className;
    private String methodName;
    private Object[] arguments;
    private Class<?>[] parameterTypes;
    //请求类型
    private byte type;
    private Map<String, String> attachments;

    @Override
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return "RequestMessage{" +
                "requestId='" + requestId + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", arguments=" + Arrays.toString(arguments) +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", type=" + type +
                ", attachments=" + attachments +
                '}';
    }

    public void setClassName(String className) {
        this.className= className;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return this.parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    @Override
    public Map<String, String> getAttachments() {
        return attachments != null ? attachments : Collections.emptyMap();
    }

    @Override
    public String getAttachment(String key) {
        return attachments.get(key);
    }

    @Override
    public String getAttachment(String key, String defaultValue) {
        return attachments.getOrDefault(key, defaultValue);
    }

    @Override
    public void setAttachment(String key, String value) {
        if (attachments == null){
            attachments = new HashMap<String, String>(10);
        }
        attachments.put(key, value);
    }
}
