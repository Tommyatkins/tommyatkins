package com.tommyatkins.http.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author TOMMYATKINS
 *
 */
public class HttpConnection {

	public static void main(String[] args) throws IOException {
		//	serialToUDID  CBF5CE79-49FF-45CC-BEA4-F0D112C2FB27
		//	isActive b448dd838e14990cbfd25ef43ee8b4fd6bc64cad
//		InputStream is = new ByteArrayInputStream("b448dd838e14990cbfd25ef43ee8b4fd6bc64cad".getBytes("utf-8"));
		Map<String, String> params = new HashMap<String, String>();
		JSONObject json = new JSONObject();
		json.put("token", "1b10c24c0bcbb48a7952714b2e83fdd0");
		params.put("paramJson", json.toJSONString());
		params.put("pageSize", "1");
		params.put("currentPage", "1");
		System.out.println(byteToString(post("http://tp.rimiedu.com/tpl/api/goods/getGoodsHomeListApi", params)));
	}

	public enum RequestMethod {
		POST, GET, PUT, DELETE;
	}

	private final static String HTTPS = "https";
	private final static String SSL = "SSL";
	private final static String ENCODING = "utf-8";
	private final static int BUFFER_SIZE = 1024;

	public static HttpURLConnection getDefaultConnector(String url, RequestMethod method) throws IOException {
		HttpURLConnection connection = getConnector(url);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod(method.name());
		return connection;
	}

