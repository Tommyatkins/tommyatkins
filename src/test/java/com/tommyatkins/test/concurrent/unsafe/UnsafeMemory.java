package com.tommyatkins.test.concurrent.unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
class UnsafeMemory {
	private static final Unsafe unsafe;
	static {
		unsafe = getUnsafe();
	}

	public static Unsafe getUnsafe() {
		Unsafe unsafe = null;
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe"); // Internal reference
			f.setAccessible(true);
			unsafe = (Unsafe) f.get(null);
		} catch (Exception e) {
		}

		return unsafe;
	}

	private static final long byteArrayOffset = unsafe.arrayBaseOffset(byte[].class);
	private static final long longArrayOffset = unsafe.arrayBaseOffset(long[].class);
	private static final long doubleArrayOffset = unsafe.arrayBaseOffset(double[].class);

	private static final int SIZE_OF_BOOLEAN = 1;
	private static final int SIZE_OF_INT = 4;
	private static final int SIZE_OF_LONG = 8;

	private int pos = 0;
	private final byte[] buffer;

	public UnsafeMemory(final byte[] buffer) {
		if (null == buffer) {
			throw new NullPointerException("buffer cannot be null");
		}

		this.buffer = buffer;
	}

	public void reset() {
		this.pos = 0;
	}

	public void putBoolean(final boolean value) {
		unsafe.putBoolean(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_BOOLEAN;
	}

	public boolean getBoolean() {
		boolean value = unsafe.getBoolean(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_BOOLEAN;

		return value;
	}

	public void putInt(final int value) {
		unsafe.putInt(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_INT;
	}

	public int getInt() {
		int value = unsafe.getInt(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_INT;

		return value;
	}

	public void putLong(final long value) {
		unsafe.putLong(buffer, byteArrayOffset + pos, value);
		pos += SIZE_OF_LONG;
	}

	public long getLong() {
		long value = unsafe.getLong(buffer, byteArrayOffset + pos);
		pos += SIZE_OF_LONG;

		return value;
	}

	public void putLongArray(final long[] values) {
		putInt(values.length);

		long bytesToCopy = values.length << 3;
		unsafe.copyMemory(values, longArrayOffset, buffer, byteArrayOffset + pos, bytesToCopy);
		pos += bytesToCopy;
	}

	public long[] getLongArray() {
		int arraySize = getInt();
		long[] values = new long[arraySize];

		long bytesToCopy = values.length << 3;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values, longArrayOffset, bytesToCopy);
		pos += bytesToCopy;

		return values;
	}

	public void putDoubleArray(final double[] values) {
		putInt(values.length);

		long bytesToCopy = values.length << 3;
		unsafe.copyMemory(values, doubleArrayOffset, buffer, byteArrayOffset + pos, bytesToCopy);
		pos += bytesToCopy;
	}

	public double[] getDoubleArray() {
		int arraySize = getInt();
		double[] values = new double[arraySize];

		long bytesToCopy = values.length << 3;
		unsafe.copyMemory(buffer, byteArrayOffset + pos, values, doubleArrayOffset, bytesToCopy);
		pos += bytesToCopy;

		return values;
	}

	public static void main(String[] args) {
		UnsafeMemory um = new UnsafeMemory(new byte[1024]);
		um.putBoolean(true);
		um.putDoubleArray(new double[] { 1.1d, 2.2d });
		um.putInt(888);
		um.putLong(8888888888888888L);
		um.putLongArray(new long[] { 5, 6 });
		um.reset();
		System.out.println(um.getBoolean());
		System.out.println(Arrays.toString(um.getDoubleArray()));
		System.out.println(um.getInt());
		System.out.println(um.getLong());
		System.out.println(Arrays.toString(um.getLongArray()));
	}

}
