package com.tommyatkins.test.socket.nio.rewrite.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.tommyatkins.test.socket.nio.rewrite.factory.NioSocketThreadFactory;

public class ThreadPoolCollection {

	private static ThreadPoolCollection threadPools;

	public static ThreadPoolCollection threadPools() {
		return threadPools(1, 1, 1);
	}

	public static ThreadPoolCollection threadPools(int distributePoolSize, int readPoolSize, int writePoolSize) {
		return threadPools == null ? new ThreadPoolCollection(distributePoolSize, readPoolSize, writePoolSize) : threadPools;
	}

	private ExecutorService clientDistributeThreadPool;
	private ExecutorService clientReaderThreadPool;
	private ExecutorService clientWriterThreadPool;

	public void distribute(Runnable runnable) {
		doWork(threadPools().clientDistributeThreadPool, runnable);
	}

	public void read(Runnable runnable) {
		doWork(threadPools().clientReaderThreadPool, runnable);
	}

	public void write(Runnable runnable) {
		doWork(threadPools().clientWriterThreadPool, runnable);
	}

	private void doWork(ExecutorService executorService, Runnable runnable) {
		executorService.submit(runnable);
	}

	private ThreadPoolCollection(ExecutorService clientDistributeThreadPool, ExecutorService clientReaderThreadPool,
			ExecutorService clientWriterThreadPool) {
		this.clientDistributeThreadPool = clientDistributeThreadPool;
		this.clientReaderThreadPool = clientReaderThreadPool;
		this.clientWriterThreadPool = clientWriterThreadPool;
	}

	private ThreadPoolCollection(int distributePoolSize, int readPoolSize, int writePoolSize) {
		ThreadFactory threadFactory = new NioSocketThreadFactory();
		this.clientDistributeThreadPool = Executors.newFixedThreadPool(distributePoolSize, threadFactory);
		this.clientReaderThreadPool = Executors.newFixedThreadPool(readPoolSize, threadFactory);
		this.clientWriterThreadPool = Executors.newFixedThreadPool(writePoolSize, threadFactory);
	}

	public ExecutorService getClientDistributeThreadPool() {
		return clientDistributeThreadPool;
	}

	public ExecutorService getClientReaderThreadPool() {
		return clientReaderThreadPool;
	}

	public ExecutorService getClientWriterThreadPool() {
		return clientWriterThreadPool;
	}

}
