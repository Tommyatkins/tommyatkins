package com.tommyatkins.test.socket.nio.rewrite.factory;

import java.util.UUID;
import java.util.concurrent.ThreadFactory;

public class NioSocketThreadFactory implements ThreadFactory {

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, UUID.randomUUID().toString());
	}

}
