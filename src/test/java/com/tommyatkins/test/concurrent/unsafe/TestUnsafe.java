package com.tommyatkins.test.concurrent.unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;

import sun.misc.Unsafe;

public class TestUnsafe {

    private static final Unsafe UNSAFE;
    static {
        UNSAFE = getUnsafe();
    }

    public static void main(String[] args)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        copyMemory();
    }

    public static void copyMemory()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        String abc = "abc", efg = "efg";
        System.out.println("abc = " + abc);
        System.out.println("efg = " + efg);

        Field f = String.class.getDeclaredField("value");
        f.setAccessible(true);
        long offect = UNSAFE.objectFieldOffset(f);
        System.out.println("offect=" + offect);
        char[] abcChars = (char[]) f.get(abc);
        char[] efgChars = (char[]) f.get(efg);
        int arrayLong = abc.toCharArray().length;
        int baseOffset = Unsafe.ARRAY_CHAR_BASE_OFFSET;
        UNSAFE.copyMemory(abcChars, baseOffset, efgChars, baseOffset, arrayLong << 1);

        System.out.println(Arrays.toString(abcChars));
        System.out.println(Arrays.toString(efgChars));

        System.out.println("abc = " + abc);
        System.out.println("efg = " + efg);

    }

    public static void manageMemory() {
        long temp = 0L;
        try {
            long size = 8;
            final long addr = temp = UNSAFE.allocateMemory(size);
            System.out.println(addr);
            System.out.println(Long.toBinaryString(UNSAFE.getLong(addr)));
            for (int i = 0; i < size; i++) {
                UNSAFE.putByte(addr + i, (byte) 0x0);
            }
            System.out.println(Long.toBinaryString(UNSAFE.getLong(addr)));
            for (int i = 0; i < size; i++) {
                UNSAFE.putByte(addr + i, (byte) 0xF0);
            }
            System.out.println(Long.toBinaryString(UNSAFE.getLong(addr)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            UNSAFE.freeMemory(temp);
        }
    }

    public static void putByte() {
        final long[] ar = new long[1000];
        final int index = ar.length - 1;
        ar[index] = -1; // FFFF FFFF FFFF FFFF
        System.out.println("Before change = " + Long.toHexString(ar[index]));
        int baseOffset = UNSAFE.arrayBaseOffset(long[].class);
        int indexScale = UNSAFE.arrayIndexScale(long[].class);
        System.out.println("baseOffset: " + baseOffset);
        for (long i = 0; i < 8; ++i) {
            UNSAFE.putByte(ar, baseOffset + indexScale * index + i, (byte) 0);
            System.out.println("After change: i = " + i + ", val = " + Long.toHexString(ar[index]));
        }
    }

    public static void setValueTest() {
        Log log = new Log();
        Unsafe unsafe = UNSAFE;
        // 这个很牛
        // unsafe.allocateMemory(1000000000l);

        Class<AnalyzedTarget> clazz = AnalyzedTarget.class;
        Field[] fields = clazz.getDeclaredFields();
        log.info("fieldName:fieldOffset");
        // 获取属性偏移量，可以通过这个偏移量给属性设置
        for (Field f : fields) {
            log.info(f.getName() + ":" + unsafe.objectFieldOffset(f));
        }
        // arg0, arg1, arg2, arg3 分别是目标对象实例，目标对象属性偏移量，当前预期值，要设的值
        // unsafe.compareAndSwapInt(arg0, arg1, arg2, arg3)
        AnalyzedTarget target = new AnalyzedTarget();
        // 偏移量编译后一般不会变的,intParam这个属性的偏移量
        long intParamOffset = 24;
        // 给它设置,返回true表明设置成功, 基于有名的CAS算法的方法，并发包用这个方法很多
        log.info(unsafe.compareAndSwapInt(target, intParamOffset, 0, 3));
        // 比较失败
        log.info(unsafe.compareAndSwapInt(target, intParamOffset, 0, 10));
        // 验证下上面是否设置成功,应该还是3，返回ture说明上面没该
        log.info(unsafe.compareAndSwapInt(target, intParamOffset, 3, 10));
    }

    public static Unsafe getUnsafe() {
        Unsafe unsafe = null;
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe"); // Internal reference
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            // TODO: handle exception
        }

        return unsafe;
    }
}


/*
 * Copyright 2010, the original author or authors. All rights reserved.
 */
class AnalyzedTarget {

    private byte byteParam;

    private char charParam;

    private short shorteParam;

    private int intParam;

    private int intParam2;

    public byte getByteParam() {
        return byteParam;
    }

    public char getCharParam() {
        return charParam;
    }

    public short getShorteParam() {
        return shorteParam;
    }

    public int getIntParam() {
        return intParam;
    }

    public int getIntParam2() {
        return intParam2;
    }

    public long getLongParam() {
        return longParam;
    }

    public double getDoubleParam() {
        return doubleParam;
    }

    public String getStrParam() {
        return strParam;
    }

    public String getStrParam2() {
        return strParam2;
    }

    private long longParam;

    private double doubleParam;

    private String strParam;

    private String strParam2;

}


class Log {
    void info(Object m) {
        System.out.println(m);
    }
}
