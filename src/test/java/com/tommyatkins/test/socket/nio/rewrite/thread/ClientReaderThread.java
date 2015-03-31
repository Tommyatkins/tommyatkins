package com.tommyatkins.test.socket.nio.rewrite.thread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.tommyatkins.test.socket.nio.rewrite.setting.ServerHandlerSetting;

public class ClientReaderThread extends ServerHandlerSetting implements Runnable {

	private SelectionKey selectionKey;

	public ClientReaderThread(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

	@Override
	public void run() {
		try {
			
			System.out.println("selectionKey.isReadable()");
			SelectableChannel channel = this.selectionKey.channel();
			ByteBuffer data = ByteBuffer.allocate(bufferSize);
			SocketChannel socketChannel = (SocketChannel) channel;
			int length = 0;
			if ((length = socketChannel.read(data)) == -1) {
				//this.selectionKey.cancel();
			} else {
				String message = decode(data);
				if (length > 0) {
					System.out.println("server receive[" + length + "]: " + message);
					this.selectionKey.attach(message);
					selectionKey.interestOps(SelectionKey.OP_WRITE);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			this.selectionKey.cancel();
		} finally {
			// this.selectionKey.cancel();
		}
	}
}