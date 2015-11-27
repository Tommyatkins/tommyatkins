package com.tommyatkins.http.base.lambdas.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Function;

import com.tommyatkins.http.base.HttpConnection;
import com.tommyatkins.http.base.lambdas.process.enu.ProcessPoint;
import com.tommyatkins.http.base.lambdas.process.functional.HttpConnectionProcess;

public class DefaultProcess {

	public static <V> HttpConnectionProcess<V> process(Function<HttpURLConnection, V> f) {
		HttpConnectionProcess<V> process = (c) -> {
			return f.apply(c);
		};
		return process;
	}

	public static HttpConnectionProcess<HttpURLConnection> header(final Map<String, String> header) {
		return process((c) -> {
			header.forEach((k, v) -> {
				c.setRequestProperty(k, v);
			});
			return c;
		}).point(ProcessPoint.beforeConnect);
	}

	public static HttpConnectionProcess<HttpURLConnection> write(final InputStream is, final int bufferSize) {
		HttpConnectionProcess<HttpURLConnection> process = (connection) -> {
			if (is != null) {
				try (OutputStream out = connection.getOutputStream();) {
					byte[] buffer = new byte[bufferSize];
					int length = 0;
					while ((length = is.read(buffer)) != -1) {
						out.write(buffer, 0, length);
					}
					is.close();
					out.flush();
					out.close();
				} catch (IOException e) {
					throw e;
				}
			}
			return connection;
		};

		return process.point(ProcessPoint.write);
	}

	public static HttpConnectionProcess<byte[]> read() {
		return read(200);
	}

	public static HttpConnectionProcess<byte[]> read(final int successCode) {
		HttpConnectionProcess<byte[]> process = (connection) -> {
			byte[] data = null;
			int code = connection.getResponseCode();
			if (code == successCode) {
				try (InputStream in = connection.getInputStream();) {
					data = HttpConnection.readBytesFromInputStream(in);
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.err.println(code + " - " + connection.getResponseMessage());
				try (InputStream in = connection.getErrorStream();) {
					data = HttpConnection.readBytesFromInputStream(in);
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return data;
		};
		return process.point(ProcessPoint.read);
	}

}
