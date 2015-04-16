package com.tommyatkins.algorithm.others;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class QueueTest {

	public static void main(String[] args) throws InterruptedException {
		// normal();
		advance();
	}

	public static void normal() throws InterruptedException {
		AscQueue<Integer> queue = new AscQueue<Integer>(5);
		queue.put(5);
		queue.put(4);
		queue.put(3);
		queue.put(2);
		queue.put(1);
		while (!queue.isEmpty()) {
			System.out.println(queue.take());
		}
	}

	public static void advance() {

		AscQueue<Integer> queue = new AscQueue<Integer>();
		int putThreadNumber = 3, takeThreadNumber = 6;

		for (int i = 0; i < putThreadNumber; i++) {
			final int flag = i;
			createBusiness(queue, (q) -> {
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

			createBusiness(queue, (q) -> {
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
		//business.setDaemon(true);
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