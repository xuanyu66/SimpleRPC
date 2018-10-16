package com.yangxin.simplerpc.rpcclient;

import com.yangxin.simplerpc.protocol.RequestMessage;
import com.yangxin.simplerpc.protocol.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author leon on 2018/10/16.
 * @version 1.0
 * @description:
 */
public class RpcFuture implements Future<Object> {
    private static final Logger logger = LoggerFactory.getLogger(RpcFuture.class);

    private RequestMessage request;
    private ResponseMessage response;
    private long startTime;
    private static final long RESPONSE_TIME_THRESHOLD = 5000;

    private ReentrantLock lock = new ReentrantLock();
    private CountDownLatch latch = new CountDownLatch(1);
    private List<RpcCallback> pendingCallbacks = new ArrayList<>();

    public RpcFuture(RequestMessage request) {
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }

    public void done(ResponseMessage response) {
        this.response = response;
        latch.countDown();
        invokeCallbacks();
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > RESPONSE_TIME_THRESHOLD) {
            logger.warn("Service response time is too slow. Request id = " + response.getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final RpcCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    private void runCallback(final  RpcCallback callback) {
        final ResponseMessage response = this.response;
        RpcClient.submit( ()->{
            if (response.getThrowable() != null) {
                callback.fail(new RuntimeException("Response error", response.getThrowable()));
            } else {
                callback.success(response.getResult());
            }
        });
    }

    public RpcFuture addCallback(RpcCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        latch.await();
        if (this.response != null){
            return this.response.getResult();
        } else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = latch.await(timeout, unit);
        if (success) {
            if (this.response != null){
                return this.response.getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.request.getRequestId()
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
    }

}
