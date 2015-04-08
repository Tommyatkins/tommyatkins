package com.tommyatkins.test.concurrent.barrier;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;

public class Solver { // Code sketch

	public static void main(String[] args) {
		Solver solver = new Solver();
		Problem p = solver.new Problem();
		solver.solve(p, 5);
	}

	public void solve(final Problem p, int nThreads) {

		final CyclicBarrier barrier =

		new CyclicBarrier(nThreads,

		new Runnable() {

			public void run() {
				p.checkConvergence();
			}
		}

		);

		for (int i = 0; i < nThreads; ++i) {

			final int id = i;

			Runnable worker = new Runnable() {

				final Segment segment = p.createSegment(id);

				public void run() {

					try {

						while (!p.converged()) {

							segment.update();
							Thread.sleep(id * 1000);
							System.out.println(String.format("segment-%d start to wait for others.", id));
							barrier.await();
						}
					}

					catch (Exception e) {
						return;
					}

				}

			};

			new Thread(worker).start();

		}

	}

	class Problem {

		private CopyOnWriteArrayList<Segment> segments = new CopyOnWriteArrayList<Segment>();

		public void checkConvergence() {
			System.out.println("All the segments had finish their work.");
		}

		public Segment createSegment(int id) {
			Segment segment = new Segment(id);
			segments.add(segment);
			return segment;
		}

		public boolean converged() {
			boolean result = true;
			for (Segment segment : segments) {
				result = result && segment.updated;
			}
			return result;
		}
	}

	class Segment {
		private int id;
		private boolean updated = false;

		public Segment(int id) {
			this.id = id;
		}

		public boolean update() {
			System.out.println(String.format("segment-%d update.", id));
			return this.updated = true;
		}

	}
}
