package com.tommyatkins.test.concurrent.countdown;

import java.util.concurrent.CountDownLatch;

public class Driver { // ...

	private static final int N = 5;

	public static void main(String[] args) throws InterruptedException {
		new Driver().main();
	}

	void main() throws InterruptedException {

		CountDownLatch startSignal = new CountDownLatch(1);

		CountDownLatch doneSignal = new CountDownLatch(N);

		for (int i = 0; i < N; ++i)
			// create and start threads

			new Thread(new Worker(startSignal, doneSignal)).start();

		doSomethingElse("start"); // don't let them run yet

		startSignal.countDown(); // let all threads proceed

		doneSignal.await(); // wait for all to finish
		
		doSomethingElse("finish");


	}

	private void doSomethingElse(String flag) {
		System.out.println(flag);
	}

}

class Worker implements Runnable {

	private final CountDownLatch startSignal;

	private final CountDownLatch doneSignal;

	Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {

		this.startSignal = startSignal;

		this.doneSignal = doneSignal;

	}

	public void run() {

		try {

			startSignal.await();

			doWork();

			doneSignal.countDown();

		} catch (InterruptedException ex) {
		} // return;

	}

	private void doWork() {
		System.out.println(Thread.currentThread().getName() + " working...");
	}

}
