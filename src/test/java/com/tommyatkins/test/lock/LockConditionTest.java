package com.tommyatkins.test.lock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockConditionTest {

	public static void main(String[] args) {
		int takeCount = 1;
		int putCount = 1;
		int capacity = 10;
		new Task().run(takeCount, putCount, capacity);

	}

}

class Task {
	public void run(int takeCount, int putCount, int capacity) {
		ReentrantLock takeLock = new ReentrantLock();
		ReentrantLock putLock = new ReentrantLock();

		Condition full = takeLock.newCondition();
		Condition empty = putLock.newCondition();

		AtomicInteger count = new AtomicInteger(0);
		AtomicBoolean takeModel = new AtomicBoolean(false);

		for (int i = 0; i < takeCount; i++) {
			Thread takeThread = new Thread(new Take(takeLock, putLock, full, empty, count, takeModel, capacity));
			takeThread.setName(String.format("Take-Thread-%d", i));
			takeThread.start();
		}

		for (int i = 0; i < putCount; i++) {
			Thread putThread = new Thread(new Put(takeLock, putLock, full, empty, count, takeModel, capacity));
			putThread.setName(String.format("Put-Thread-%d", i));
			putThread.start();
		}
	}
}

abstract class CommonPropertiesThread implements Runnable {

	protected ReentrantLock takeLock;
	protected ReentrantLock putLock;

	protected Condition full;
	protected Condition empty;

	protected AtomicInteger count;
	protected AtomicBoolean takeModel;

	protected int capacity;

	public CommonPropertiesThread(ReentrantLock takeLock, ReentrantLock putLock, Condition full, Condition empty, AtomicInteger count,
			AtomicBoolean takeModel, int capacity) {
		this.takeLock = takeLock;
		this.putLock = putLock;
		this.full = full;
		this.empty = empty;
		this.count = count;
		this.takeModel = takeModel;
		this.capacity = capacity;
	}

	protected String getSelfName() {
		return Thread.currentThread().getName();
	}

}

class Take extends CommonPropertiesThread {

	public Take(ReentrantLock takeLock, ReentrantLock putLock, Condition full, Condition empty, AtomicInteger count, AtomicBoolean takeModel,
			int capacity) {
		super(takeLock, putLock, full, empty, count, takeModel, capacity);
	}

	@Override
	public void run() {
		while (true) {
			try {
				takeLock.lockInterruptibly();
				while (!takeModel.get()) {
					full.await();
				}

				if (count.get() > 0) {
					System.out.println(String.format("%s, count = %d", getSelfName(), count.getAndDecrement() - 1));
					full.signal();
					full.await();
				} else {
					takeModel.set(false);
					signalAllPutThread();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				takeLock.unlock();
			}
		}
	}

	protected void signalAllPutThread() {
		try {
			putLock.lock();
			empty.signalAll();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			putLock.unlock();
		}
	}
}

class Put extends CommonPropertiesThread {

	public Put(ReentrantLock takeLock, ReentrantLock putLock, Condition full, Condition empty, AtomicInteger count, AtomicBoolean takeModel,
			int capacity) {
		super(takeLock, putLock, full, empty, count, takeModel, capacity);
	}

	@Override
	public void run() {
		while (true) {
			try {
				putLock.lockInterruptibly();
				while (takeModel.get()) {
					empty.await();
				}

				if (count.get() < capacity) {
					System.out.println(String.format("%s, count = %d", getSelfName(), count.getAndIncrement() + 1));
					empty.signal();
					empty.await();
				} else {
					takeModel.set(true);
					signalAllTakeThread();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				putLock.unlock();
			}

		}
	}

	protected void signalAllTakeThread() {
		try {
			takeLock.lock();
			full.signalAll();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			takeLock.unlock();
		}
	}
}
