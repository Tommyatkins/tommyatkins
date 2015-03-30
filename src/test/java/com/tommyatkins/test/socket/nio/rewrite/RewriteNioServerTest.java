package com.tommyatkins.test.socket.nio.rewrite;

import java.io.IOException;

public class RewriteNioServerTest {

	public static void main(String[] args) throws IOException {
		ServerChannel server = new ServerChannel(9999);
		new Thread(server).start();
	}

}
