package com.yangxin.simplerpc.provider;

/**
 * @author leon on 2018/8/7.
 * @version 1.0
 * @Description: ServiceExporter将服务发布到网络环境，负责响应&解析Request、编码&发送Response。
 * 总而言之就是负责网络边界处理，不负责Request的执行。
 */
public interface ServiceExporter {

    /***
    * @Description: 发布服务到网络环境
    * @Param: []
    * @return: void
    */
    void export();

    /*** 
    * @Description: 具体查找Provider、执行Request、调用业务代码、产生Response的功能被委托给ProviderManager。
    * @Param: [] 
    * @return: void  
    */ 
    void setProviderManager();
}
