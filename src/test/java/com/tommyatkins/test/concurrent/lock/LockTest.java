package com.tommyatkins.test.concurrent.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockTest {

	public static void main(String[] args) throws InterruptedException {
		ReentrantLock lock = new ReentrantLock();
		lock.lock();
		Thread t1 = new Thread(new LockRunnable(lock));
		t1.start();
		TimeUnit.SECONDS.sleep(1);
		//t1.interrupt();
		TimeUnit.SECONDS.sleep(5);
		System.out.println("t1.isInterrupted(): "+ t1.isInterrupted());
		lock.unlock();
	}

}

class LockRunnable implements Runnable {
	private Lock lock;

	public LockRunnable(Lock lock) {
		this.lock = lock;
	}

	@Override
	public void run() {
		try {
			System.out.println(lock.tryLock(3, TimeUnit.SECONDS));
			//lock.lock();
			//lock.lockInterruptibly();
			String name = Thread.currentThread().getName();
			System.out.println(String.format("%s running...", name));
			TimeUnit.SECONDS.sleep(3);
			System.out.println(String.format("%s finished...", name));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	};
}
