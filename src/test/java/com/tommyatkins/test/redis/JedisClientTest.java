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
		jedisPool = new JedisPool(config, "127.0.0.1", 6379,10000,"rimi");
	}
	
	public JedisPool getJedisPool() {
		return this.jedisPool;
	}
	
	public static void main(String[] args) {
		JedisClientTest test = new JedisClientTest();
		JedisPool pool = test.getJedisPool();
		
		Jedis conn = pool.getResource();
		conn.shutdown();
		pool.returnResource(conn);
	}
	
	
}

