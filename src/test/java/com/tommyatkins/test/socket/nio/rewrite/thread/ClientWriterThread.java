package com.tommyatkins.test.socket.nio.rewrite.thread;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.tommyatkins.test.socket.nio.rewrite.setting.ServerHandlerSetting;

public class ClientWriterThread extends ServerHandlerSetting implements Runnable {

	private SelectionKey selectionKey;

	public ClientWriterThread(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

	@Override
	public void run() {
		try {
			System.out.println("selectionKey.isWritable()");
			SelectableChannel channel = this.selectionKey.channel();
			Object messageAtta = this.selectionKey.attachment();
			String message = messageAtta == null ? "" : messageAtta.toString();
			SocketChannel socketChannel = (SocketChannel) channel;
			socketChannel.write(encode(message + "[TOMMYATKINS_孔梓茏]\r\n"));
		} catch (IOException e) {
			e.printStackTrace();
			this.selectionKey.cancel();
		} 

	}
}