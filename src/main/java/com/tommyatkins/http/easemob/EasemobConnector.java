package com.tommyatkins.http.easemob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tommyatkins.http.base.HttpConnection;
import com.tommyatkins.http.base.MultipartFormFile;
import com.tommyatkins.http.base.HttpConnection.RequestMethod;

public class EasemobConnector {

	private static final String FILE_PATH;
	private static final String BASE_URL;
	private static final String CLIENT_ID;
	private static final String CLIENT_SECRET;
	private static final String Json = "application/json";
	private static final String UTF_8 = "UTF-8";

	private static String accessToken;
	private static String Bearer;

	public static void main(String[] args) throws IOException {

		System.out.println(getUser("18628157290"));;
	}

	static {
		StringBuilder baseURL = new StringBuilder();
		String client_id = null, client_secret = null, token = null;
		Class<EasemobConnector> _class = EasemobConnector.class;
		FILE_PATH = _class.getClassLoader().getResource(_class.getPackage().getName().replaceAll("\\.", "/") + "/easemob.properties").getPath()
				.replaceAll("%20", " ");
		try {
			InputStream is = new FileInputStream(new File(FILE_PATH));
			Properties properties = new Properties();
			properties.load(is);
			is.close();

			baseURL.append(properties.getProperty("easemob.rest_url")).append("/").append(properties.getProperty("easemob.org_name")).append("/")
					.append(properties.getProperty("easemob.app_name")).append("/");
			client_id = properties.getProperty("easemob.client_id");
			client_secret = properties.getProperty("easemob.client_secret");
			token = properties.getProperty("easemob.token");

		} catch (IOException e) {
			e.printStackTrace();
		}
		BASE_URL = baseURL.toString();
		CLIENT_ID = client_id;
		CLIENT_SECRET = client_secret;
		setAccessToken(token);
	}

	public static byte[] sendContent(RequestMethod method, String subURL) throws IOException {
		return sendContent(method, subURL, null);
	}

	public static byte[] sendContent(RequestMethod method, String subURL, String content) throws IOException {
		return sendContent(method, subURL, content, null);
	}

