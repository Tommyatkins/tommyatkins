package com.tommyatkins.test.socket.nio.rewrite.setting;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ServerHandlerSetting {

	protected int bufferSize = 1024;

	private Charset charset = Charset.forName("GBK");

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public String decode(ByteBuffer bb) {
		bb.flip();
		return charset.decode(bb).toString();
	}

	public ByteBuffer encode(String str) {
		return charset.encode(str);
	}

}
