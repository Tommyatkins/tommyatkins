package com.tommyatkins.test.socket.nio.rewrite.thread;

import java.nio.channels.SelectionKey;

import com.tommyatkins.test.socket.nio.rewrite.pool.ThreadPoolCollection;

public class ClientDistributeThread implements Runnable {

	private SelectionKey selectionKey;

	public ClientDistributeThread(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

	@Override
	public void run() {
		try {
			if ((selectionKey.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
				ThreadPoolCollection.threadPools().read(new ClientReaderThread(selectionKey));
			} else if ((selectionKey.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
				ThreadPoolCollection.threadPools().write(new ClientWriterThread(selectionKey));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}