	public static byte[] sendContent(RequestMethod method, String subURL, String content, Map<String, String> header) throws IOException {

		byte[] data = null;
		HttpURLConnection connection = HttpConnection.getDefaultConnector(BASE_URL + subURL, method);
		System.out.println(connection.getURL().getPath());
		connection.setRequestProperty("Content-Type", Json);
		addAuthorizedHeader(connection);
		if (header != null && header.size() > 0) {
			for (String head : header.keySet()) {
				connection.setRequestProperty(head, header.get(head));
			}
		}
		connection.connect();
		if (content != null) {
			try (OutputStream os = connection.getOutputStream()) {
				os.write(content.getBytes(UTF_8));
				os.flush();
				os.close();

			} catch (IOException e) {
				System.err.println(connection.getResponseCode() + " - " + connection.getResponseMessage());
				throw e;
			}
		}
		int code = connection.getResponseCode();
		if (code == 200) {
			try (InputStream in = connection.getInputStream();) {
				data = HttpConnection.readBytesFromInputStream(in);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println(code + " - " + connection.getResponseMessage());
			try (InputStream in = connection.getErrorStream();) {
				byte[] error = HttpConnection.readBytesFromInputStream(in);
				in.close();
				JSONObject json = JSON.parseObject(HttpConnection.byteToString(error));
				if (json != null && ("unauthorized".equals(json.getString("error")) || "auth_unverified_oath".equals(json.getString("error")))) {
					// 身份校验失败,重新获取token
					applyAccessToken();
					// 关闭当前连接
					connection.disconnect();
					// 再重新调用业务逻辑
					return sendContent(method, subURL, content, header);
				} else {
					data = error;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		connection.disconnect();
		return data;

	}

	/**
	 * 获取管理员Token
	 * 
	 * @return
	 * @throws IOException
	 */
	public static synchronized String applyAccessToken() throws IOException {
		JSONObject requestJSON = new JSONObject();
		requestJSON.put("grant_type", "client_credentials");
		requestJSON.put("client_id", CLIENT_ID);
		requestJSON.put("client_secret", CLIENT_SECRET);

		byte[] data = sendContent(HttpConnection.RequestMethod.POST, "token", requestJSON.toString());

		String ret = HttpConnection.byteToString(data);
		JSONObject json = JSON.parseObject(ret);
		String token = json.getString("access_token");
		if (token == null || token.trim().isEmpty()) {
			throw new RuntimeException("Apply easemob admin token fail!");
		} else {
			setAccessToken(token);
			System.out.println("token: " + accessToken);
			InputStream is = new FileInputStream(new File(FILE_PATH));
			Properties properties = new Properties();
			properties.load(is);
			is.close();

			// 写入文件
			properties.setProperty("easemob.token", accessToken);
			FileOutputStream fos = new FileOutputStream(new File(FILE_PATH));
			properties.store(fos, null);
			fos.close();
		}
		return accessToken;
	}

	/**
	 * 添加用户
	 * 
	 * @param username
	 * @param password
	 * @param nickname
	 * @return
	 * @throws IOException
	 */
	public static String addUser(String username, String password, String nickname) throws IOException {
		JSONObject requestJSON = new JSONObject();
		requestJSON.put("username", username);
		requestJSON.put("password", password);
		requestJSON.put("nickname", nickname);
		return HttpConnection.byteToString(sendContent(HttpConnection.RequestMethod.POST, "user", requestJSON.toString()));
	}

	/**
	 * 获取用户信息
	 * 
	 * @param identify
	 * @return
	 * @throws IOException
	 */
	public static String getUser(String identify) throws IOException {
		return HttpConnection.byteToString(sendContent(HttpConnection.RequestMethod.GET, "user/" + identify));
	}

	/**
	 * 删除用户
	 * 
	 * @param identify
	 * @return
	 * @throws IOException
	 */
	public static String deleteUser(String identify) throws IOException {
		return HttpConnection.byteToString(sendContent(HttpConnection.RequestMethod.DELETE, "user/" + identify));
	}

	/**
	 * 重置密码
	 * 
	 * @param identify
	 * @param newpassword
	 * @return
	 * @throws IOException
	 */
	public static String resetPassword(String identify, String newpassword) throws IOException {
		JSONObject requestJSON = new JSONObject();
		requestJSON.put("newpassword", newpassword);
		return HttpConnection.byteToString(sendContent(HttpConnection.RequestMethod.PUT, "users/" + identify + "/password", requestJSON.toString()));
	}

	/**
	 * 更改昵称
	 * 
	 * @param identify
	 * @param nickname
	 * @return
	 * @throws IOException
	 */
	public static String setNickname(String identify, String nickname) throws IOException {
		JSONObject requestJSON = new JSONObject();
		requestJSON.put("nickname", nickname);
		return HttpConnection.byteToString(sendContent(HttpConnection.RequestMethod.PUT, "users/" + identify, requestJSON.toString()));

	}

	/**
	 * 是否在线
	 * 
	 * @param identify
	 * @return
	 * @throws IOException
	 */
	public static boolean isOnline(String identify) throws IOException {
		JSONObject json = JSON.parseObject(HttpConnection
				.byteToString(sendContent(HttpConnection.RequestMethod.PUT, "users/" + identify + "/status")));
		return "online".equals(json.getJSONObject("data").get(identify));
	}

	/**
	 * 文件上传，返回数组
	 * 
	 * @param file
	 * @param contentType
	 * @return
	 * @throws IOException
	 */
	public static String[] upload(File file, String contentType) throws IOException {
		String boundary = HttpConnection.MultipartForm.newBoundary();
		HttpURLConnection connection = HttpConnection.MultipartForm.getMultipartFormConnection(BASE_URL + "chatfiles", boundary);
		addAuthorizedHeader(connection);
		connection.addRequestProperty("restrict-access", "true");

		try (OutputStream os = connection.getOutputStream();) {
			MultipartFormFile inputFile = new MultipartFormFile(file, "file", contentType);
			HttpConnection.MultipartForm.multipartFile(os, boundary, inputFile);
			HttpConnection.MultipartForm.finishWriting(os, boundary);
			os.close();
		} catch (IOException e) {
			System.err.println(connection.getResponseCode() + " - " + connection.getResponseMessage());
			throw e;
		}
		byte[] data = null;
		try (InputStream is = connection.getInputStream();) {
			data = HttpConnection.readBytesFromInputStream(is);
			is.close();
		} catch (IOException e) {
			System.err.println(connection.getResponseCode() + " - " + connection.getResponseMessage());
			throw e;
		}
		connection.disconnect();
		JSONObject json = JSON.parseObject(HttpConnection.byteToString(data));
		JSONObject entities = json.getJSONArray("entities").getJSONObject(0);
		return new String[] { entities.getString("uuid"), entities.getString("share-secret") };
	}

	/**
	 * 下载附件,thumb：是否是缩略图
	 * 
	 * @param uuid
	 * @param share_secret
	 * @param thumbnail
	 * @return
	 * @throws IOException
	 */
	public static byte[] download(String uuid, String share_secret, boolean thumb) throws IOException {
		Map<String, String> header = new HashMap<String, String>();
		header.put("Accept", "application/octet-stream");
		header.put("share-secret", share_secret);
		if (thumb) {
			header.put("thumbnail", "true");
		}
		return sendContent(HttpConnection.RequestMethod.GET, "chatfiles/" + uuid, null, header);
	}

	public static String sendMessage(String from, String[] target, String message, Map<String, Object> ex) throws IOException {
		JSONObject messageBody = new JSONObject();
		messageBody.put("type", "txt");
		messageBody.put("msg", message);
		return sendMessageBody(from, target, messageBody, ex);
	}

	public static String sendImage(String from, String[] target, String filename, String uuid, String share_secret, Map<String, Object> ex)
			throws IOException {
		JSONObject messageBody = new JSONObject();
		messageBody.put("type", "img");
		messageBody.put("url", BASE_URL + "chatfiles/" + uuid);
		messageBody.put("filename", filename);
		messageBody.put("secret", share_secret);
		return sendMessageBody(from, target, messageBody, ex);
	}

	public static String sendAudio(String from, String[] target, String filename, String length, String uuid, String share_secret,
			Map<String, Object> ex) throws IOException {
		JSONObject messageBody = new JSONObject();
		messageBody.put("type", "audio");
		messageBody.put("url", BASE_URL + "chatfiles/" + uuid);
		messageBody.put("filename", filename);
		messageBody.put("length", length);
		messageBody.put("secret", share_secret);
		return sendMessageBody(from, target, messageBody, ex);
	}

	public static String sendCommand(String from, String[] target, String action, Map<String, Object> ex) throws IOException {
		JSONObject messageBody = new JSONObject();
		messageBody.put("type", "cmd");
		messageBody.put("action", action);
		return sendMessageBody(from, target, messageBody, ex);
	}

	public static String getChatMessages(int size, String sql, String cursor) throws IOException {
		StringBuilder param = new StringBuilder();
		if (size > 0) {
			param.append("&limit=");
			param.append(size);
		}
		if (sql == null) {

		} else {
			param.append("&ql=");
			param.append(URLEncoder.encode(sql, UTF_8));
			if (cursor == null) {

			} else {
				param.append("&cursor=");
				param.append(cursor);
			}
		}
		return HttpConnection.byteToString(sendContent(HttpConnection.RequestMethod.GET, "chatmessages?" + param.toString()));
	}

	private static String sendMessageBody(String from, String[] target, JSONObject messageBody, Map<String, Object> ex) throws IOException {
		JSONObject requestJSON = new JSONObject();
		requestJSON.put("target_type", "users");
		requestJSON.put("target", target);
		requestJSON.put("msg", messageBody);
		requestJSON.put("from", from);
		requestJSON.put("ext", ex);
		byte[] data = sendContent(HttpConnection.RequestMethod.POST, "messages", requestJSON.toString());
		return HttpConnection.byteToString(data);
	}

	private static void addAuthorizedHeader(HttpURLConnection connection) {
		connection.setRequestProperty("Authorization", Bearer);
	}

	private static void setAccessToken(String token) {
		accessToken = token;
		Bearer = "Bearer " + accessToken;
	}

}
