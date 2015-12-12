package com.tommyatkins.test.docker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.function.Function;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tommyatkins.http.base.HttpConnection;
import com.tommyatkins.http.base.TrustAnyHostnameVerifier;
import com.tommyatkins.http.base.TrustAnyTrustManager;

public class SwarmRemoteApi {

	public enum RequestMethod {
		POST, GET, PUT, DELETE;
	}

	private final static String HTTPS = "https";
	private final static String SSL = "SSL";
	private final static int BUFFER_SIZE = 1024;

	private String username = "kongzl";
	private String password = "123456";

	private String serverURL = "https://192.168.0.80:3376";

	private KeyManager[] km;

	public SwarmRemoteApi() {
		try {
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(this.getClass().getResource("/cert/client.p12").openStream(), "123456".toCharArray());
			kmf.init(ks, "123456".toCharArray());
			this.km = kmf.getKeyManagers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] request(RequestMethod method, int successCode, String url, Function<HttpURLConnection, HttpURLConnection> headerFunction,
			final InputStream is) throws IOException {
		byte[] data = null;
		URL requestURL = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
		if (requestURL.getProtocol().equals(HTTPS)) {
			SSLContext sc;
			try {
				sc = SSLContext.getInstance(SSL);
				sc.init(this.km == null ? null : this.km, new TrustManager[] { new TrustAnyTrustManager() }, new SecureRandom());
				((HttpsURLConnection) connection).setSSLSocketFactory(sc.getSocketFactory());
				((HttpsURLConnection) connection).setHostnameVerifier(new TrustAnyHostnameVerifier());
			} catch (NoSuchAlgorithmException e) {
			} catch (KeyManagementException e) {
			}
		}

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod(method.name());

		headerFunction.apply(connection);

		connection.connect();

		if (is != null) {
			try (OutputStream out = connection.getOutputStream();) {
				byte[] buffer = new byte[BUFFER_SIZE];
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

		int code = connection.getResponseCode();
		if (code == successCode) {
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

	public byte[] readBytesFromInputStream(InputStream is) throws IOException {
		return readBytesFromInputStream(is, BUFFER_SIZE);
	}

	public byte[] readBytesFromInputStream(InputStream is, int buffer_size) throws IOException {
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

	private Function<HttpURLConnection, HttpURLConnection> authHeader(final String username, final String password) {
		return authHeader(username, password, 30);
	}

	private Function<HttpURLConnection, HttpURLConnection> authHeader(final String username, final String password, int timeout) {
		return (connection) -> {

			connection.setConnectTimeout(timeout);

			connection.addRequestProperty("Content-Type", "application/json");
			connection.addRequestProperty("Connection", "keep-alive");

			return connection;
		};
	}

	private String post(int successCode, String url, String queryParams) throws IOException {
		return post(successCode, url, queryParams == null ? null : queryParams.getBytes(), 30);
	}

	private String post(int successCode, String url, String queryParams, int timeout) throws IOException {
		return post(successCode, url, queryParams == null ? null : queryParams.getBytes(), timeout);
	}

	private String post(int successCode, String url, final byte[] requestData, int timeout) throws IOException {
		byte[] data = request(RequestMethod.POST, successCode, url, authHeader(username, password, timeout), requestData == null ? null
				: new ByteArrayInputStream(requestData));
		return HttpConnection.byteToString(data);
	}

	private String get(int successCode, String url, String queryParams) throws IOException {
		return get(successCode, String.format("%s?%s", url, queryParams == null ? "" : queryParams));
	}

	private String get(int successCode, String url) throws IOException {
		byte[] data = request(RequestMethod.GET, successCode, url, authHeader(username, password), null);
		return HttpConnection.byteToString(data);
	}

	private String delete(int successCode, String url) throws IOException {
		byte[] data = request(RequestMethod.DELETE, successCode, url, authHeader(username, password), null);
		return HttpConnection.byteToString(data);
	}

	public String subURL(String url) {
		return String.format("%s%s", serverURL, url);
	}

	private String readJSONData(String path) throws IOException {
		InputStream fis = this.getClass().getResource(path).openStream();
		byte[] tmp = new byte[fis.available()];
		fis.read(tmp);
		fis.close();
		return new String(tmp, "UTF-8");
	}

	public String createContainer(String name, String config) throws IOException {
		return post(201, subURL(String.format("/containers/create?name=%s", name == null ? "" : name)), config, 360);
	}

	public String getContainer(String id) throws IOException {
		return get(200, subURL(String.format("/containers/%s/json", id)), null);
	}

	public String startContainer(String id) throws IOException {
		return post(204, subURL(String.format("/containers/%s/start", id)), null);
	}

	public String killContainer(String id) throws IOException {
		return post(204, subURL(String.format("/containers/%s/kill", id)), null);
	}

	public String removeContainer(String id) throws IOException {
		return delete(204, subURL(String.format("/containers/%s", id)));
	}

	public static void main(String[] args) throws IOException {
		SwarmRemoteApi api = new SwarmRemoteApi();
		String id = "67fe68ccf155212481de82ac4d56524a6450b17e5b103e53ec646c4df031ff43";

		String createConfig = api.readJSONData("/data/data.json");

		String ret = null;
		ret = api.createContainer("TOMMYATKINS_Clipboard_Test", createConfig);

		JSONObject createRet = JSONObject.parseObject(ret);

		id = createRet.getString("Id");

		System.out.println(id);

		api.startContainer(id);

		System.out.println(ret = api.getContainer(id));

		JSONObject container = JSON.parseObject(ret);
		JSONObject networkSettings = container.getJSONObject("NetworkSettings");
		JSONObject portsMappings = networkSettings.getJSONObject("Ports");
		JSONArray vncPorts = portsMappings.getJSONArray("5900/tcp");

		if (vncPorts != null && vncPorts.size() > 0) {
			JSONObject vncConfig = vncPorts.getJSONObject(0);
			System.out.println(vncConfig.getString("HostIp"));
			System.out.println(vncConfig.getString("HostPort"));
		}

//		System.out.println(api.killContainer(id));
//
//		System.out.println(api.removeContainer(id));

	}

}
