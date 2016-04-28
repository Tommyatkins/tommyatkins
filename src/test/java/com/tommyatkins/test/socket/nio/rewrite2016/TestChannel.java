package com.tommyatkins.test.socket.nio.rewrite2016;

import com.tommyatkins.test.socket.nio.rewrite2016.server.ServerChannel;

public class TestChannel {

	public static void main(String[] args) {
		new ServerChannel("127.0.0.1", 8888, "GBK").start();
	}

}
