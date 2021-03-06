package com.yangxin.simplerpc.protocol;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author leon on 2018/8/9.
 * @version 1.0
 * @Description: 定义响应消息格式
 */
public class ResponseMessage implements Response, Serializable {

    private static final long serialVersionUID = 18311317304L;

    private String requestId;
    private Throwable throwable;
    private Object result;
    private Map<String, String> attachments;

    private long processTime;

    @Override
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public Map<String, String> getAttachments() {
        return attachments != null ? attachments : Collections.emptyMap();
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
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
        if (this.attachments == null) {
            this.attachments = new HashMap<String, String>(10);
        }

        this.attachments.put(key, value);
    }

    public void setProcessTime(long processTime) {
        this.processTime = processTime;
    }

    public long getProcessTime() {
        return processTime;
    }


    @Override
    public String toString() {
        return "ResponseMessage{" +
                "requestId='" + requestId + '\'' +
                ", throwable=" + throwable +
                ", result=" + result +
                ", attachments=" + attachments +
                ", processTime=" + processTime +
                '}';
    }
}
