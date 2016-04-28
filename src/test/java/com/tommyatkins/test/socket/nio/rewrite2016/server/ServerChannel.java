package com.tommyatkins.test.socket.nio.rewrite2016.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class ServerChannel extends Thread {

	private Selector selector;
	private String host;
	private int port;
	private boolean keep_working = true;
	private Charset charset;

	public ServerChannel(String host, int port, String charsetName) {
		this.host = host;
		this.port = port;
		this.charset = Charset.forName(charsetName);
	}

	private void listenStart(Selector selector) throws IOException {
		while (keep_working) {

			if (selector.select() > 0) {
				Set<SelectionKey> selectedKeySet = selector.selectedKeys();
				Iterator<SelectionKey> selectedKeys = selectedKeySet.iterator();
				try {
					if (selectedKeys.hasNext()) {
						SelectionKey key = selectedKeys.next();
						System.out.println("#####################");
						System.out.println("isValid: " + key.isValid());
						System.out.println("isConnectable: " + key.isConnectable());
						System.out.println("#####################");
						if (key.isAcceptable()) {
							System.out.println("--------------isAcceptable--------------");
							ServerSocketChannel channel = (ServerSocketChannel) key.channel();
							SocketChannel clientChannel = channel.accept();
							clientChannel.configureBlocking(false);
							clientChannel.write(charset.encode("中文我感觉看不到。"));
							clientChannel.register(selector, SelectionKey.OP_READ);
						} else if (key.isReadable()) {
							System.out.println("--------------isReadable--------------");
							SocketChannel clientChannel = (SocketChannel) key.channel();
							ByteBuffer bf = ByteBuffer.allocate(1024);
							clientChannel.read(bf);
							bf.flip();
							System.out.println("read: " + charset.decode(bf));
							//clientChannel.close();
						} else if (key.isWritable()) {
							System.out.println("--------------isWritable--------------");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					selectedKeys.remove();
				}

			}

		}
	}

	public void shutDown() {
		this.keep_working = false;
	}

	@Override
	public void run() {
		try {
			ServerSocketChannel channel = ServerSocketChannel.open();
			this.selector = Selector.open();
			channel.configureBlocking(false);
			channel.bind(new InetSocketAddress(this.host, this.port));
			channel.register(this.selector, SelectionKey.OP_ACCEPT);
			this.listenStart(this.selector);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
