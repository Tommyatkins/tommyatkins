package com.tommyatkins.test.socket.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;

public class Client extends Thread {
	private Charset charset = Charset.forName("GBK");
	private Scanner clientWrite = new Scanner(System.in);

	public static void main(String[] args) throws IOException, InterruptedException {
		new Client("127.0.0.1", 9999).start();
	}

	private Selector selector;

	public Client(String host, int port) throws IOException, InterruptedException {
		selector = Selector.open();
		SocketChannel sc = SocketChannel.open(new InetSocketAddress(host, port));
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_WRITE);
	}

	/* 编码过程 */
	public ByteBuffer encode(String str) {
		return charset.encode(str);
	}

	/* 解码过程 */
	public String decode(ByteBuffer bb) {
		return charset.decode(bb).toString();
	}

	@Override
	public void run() {
		boolean keep = true;
		while (keep) {
			SelectionKey key = null;
			try {
				selector.select();
				Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
				while (keysIterator.hasNext()) {
					key = keysIterator.next();
					if (key.isConnectable()) {
						System.out.println("client.isConnectable");
					} else if (key.isReadable()) {
						SocketChannel sc = (SocketChannel) key.channel();
						System.out.println("client.isReadable");
						ByteBuffer b = ByteBuffer.allocate(1024);
						if (sc.read(b) == -1) {
							System.out.println("server had disconnect the request and we shall canele it :" + sc.getLocalAddress());
							// key.cancel();
							sc.close();
							clientWrite.close();
							keep = false;
						} else {
							b.flip();
							String retMessage = decode(b);
							if ("exit".equals(retMessage)) {
								System.out.println("server allow client to exit");
								// key.cancel();
								sc.close();
								keep = false;
							} else {
								System.out.println("return:" + retMessage);
								key.interestOps(SelectionKey.OP_WRITE);
							}
						}
					} else if (key.isWritable()) {
						System.out.println("client.isWritable");
						System.out.print("请输入你要发送的内容：");
						String message = clientWrite.nextLine();
						SocketChannel sc = (SocketChannel) key.channel();
						ByteBuffer b = encode(message);
						sc.write(b);
						if (message == null || message.isEmpty()) {
							
						} else {
							key.interestOps(SelectionKey.OP_READ);
						}
					} else {
						System.out.println("Connectable:" + key.isConnectable());
						System.out.println("Valid:" + key.isValid());
						key.cancel();
					}
					keysIterator.remove();
				}
			} catch (Exception e) {
				if (key != null) {
					System.out.println("Connectable:" + key.isConnectable());
					System.out.println("Valid:" + key.isValid());
					key.cancel();
				}
				e.printStackTrace();
			}

		}
	}
}
