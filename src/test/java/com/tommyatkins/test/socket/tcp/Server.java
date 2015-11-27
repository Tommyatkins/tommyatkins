package com.tommyatkins.test.socket.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class Server extends Thread {

	private final Selector selector;
	private Charset charset = Charset.forName("GBK");

	public static void main(String[] args) throws IOException {
		new Server(9999).start();
	}

	public Server(int port) throws IOException {
		selector = Selector.open();
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.socket().bind(new InetSocketAddress("127.0.0.1", port));
		ssc.register(selector, SelectionKey.OP_ACCEPT);
	}

	@Override
	public void run() {
		System.out.println("------------server start------------");
		while (true) {
			SelectionKey key = null;
			try {
				selector.select();
				Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
				while (keysIterator.hasNext()) {
					key = (SelectionKey) keysIterator.next();
					keysIterator.remove();
					if (key.isAcceptable()) {
						System.out.println("server.isAcceptable");
						ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
						SocketChannel sc = ssc.accept();
						sc.configureBlocking(false);
						ByteBuffer b = ByteBuffer.allocate(1024);
						b.limit(b.capacity());
						sc.register(selector, SelectionKey.OP_READ, b);
					} else if (!key.isValid()) {
						SocketChannel sc = (SocketChannel) key.channel();
						System.out.println(sc.getRemoteAddress() + " is invalid or can not be connected");
						sc.close();
					} else if (key.isReadable()) {
						SocketChannel sc = (SocketChannel) key.channel();
						ByteBuffer b = (ByteBuffer) key.attachment();
						if (sc.read(b) == -1) {
							System.out.println("end of content, cilent has disconnect this request!");
							key.cancel();
						} else {
							b.flip();
							System.out.println("client.isReadable: ");
							String message = decode(b);
							System.out.println("receive from device: " + message);
							key.attach(encode(message));
							key.interestOps(SelectionKey.OP_WRITE);
						}

					} else if (key.isWritable()) {
						try {
							System.out.println("client.isWritable: ");
							SocketChannel sc = (SocketChannel) key.channel();
							ByteBuffer receiveBuffer = (ByteBuffer) key.attachment();
							String message = decode(receiveBuffer);
							if ("exit".equals(message)) {
								System.out.println("client ask server to disconnect the request: " + sc.getRemoteAddress());
								ByteBuffer b = encode("exit");
								sc.write(b);
								//key.cancel();
							} else {
								ByteBuffer b = encode("服务器已经接收到客户端消息：“" + message + "”");
								System.out.println("return to device: " + decode(b));
								b.flip();
								sc.write(b);
							}
							key.attach(ByteBuffer.allocate(1024));
							key.interestOps(SelectionKey.OP_READ);
						} catch (IOException e) {
							e.printStackTrace();
							key.cancel();
						}

					} else {
						System.out.println("Connectable:" + key.isConnectable());
						System.out.println("Valid:" + key.isValid());
						key.cancel();
					}
				}
			} catch (Exception e) {
				if (key != null) {
					System.out.println("Connectable:" + key.isConnectable());
					System.out.println("Valid:" + key.isValid());
					SocketChannel sc = (SocketChannel) key.channel();
					key.cancel();
					try {
						sc.close();
					} catch (Exception ee) {
						//Ignore
					}
				}
				e.printStackTrace();

			}

		}

	}

	/* 编码过程 */
	public ByteBuffer encode(String str) {
		return charset.encode(str);
	}

	/* 解码过程 */
	public String decode(ByteBuffer bb) {
		return charset.decode(bb).toString();
	}

}
