package com.tommyatkins.test.concurrent.exchanger;

import java.util.concurrent.Exchanger;

public class FillAndEmpty {

	public static void main(String[] args) {
		new FillAndEmpty().start();
		// TEST
	}

	Exchanger<DataBuffer> exchanger = new Exchanger<DataBuffer>();

	DataBuffer initialEmptyBuffer = new DataBuffer("NO.1").clear();

	DataBuffer initialFullBuffer = new DataBuffer("NO.2").fill();

	class FillingLoop implements Runnable {

		public void run() {

			DataBuffer currentBuffer = initialEmptyBuffer;

			try {
				while (currentBuffer != null) {
					addToBuffer(currentBuffer);
					System.out.println(currentBuffer.getName() + " had been fill, start to exchange the empty one.");
					currentBuffer = (DataBuffer) exchanger.exchange(currentBuffer);
					if (currentBuffer.empty()) {
						System.out.println("FillingLoop receive a full buffer of " + currentBuffer.getName());
					} else {
						throw new RuntimeException("FillingLoop had got an error buffer!");
					}
				}

			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

		}

		private void addToBuffer(DataBuffer currentBuffer) {
			currentBuffer.fill();
			System.out.println("FillingLoop fill buffer " + currentBuffer.getName());
		}
	}

	class EmptyingLoop implements Runnable {

		public void run() {

			DataBuffer currentBuffer = initialFullBuffer;
			try {
				while (currentBuffer != null) {
					takeFromBuffer(currentBuffer);
					System.out.println(currentBuffer.getName() + " had been clear, start to exchange the full one.");
					currentBuffer = (DataBuffer) exchanger.exchange(currentBuffer);
					if (currentBuffer.full()) {
						System.out.println("EmptyingLoop receive an empty buffer of " + currentBuffer.getName());
					} else {
						throw new RuntimeException("EmptyingLoop had got an error buffer!");
					}
				}

			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

		}

		private void takeFromBuffer(DataBuffer currentBuffer) {
			currentBuffer.clear();
			System.out.println("EmptyingLoop take buffer from " + currentBuffer.getName());
		}
	}

	void start() {

		new Thread(new FillingLoop()).start();

		new Thread(new EmptyingLoop()).start();

	}

	class DataBuffer {
		private String name;

		private boolean isEmpty = true;

		private String value;

		public DataBuffer fill() {
			this.isEmpty = false;
			return this;
		}

		public DataBuffer clear() {
			this.isEmpty = true;
			return this;
		}

		public String getName() {
			return this.name;
		}

		public DataBuffer(String name) {
			this.name = name;
		}

		public boolean empty() {
			return isEmpty;
		}

		public boolean full() {
			return !isEmpty;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

	}
}
