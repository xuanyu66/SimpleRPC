package com.yangxin.simplerpc.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * @ClassName: CustomThreadPoolExecutor
 * @Description: 定制线程池
 * @author leon
 * @date 2018年9月5日
 * 
 */
public class CustomThreadPoolExecutor {

	private static Logger LOGGER = Logger.getLogger(CustomThreadPoolExecutor.class);
	
	private ThreadPoolExecutor poolExecutor = null;
	
	public CustomThreadPoolExecutor init() {
		poolExecutor = new ThreadPoolExecutor(Constant.THREAD_POOL_CORE_POOL_SIZE,
				Constant.THREAD_POOL_MAXIMUM_POOL_SIZE,
				Constant.KEEP_ALIVE_TIME, TimeUnit.MINUTES,
				new ArrayBlockingQueue<>(Constant.BLOCKING_QUEUE_SIZE),
				new CustomThreadFactory(),
				new CustomRejectedExecutionHandler());
		return this;
	}
	
	public void destory() {
		if (poolExecutor != null) {
			poolExecutor.shutdownNow();
		}
	}

	public ThreadPoolExecutor getCustomThreadPoolExecutor() {
		return poolExecutor;
	}
	
	private class CustomThreadFactory implements ThreadFactory{

		private AtomicInteger count = new AtomicInteger(0); 
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			String ThreadName = CustomThreadPoolExecutor.class.getSimpleName() + count.addAndGet(1);
			t.setName(ThreadName);
			return t;
		}
		
	}
	
	private class CustomRejectedExecutionHandler implements RejectedExecutionHandler {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			LOGGER.error("ThreadPool is full, can't execute task" + r.getClass().getSimpleName());
			executor.shutdownNow();
			LOGGER.info("shutdown the customThreadPoolExecutor now.");
		}
		
	}

	
}
