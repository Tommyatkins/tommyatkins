package com.tommyatkins.test.pools;

import java.util.concurrent.CountDownLatch;

import com.tommyatkins.test.pools.config.MyAbandonedConfig;
import com.tommyatkins.test.pools.config.MyPoolConfig;
import com.tommyatkins.test.pools.factory.MyPoolObjectFactory;
import com.tommyatkins.test.pools.obj.MyObject;

public class PoolTest {

    public static void main(String[] args) throws Exception {

        int threadCount = 30;

        final CountDownLatch latch = new CountDownLatch(threadCount);

        MyPoolConfig config = new MyPoolConfig();
        config.setMaxTotal(10);
        config.setMaxIdle(5);
        config.setMaxWaitMillis(5000);

        MyAbandonedConfig abandoneConfig = new MyAbandonedConfig();

        // abandoneConfig.setLogAbandoned(true);
        // abandoneConfig.setLogWriter(new PrintWriter(System.out));

        MyPoolObjectFactory factory = new MyPoolObjectFactory("127.0.0.1", 10086);

        final MyObjectPool pool = new MyObjectPool(factory, config, abandoneConfig);

        for (int i = 0; i < threadCount; i++) {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    try {
                        MyObject obj = pool.borrowObject();
                        System.out.println(String.format("Thread-%s use object %s", Thread.currentThread().getName(),
                                obj.toString()));
                        pool.returnObject(obj);
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            new Thread(r).start();
        }

        latch.await();

        System.out.println("finish");

    }
}
