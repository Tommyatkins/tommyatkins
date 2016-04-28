package com.tommyatkins.test.socket.nio.rewrite2016.client;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ClientChannel extends Thread {

	
	
	public ClientChannel(String host, int port) throws IOException {
		SocketChannel.open();
	}

	@Override
	public void run() {
	}

}
