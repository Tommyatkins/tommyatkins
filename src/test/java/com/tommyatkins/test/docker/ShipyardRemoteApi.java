package com.tommyatkins.test.docker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tommyatkins.http.base.HttpConnection;
import com.tommyatkins.http.base.HttpConnection.RequestMethod;
import com.tommyatkins.http.base.PostParameters;
import com.tommyatkins.http.base.lambdas.HttpConnectionAdvanced;
import com.tommyatkins.http.base.lambdas.process.DefaultProcess;
import com.tommyatkins.http.base.lambdas.process.enu.ProcessPoint;
import com.tommyatkins.http.base.lambdas.process.functional.HttpConnectionProcess;

public class ShipyardRemoteApi {

	private String username = "kongzl";
	private String password = "123456";

	private String token = "username:*************************************";

	private String serverURL = "http://192.168.7.180:8080";

	private Function<HttpURLConnection, HttpURLConnection> doNothingFunction = (connection) -> {
		return connection;
	};

	private byte[] request(RequestMethod method, int successCode, String url, Function<HttpURLConnection, HttpURLConnection> headerFunction,
			final InputStream is) throws IOException {
		HttpConnectionProcess<HttpURLConnection> headerProcesses = DefaultProcess.process(headerFunction).point(ProcessPoint.beforeConnect);
		HttpConnectionProcess<HttpURLConnection> writeProcesses = DefaultProcess.write(is, 1024);
		HttpConnectionProcess<byte[]> readProcesses = DefaultProcess.read(successCode);
		Map<ProcessPoint, List<HttpConnectionProcess<?>>> processesMap = new HashMap<ProcessPoint, List<HttpConnectionProcess<?>>>();
		List<HttpConnectionProcess<?>> beforeConnectList = new ArrayList<HttpConnectionProcess<?>>();
		beforeConnectList.add(headerProcesses);
		List<HttpConnectionProcess<?>> writeList = new ArrayList<HttpConnectionProcess<?>>();
		writeList.add(writeProcesses);
		List<HttpConnectionProcess<?>> readList = new ArrayList<HttpConnectionProcess<?>>();
		readList.add(readProcesses);
		processesMap.put(ProcessPoint.beforeConnect, beforeConnectList);
		processesMap.put(ProcessPoint.write, writeList);
		processesMap.put(ProcessPoint.read, readList);
		byte[] data = HttpConnectionAdvanced.request(method, url, processesMap);

		return data;
	}

	private Function<HttpURLConnection, HttpURLConnection> authHeader(final String authToken) {
		return authHeader(authToken, 30);
	}

	private Function<HttpURLConnection, HttpURLConnection> authHeader(final String authToken, int timeout) {
		return (connection) -> {

			connection.setConnectTimeout(timeout);

			connection.addRequestProperty("Content-Type", "application/json");
			connection.addRequestProperty("X-Access-Token", authToken);

			connection.addRequestProperty("Connection", "keep-alive");
			connection.addRequestProperty("Accept", "application/json, text/plain, */*");

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
		byte[] data = request(RequestMethod.POST, successCode, url, authHeader(token, timeout), requestData == null ? null
				: new ByteArrayInputStream(requestData));
		return HttpConnection.byteToString(data);
	}

	private String get(int successCode, String url, String queryParams) throws IOException {
		return get(successCode, String.format("%s?%s", url, queryParams));
	}

	private String get(int successCode, String url) throws IOException {
		byte[] data = request(RequestMethod.GET, successCode, url, authHeader(token), null);
		return HttpConnection.byteToString(data);
	}

	private String delete(int successCode, String url) throws IOException {
		byte[] data = request(RequestMethod.DELETE, successCode, url, authHeader(token), null);
		return HttpConnection.byteToString(data);
	}

	public String subURL(String url) {
		return String.format("%s%s", serverURL, url);
	}

	public String accessToken(String username, String password) {
		JSONObject json = new JSONObject();
		json.put("username", username);
		json.put("password", password);
		try {
			byte[] data = request(RequestMethod.POST, 200, subURL("/auth/login"), doNothingFunction, new ByteArrayInputStream(json.toJSONString()
					.getBytes()));
			String responseData = HttpConnection.byteToString(data);
			JSONObject responseJSON = JSON.parseObject(responseData);
			token = String.format("%s:%s", username, responseJSON.getString("auth_token"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return token;
	}

	public String listContainers(boolean showAll, boolean showSize, String filters) throws IOException {
		PostParameters params = new PostParameters("all", String.valueOf(showAll)).add("size", String.valueOf(showSize));
		if (filters != null) {
			params.add("filters", filters);
		}
		return get(200, subURL("/containers/json"), params.format());
	}

	public String getContainer(String id) throws IOException {
		JSONObject filtersJSON = new JSONObject();
		filtersJSON.put("id", new String[] { id });
		return listContainers(true, true, filtersJSON.toJSONString());
	}

	public String getNode(String name) throws IOException {
		return get(200, subURL(String.format("/api/nodes/%s", name)));
	}

	public String createContainer(String name, String config) throws IOException {

		return post(201, subURL(String.format("/containers/create?name=%s", name == null ? "" : name)), config, 360);
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

	private String readJSONData() throws IOException {
		InputStream fis = this.getClass().getResource("/data/data.json").openStream();
		byte[] tmp = new byte[fis.available()];
		fis.read(tmp);
		fis.close();
		return new String(tmp, "UTF-8");
	}

	public static void main(String[] args) throws IOException {

		ShipyardRemoteApi api = new ShipyardRemoteApi();

		// token
		System.out.println(api.accessToken(api.username, api.password));

		// create
		String config = api.readJSONData();

		String createdData = api.createContainer("TOMMYATKINS", config);

		System.out.println(createdData);

		JSONObject createdJSON = JSONObject.parseObject(createdData);
		// container id
		String createdId = createdJSON.getString("Id");

		System.out.println(createdId);
		// start
		api.startContainer(createdId);
		// container information
		String containerData = api.getContainer(createdId);

		System.out.println(containerData);

		JSONArray containerArray = JSONArray.parseArray(containerData);

		int containerSize = containerArray.size();

		if (containerSize == 1) {

			JSONObject container = containerArray.getJSONObject(0);
			JSONArray names = container.getJSONArray("Names");
			if (names.size() > 0) {
				// container full name
				String fullName = names.getString(0);
				System.out.println(fullName);
				Pattern pattern = Pattern.compile("^/[\\D\\d]*/");

				Matcher matcher = pattern.matcher(fullName);

				String nodeName = null;
				// matche node name
				if (matcher.find()) {
					nodeName = fullName.substring(matcher.start() + 1, matcher.end() - 1);
				}

				if (nodeName == null) {
					System.out.println("can not match node name.");
				} else {
					System.out.println(nodeName);

					// node information
					String nodeData = api.getNode(nodeName);

					JSONObject node = JSONObject.parseObject(nodeData);
					// node address
					String nodeAddr = node.getString("addr");
					int splitIndex = nodeAddr.indexOf(":");
					String host = null, port = null;
					if (splitIndex > -1) {
						// host and port
						host = nodeAddr.substring(0, splitIndex);
						port = nodeAddr.substring(splitIndex + 1);
					}
					System.out.println(host);
					System.out.println(port);

				}
			}
		} else {
			System.out.printf("error size %d \r\n", containerSize);
		}

		api.killContainer(createdId);
		api.removeContainer(createdId);
	}

}
