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
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client extends Thread {
	private Charset charset = Charset.forName("GBK");
	private ConcurrentLinkedQueue<String> messageQueue;

	public static void main(String[] args) throws IOException, InterruptedException {
		ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<String>();
		Client client = new Client("127.0.0.1", 9999, messageQueue);
		client.new MessageWriter(messageQueue).start();
		client.start();
	}

	private Selector selector;

	public Client(String host, int port, ConcurrentLinkedQueue<String> messageQueue) throws IOException, InterruptedException {
		selector = Selector.open();
		SocketChannel sc = SocketChannel.open(new InetSocketAddress(host, port));
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_WRITE);
		this.messageQueue = messageQueue;
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
								key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
							}
						}
					} else if (key.isWritable()) {
						while (!messageQueue.isEmpty()) {
							String message = messageQueue.poll();
							SocketChannel sc = (SocketChannel) key.channel();
							ByteBuffer b = encode(message);
							sc.write(b);
							System.out.println("Send message: " + message);
						}
						key.interestOps(key.interestOps() | SelectionKey.OP_READ);

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

	private class MessageWriter extends Thread {

		private ConcurrentLinkedQueue<String> messageQueue;
		private Scanner clientWrite = new Scanner(System.in);

		public MessageWriter(ConcurrentLinkedQueue<String> messageQueue) {
			this.messageQueue = messageQueue;
		}

		@Override
		public void run() {
			while (true) {
				System.out.println("Please input your message !");
				String message = clientWrite.nextLine();
				this.messageQueue.offer(message);
				System.out.println("Add message to queue: " + message);
			}
		}
	}
}
