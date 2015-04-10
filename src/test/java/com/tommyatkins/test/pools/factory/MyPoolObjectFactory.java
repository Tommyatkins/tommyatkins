package com.tommyatkins.test.pools.factory;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.tommyatkins.test.pools.obj.MyObject;

public class MyPoolObjectFactory implements PooledObjectFactory<MyObject> {
	private final String host;
	private final int port;
	private AtomicInteger count = new AtomicInteger(0);

	public MyPoolObjectFactory(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public PooledObject<MyObject> makeObject() throws Exception {

		MyObject obj = new MyObject(String.valueOf(count.incrementAndGet()));
		obj.setHost(this.host);
		obj.setPort(this.port);
		System.out.println(String.format("%s had been create.", obj.toString()));
		return new DefaultPooledObject<MyObject>(obj);
	}

	@Override
	public void destroyObject(PooledObject<MyObject> p) throws Exception {
		System.out.println(String.format("%s had been destroy.", p.getObject().toString()));
	}

	@Override
	public boolean validateObject(PooledObject<MyObject> p) {
		return p.getObject().isValid();
	}

	@Override
	public void activateObject(PooledObject<MyObject> p) throws Exception {
		p.getObject().activate();
	}

	@Override
	public void passivateObject(PooledObject<MyObject> p) throws Exception {
		p.getObject().passivate();
	}

}