	public static HttpURLConnection getConnector(String url) throws IOException {
		URL requestURL = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
		if (requestURL.getProtocol().equals(HTTPS)) {
			SSLContext sc;
			try {
				sc = SSLContext.getInstance(SSL);
				sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new SecureRandom());
				((HttpsURLConnection) connection).setSSLSocketFactory(sc.getSocketFactory());
				((HttpsURLConnection) connection).setHostnameVerifier(new TrustAnyHostnameVerifier());
			} catch (NoSuchAlgorithmException e) {
			} catch (KeyManagementException e) {
			}
		}
		return connection;
	}

	public static byte[] readBytesFromInputStream(InputStream is) throws IOException {
		return readBytesFromInputStream(is, BUFFER_SIZE);
	}

	public static byte[] readBytesFromInputStream(InputStream is, int buffer_size) throws IOException {
		final ByteArrayOutputStream boas = new ByteArrayOutputStream();
		final DataOutputStream dos = new DataOutputStream(boas);
		byte[] data = new byte[buffer_size];
		int length = 0;
		while ((length = is.read(data)) != -1) {
			dos.write(data, 0, length);
		}
		dos.close();
		boas.close();
		return boas.toByteArray();
	}

	public static String byteToString(byte[] data) throws IOException {
		return data == null ? null : new String(data, ENCODING);
	}

	public static byte[] get(final String url) throws IOException {
		return request(RequestMethod.GET, url, null);
	}

	public static byte[] put(final String url, InputStream inputStream) throws IOException {
		return request(RequestMethod.PUT, url, inputStream);
	}

	public static byte[] post(final String url, final Map<String, String> paramMap) throws IOException {
		PostParameters params = new PostParameters(ENCODING);
		if (paramMap != null && paramMap.size() > 0) {
			for (String key : paramMap.keySet()) {
				params.add(key, paramMap.get(key));
			}
		}
		return post(url, params);
	}

	public static byte[] post(final String url, final PostParameters params) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(params.format().getBytes(ENCODING));
		byte[] data = request(RequestMethod.POST, url, bais);
		bais.close();
		return data;
	}

	public static byte[] request(final RequestMethod method, final String url, InputStream inputStream) throws IOException {
		return request(method, url, null, inputStream);
	}

	public static byte[] request(final RequestMethod method, final String url, Map<String, String> header, InputStream inputStream)
			throws IOException {
		byte[] data = null;

		HttpURLConnection connection = getDefaultConnector(url, method);

		if (header != null && header.size() > 0) {
			for (String head : header.keySet()) {
				connection.setRequestProperty(head, header.get(head));
			}
		}

		connection.connect();

		if (inputStream != null) {
			try (OutputStream out = connection.getOutputStream();) {
				byte[] buffer = new byte[BUFFER_SIZE];
				int length = 0;
				while ((length = inputStream.read(buffer)) != -1) {
					out.write(buffer, 0, length);
				}
				inputStream.close();
				out.flush();
				out.close();

			} catch (IOException e) {
				throw e;
			}
		}

		int code = connection.getResponseCode();
		if (code == 200) {
			try (InputStream in = connection.getInputStream();) {
				data = readBytesFromInputStream(in);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println(code + " - " + connection.getResponseMessage());
			try (InputStream in = connection.getErrorStream();) {
				data = readBytesFromInputStream(in);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		connection.disconnect();
		return data;
	}

	public static class MultipartForm {
		private final static String PREFIX = "--";
		private final static String END = "\r\n";

		public static String newBoundary() {
			return new StringBuilder("----").append(new Date().getTime()).toString();
		}

		public static HttpURLConnection getMultipartFormConnection(String url, String boundary) throws IOException {
			HttpURLConnection connection = getDefaultConnector(url, RequestMethod.POST);
			connection.setRequestProperty("Connection", "keep-alive");
			connection.setRequestProperty("Charset", ENCODING);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			return connection;
		}

		public static byte[] submit(String url, final Map<String, String> paramMap, MultipartFormFile... files) throws IOException {
			PostParameters params = new PostParameters(ENCODING);
			if (paramMap != null && paramMap.size() > 0) {
				for (String key : paramMap.keySet()) {
					params.add(key, paramMap.get(key));
				}
			}
			return submit(url, params, files);
		}

		/**
		 * multipart/form-data encoding=utf-8;method=post
		 * 
		 * @param _url
		 * @param paramsMap
		 * @param filesMap
		 * @param fileContentType
		 * @return
		 * @throws Exception
		 */
		public static byte[] submit(String url, final PostParameters params, MultipartFormFile... files) throws IOException {
			final String boundary = newBoundary();
			byte[] data = null;
			HttpURLConnection connection = getMultipartFormConnection(url, boundary);
			connection.connect();
			try (OutputStream out = connection.getOutputStream()) {
				// start to submit data
				// parameters
				if (params != null) {
					for (KeyValuePair pair : params) {
						multipartParam(out, boundary, pair.getKey(), pair.getValue());
					}
				}

				// files
				if (files != null) {
					for (MultipartFormFile f : files) {
						multipartFile(out, boundary, f);
					}
				}

				// finish form submit
				finishWriting(out, boundary);
				out.close();
			} catch (IOException e) {
				InputStream errorStream = connection.getErrorStream();
				byte[] errData = HttpConnection.readBytesFromInputStream(errorStream);
				System.err.println(connection.getResponseCode() + " - " + connection.getResponseMessage());
				System.err.println(HttpConnection.byteToString(errData));
				errorStream.close();
				throw e;
			}
			int code = connection.getResponseCode();
			if (code == 200) {
				try (InputStream in = connection.getInputStream();) {
					data = readBytesFromInputStream(in);
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.err.println(code + " - " + connection.getResponseMessage());
				try (InputStream in = connection.getErrorStream();) {
					data = readBytesFromInputStream(in);
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			connection.disconnect();
			return data;
		}

		public static void finishWriting(OutputStream outputStream, String boundary) throws IOException {
			StringBuilder payload = new StringBuilder(PREFIX).append(boundary).append(PREFIX).append(END);
			outputStream.write(payload.toString().getBytes(ENCODING));
			outputStream.flush();
		}

		public static void multipartFile(OutputStream out, String boundary, MultipartFormFile f) throws IOException {
			// first - file start
			StringBuilder payload = new StringBuilder(PREFIX).append(boundary).append(END);
			InputStream fis = f.getInputStream();
			// second - file content type description
			payload.append("Content-Disposition: form-data; name=\"").append(f.getName()).append("\"; filename=\"").append(f.getFileName())
					.append("\"").append(END);
			String contentType = f.getContentType();
			if (contentType != null) {
				payload.append("Content-Type: ").append(contentType).append(END);
			}
			// with a new row to start to write the file byte, @important
			payload.append(END);

			out.write(payload.toString().getBytes(ENCODING));
			// start to write the file bytes
			int len = 0;
			byte[] data = new byte[BUFFER_SIZE];
			while ((len = fis.read(data)) != -1) {
				out.write(data, 0, len);
			}
			// final with a new row
			out.write(END.getBytes(ENCODING));
			fis.close();
		}

		public static void multipartParam(OutputStream out, String boundary, String name, String value) throws IOException {
			// first - file start
			StringBuilder payload = new StringBuilder(PREFIX).append(boundary).append(END);
			// second - file content type description
			payload.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(END);
			// with a new row to start to write the parameters value content
			// byte,
			// @important
			payload.append(END);
			out.write(payload.toString().getBytes(ENCODING));
			// start to write the parameters bytes
			out.write(value.getBytes(ENCODING));
			// final with a new row
			out.write(END.getBytes(ENCODING));
		}

	}

}
