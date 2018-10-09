package com.yangxin.simplerpc.protocol;

import java.util.Map;

/**
 * @author leon on 2018/8/8.
 * @version 1.0
 * @Description: 定义响应对象的基本方法
 */
public interface Response {

    String getRequestId();

    Throwable getThrowable();

    Object getResult();

    Map<String, String> getAttachments();

    String getAttachment(String key);

    String getAttachment(String key, String defaultValue);

    void setAttachment(String key, String value);
}
