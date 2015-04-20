package com.tommyatkins.algorithm.others;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AscQueue<E extends Comparable<E>> {

	transient AscNode<E> head;

	private transient AscNode<E> last;

	private final AtomicInteger count = new AtomicInteger();

	private final ReentrantLock takeLock = new ReentrantLock();

	private final Condition notEmpty = takeLock.newCondition();

	private final ReentrantLock putLock = new ReentrantLock();

	private final Condition notFull = putLock.newCondition();

	private final int capacity;

	public AscQueue() {
		this(Integer.MAX_VALUE);
	}

	public AscQueue(int capacity) {
		this.capacity = capacity;
	}

	static class AscNode<E> {
		E item;
		AscNode<E> next;

		AscNode(E x) {
			item = x;
		}
	}

	public void put(E entity) throws InterruptedException {
		Objects.requireNonNull(entity);
		putLock.lockInterruptibly();
		int c = -1;
		try {
			// wait until count < capacity
			while (count.get() == capacity) {
				notFull.await();
			}
			enqueueASC(entity);

			c = count.getAndIncrement();

			// 在put的过程中存在take的动作，put完之后确定如果有被take过，则唤醒其他等待中的线程继续进行enqueue动作。
			if (c + 1 < capacity)
				notFull.signal();

		} finally {
			putLock.unlock();
		}
		if (c == 0) {
			// 通知可以take
			this.signalNotEmpty();
		}
	};

	public E take() throws InterruptedException {
		takeLock.lockInterruptibly();
		E entity = null;
		int c = -1;
		try {
			if (count.get() == 0)
				notEmpty.await();

			entity = dequeue();
			c = count.getAndDecrement();
			if (c - 1 > 0) {
				notEmpty.signal();
			}

		} finally {
			takeLock.unlock();
		}
		if (c == capacity) {
			// 通知可以put
			this.signalNotFull();
		}
		return entity;
	}

	public E last() {
		return last == null ? null : last.item;
	}

	public boolean isEmpty() {
		return last == null;
	}

	private void signalNotEmpty() {
		final ReentrantLock takeLock = this.takeLock;
		takeLock.lock();
		try {
			notEmpty.signal();
		} finally {
			takeLock.unlock();
		}
	}

	private void signalNotFull() {
		final ReentrantLock putLock = this.putLock;
		putLock.lock();
		try {
			notFull.signal();
		} finally {
			putLock.unlock();
		}
	}

	private final void enqueueASC(E entity) {
		AscNode<E> entityNode = new AscNode<E>(entity);
		if (head == null) {
			// first element
			last = head = entityNode;
		} else {
			AscNode<E> current = head;
			int result = entity.compareTo(current.item);
			if (result < 0) {
				AscNode<E> preHead = head;
				head = entityNode;
				head.next = preHead;
			} else {
				do {
					AscNode<E> previous = current;
					current = current.next;

					if (current == null) {
						last = current = previous.next = entityNode;
					} else {
						int r = entity.compareTo(current.item);
						if (r < 0) {
							entityNode.next = previous.next;
							current = previous.next = entityNode;
						} else {
						}
					}
				} while (current != entityNode);
			}

		}
	}

	private final E dequeue() {
		AscNode<E> node = head;
		head = head.next;
		E entity = node.item;
		node.item = null; // help GC
		node.next = node;// help GC
		if (head == null) {
			last = null;
		}
		return entity;
	}

}
