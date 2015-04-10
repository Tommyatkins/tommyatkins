package com.tommyatkins.test.pools.obj;

import java.util.concurrent.atomic.AtomicInteger;

public class MyObject {

	private String name;
	private String status = "new";

	private String host;
	private int port;

	private AtomicInteger borrowCount = new AtomicInteger(0);
	private AtomicInteger returnCount = new AtomicInteger(0);

	private boolean valid = true;

	public MyObject(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getBorrowCount() {
		return this.borrowCount.intValue();
	}

	public int raiseBorrowCount() {
		return this.borrowCount.incrementAndGet();
	}

	public int getReturnCount() {
		return this.returnCount.intValue();
	}

	public int raiseReturnCount() {
		return this.returnCount.incrementAndGet();
	}

	public void activate() {
		this.status = "activate";
	}

	public void passivate() {
		this.status = "passivate";
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return String.format("%s[%s](%d-%d)", this.name, this.status, this.getBorrowCount(), this.getReturnCount());
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

}
