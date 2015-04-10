package com.tommyatkins.test.pools;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.tommyatkins.test.pools.obj.MyObject;

public class MyObjectPool extends GenericObjectPool<MyObject> {

	public MyObjectPool(PooledObjectFactory<MyObject> factory) {
		super(factory);
	}

	public MyObjectPool(PooledObjectFactory<MyObject> factory, GenericObjectPoolConfig config) {
		super(factory, config);
	}

	public MyObjectPool(PooledObjectFactory<MyObject> factory, GenericObjectPoolConfig config, AbandonedConfig abandonedConfig) {
		super(factory, config, abandonedConfig);
	}

	@Override
	public MyObject borrowObject() throws Exception {
		MyObject obj = super.borrowObject();
		System.out.println(String.format("Prepare to borrow out %s", obj.toString()));
		obj.raiseBorrowCount();
		return obj;
	}

	@Override
	public void returnObject(MyObject obj) {
		System.out.println(String.format("Prepare to put back %s", obj.toString()));
		obj.raiseReturnCount();
		super.returnObject(obj);
	}

}
