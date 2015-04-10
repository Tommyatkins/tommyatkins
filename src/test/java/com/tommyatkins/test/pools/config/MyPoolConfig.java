package com.tommyatkins.test.pools.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class MyPoolConfig extends GenericObjectPoolConfig {

	public MyPoolConfig() {
		setTestWhileIdle(true);
		setMinEvictableIdleTimeMillis(60000);
		setTimeBetweenEvictionRunsMillis(30000);
		setNumTestsPerEvictionRun(-1);
	}
}
