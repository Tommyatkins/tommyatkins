package com.tommyatkins.fasedfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.csource.fastdfs.UploadCallback;

public class UploadFileSender implements UploadCallback {
	private InputStream inStream;

	public UploadFileSender(InputStream inStream) {
		this.inStream = inStream;
	}

	public int send(OutputStream out) throws IOException {

		int readBytes;
		byte[] buff = new byte[256 * 1024];

		try {
			while ((readBytes = inStream.read(buff)) >= 0) {
				if (readBytes == 0) {
					continue;
				}

				out.write(buff, 0, readBytes);
			}
		} finally {
			inStream.close();
		}

		return 0;
	}
}
