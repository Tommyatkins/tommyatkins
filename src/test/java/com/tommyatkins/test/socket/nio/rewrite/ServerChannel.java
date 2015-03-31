package com.tommyatkins.test.socket.nio.rewrite;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.tommyatkins.test.socket.nio.rewrite.pool.ThreadPoolCollection;
import com.tommyatkins.test.socket.nio.rewrite.thread.ClientReaderThread;
import com.tommyatkins.test.socket.nio.rewrite.thread.ClientWriterThread;

public class ServerChannel implements Runnable {

	private ServerSocketChannel server;

	private Selector selector;

	private boolean keep_working;

	private ThreadPoolCollection threadPools;

	private String host;

	private int port;

	public ServerChannel(int port) {
		this("127.0.0.1", port);
	}

	public ServerChannel(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public Selector registSelector(String host, int port, Selector selector, Object attach) throws IOException {
		server = ServerSocketChannel.open();
		server.configureBlocking(false);
		server.socket().bind(new InetSocketAddress(host, port));
		server.register(selector, SelectionKey.OP_ACCEPT, attach);
		return selector;
	}

	public void startListener(Selector selector) throws IOException {
		// SelectionKey selectionKey = null;
		while (keep_working) {
			int total = selector.select();
			if (total == 0) {
				continue;
			}
			Set<SelectionKey> selectedKeySet = selector.selectedKeys();
			Iterator<SelectionKey> selectedKeys = selectedKeySet.iterator();
			if (selectedKeys.hasNext()) {
				SelectionKey selectionKey = selectedKeys.next();
				try {
					if (selectionKey.isValid()) {
						if (selectionKey.isAcceptable()) {
							SelectableChannel channel = selectionKey.channel();
							ServerSocketChannel serverSocketChannel = (ServerSocketChannel) channel;
							SocketChannel clientChannel = serverSocketChannel.accept();
							System.out.println("accept:" + clientChannel.getRemoteAddress());
							clientChannel.configureBlocking(false);
							clientChannel.register(selector, SelectionKey.OP_READ);
						} else if (selectionKey.isReadable()) {
							threadPools.read(new ClientReaderThread(selectionKey));
						} else if (selectionKey.isWritable()) {
							threadPools.write(new ClientWriterThread(selectionKey));
						} else {
							// threadPools.distribute(new ClientDistributeThread(selectionKey));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					selectedKeys.remove();
				}
			}

		}
	}

	public void shutdown() {
		keep_working = false;
	}

	public boolean andStatus(int readyOps, int... status) {
		int totalStatus = 0x0;
		for (int s : status) {
			totalStatus = totalStatus | s;
		}
		return (readyOps & totalStatus) == totalStatus;
	}

	@Override
	public void run() {
		try {
			System.out.println("Nio ServerChannel Startup  ————————————————————");
			this.selector = Selector.open();
			this.keep_working = true;
			threadPools = ThreadPoolCollection.threadPools(3, 5, 5);
			startListener(registSelector(this.host, this.port, this.selector, threadPools));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
