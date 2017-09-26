package com.tommyatkins.test.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisClientTest {

    private JedisPool jedisPool;

    {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(200);
        config.setMaxIdle(10);
        config.setMaxWaitMillis(10000);
        config.setTestOnBorrow(true);
        jedisPool = new JedisPool(config, "192.168.7.254", 6379, 10000);
    }

    public JedisPool getJedisPool() {
        return this.jedisPool;
    }

    public static void main(String[] args) {
        JedisClientTest test = new JedisClientTest();
        JedisPool pool = test.getJedisPool();

        Jedis conn = pool.getResource();
        // conn.set("tommyatkins", "test");
        System.out.println(conn.randomKey());
        conn.del("tommyatkins");
        conn.close();
        System.out.println(111);
    }

}
