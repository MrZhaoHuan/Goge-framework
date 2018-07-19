package com.yuyenews.easy.netty.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池
 * @author yuye
 *
 */
public class ThreadPool {

	private static ExecutorService pool = Executors.newCachedThreadPool();
	
	/**
	 * 新增请求线程
	 * @param command
	 */
	public static void execute(Runnable command) {
		pool.execute(command);
	}
}
