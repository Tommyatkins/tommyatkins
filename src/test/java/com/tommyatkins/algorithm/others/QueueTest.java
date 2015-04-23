package com.tommyatkins.algorithm.others;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class QueueTest {

	public static void main(String[] args) throws InterruptedException {
		 normal();
		// advance();
		// advanceDefault();
		// logicConfirm();
	}

	public static void logicConfirm() {

		AscQueue<Integer> queue = new AscQueue<Integer>();
		// final ReentrantLock lock = new ReentrantLock();
		// final Condition condition = lock.newCondition();
		try {
			int takeThreadNum = 5;
			for (int i = 0; i < takeThreadNum; i++) {
				createBusiness(queue, (q) -> {
					// lock.lock();
						try {
							// condition.await();
						System.out.println(q.take());
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						// lock.unlock();
					}
				});
			}
			createBusiness(queue, (q) -> {
				// lock.lock();
					try {
						q.put(1);
						q.put(2);
						// condition.signalAll();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					// lock.unlock();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("main - end");

	}

	public static void normal() throws InterruptedException {
		int total = 50;
		AscQueue<Integer> queue = new AscQueue<Integer>(total);
		for (int i = 1; i <= total; i++) {
			queue.put(i);
		}
		int threadSize = 2;
		for (int i = 0; i < threadSize; i++) {
			createBusiness(queue, (p) -> {
				while (!p.isEmpty()) {
					try {
						p.take();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public static void advance() {

		AscQueue<Integer> queue = new AscQueue<Integer>();
		int putThreadNumber = 3, takeThreadNumber = 3;

		final int loop = 100;

		for (int i = 0; i < putThreadNumber; i++) {
			final int flag = i;
			createBusiness(queue, (q) -> {
				try {
					for (int k = 0; k < loop; k++) {
						TimeUnit.SECONDS.sleep(flag);
						for (int j = 0; j < flag; j++) {
							int num = (int) ((flag + Math.random()) * 10);
							System.out.println(String.format("%s put %d", Thread.currentThread().getName(), num));
							q.put(num);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		for (int i = 0; i < takeThreadNumber; i++) {
			final int flag = i;
			createBusiness(queue, (q) -> {
				try {
					for (int k = 0; k < loop; k++) {
						TimeUnit.SECONDS.sleep(flag);
						for (int j = 0; j < flag; j++) {
							int num = q.take();
							System.out.println(String.format("%s take %d", Thread.currentThread().getName(), num));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

	}

	public static void advanceDefault() {

		LinkedBlockingDeque<Integer> queue = new LinkedBlockingDeque<Integer>();
		int putThreadNumber = 3, takeThreadNumber = 6;

		for (int i = 0; i < putThreadNumber; i++) {
			final int flag = i;
			createDefaultBusiness(queue, (q) -> {
				try {
					while (true) {
						TimeUnit.SECONDS.sleep(flag);
						for (int j = 0; j < flag; j++) {
							int num = (int) ((flag + Math.random()) * 10);
							System.out.println(String.format("%s put %d", Thread.currentThread().getName(), num));
							q.put(num);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		for (int i = 0; i < takeThreadNumber; i++) {

			createDefaultBusiness(queue, (q) -> {
				try {
					while (true) {
						TimeUnit.SECONDS.sleep(1);
						System.out.println(String.format("%s take %d", Thread.currentThread().getName(), q.take()));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

	}

	public static void createBusiness(AscQueue<Integer> queue, Consumer<AscQueue<Integer>> consumer) {
		Thread business = new Thread(new QueueBusiness(queue, consumer));
		// business.setDaemon(true);
		business.start();
	}

	public static void createDefaultBusiness(LinkedBlockingDeque<Integer> queue, Consumer<LinkedBlockingDeque<Integer>> consumer) {
		Thread business = new Thread(new DefaultQueueBusiness(queue, consumer));
		// business.setDaemon(true);
		business.start();
	}

}

class QueueBusiness implements Runnable {

	private AscQueue<Integer> queue;
	private Consumer<AscQueue<Integer>> consumer;

	public QueueBusiness(AscQueue<Integer> queue, Consumer<AscQueue<Integer>> consumer) {
		this.queue = queue;
		this.consumer = consumer;
	}

	@Override
	public void run() {
		consumer.accept(queue);
	}
}

class DefaultQueueBusiness implements Runnable {

	private LinkedBlockingDeque<Integer> queue;
	private Consumer<LinkedBlockingDeque<Integer>> consumer;

	public DefaultQueueBusiness(LinkedBlockingDeque<Integer> queue, Consumer<LinkedBlockingDeque<Integer>> consumer) {
		this.queue = queue;
		this.consumer = consumer;
	}

	@Override
	public void run() {
		consumer.accept(queue);
	}
}