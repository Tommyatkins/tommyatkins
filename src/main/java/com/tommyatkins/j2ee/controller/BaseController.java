package com.tommyatkins.j2ee.controller;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tommyatkins.freemarker.FreeMarker;
import com.tommyatkins.j2ee.SystemProperties;
import com.tommyatkins.j2ee.user.BaseUser;

import freemarker.template.TemplateException;

public class BaseController {

	public static final String AJAX_REDIRECT = "ajax-redirect";
	public static final String YL_PC_USER = "YL_PC_USER";

	protected static final String SUCCESS = "success";
	protected static final String ERROR = "error";
	protected static final String CODE = "code";
	protected static final String MESSAGE = "message";
	protected static final String ALERT = "alert_message";
	protected static final String STATUS = "status";
	protected static final String RESULT = "result";

	@SuppressWarnings("unchecked")
	public <T extends BaseUser> T getCurrentUser(HttpServletRequest request, Class<T> userClass) {
		T user = null;
		HttpSession session = null;
		if (request == null || (session = request.getSession()) == null) {
			// invalid request or session,ignore
		} else {
			Object o = null;
			if ((o = session.getAttribute(YL_PC_USER)) != null && (userClass == BaseUser.class || o.getClass().getName().equals(userClass.getName()))) {
				user = (T) o;
			}
		}
		return user;
	}

	// @ModelAttribute
	public void baseModel(HttpServletRequest request, HttpServletResponse response) {
	}

	// @ExceptionHandler
	public String exceptionHandler(HttpServletRequest request, Exception e) {
		return "result/error";
	}

	public String responseJson(String message, HttpServletResponse response) {
		return this.responseJson(message, null, response);
	}

	public String responseJson(String message, Object result, HttpServletResponse response) {
		return message == null ? this.responseJson(SUCCESS, message, result, response) : this.responseJson(ERROR, message, null, response);
	}

	public String responseJson(String code, String msg, Object result, HttpServletResponse response) {
		JSONObject json = new JSONObject();
		json.put(STATUS, code);
		json.put(MESSAGE, msg);
		json.put(RESULT, result);
		String jsonString = json.toJSONString();
		this.writeJSON(jsonString, response);
		return jsonString;
	}

	public String ajaxJson(String message, HttpServletResponse response) {
		return this.ajaxJson(message == null ? SUCCESS : ERROR, message, response);
	}

	public String ajaxJson(String code, String msg, HttpServletResponse response) {
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put(CODE, code);
		jsonMap.put(MESSAGE, msg);
		String json = JSON.toJSON(jsonMap).toString();
		this.writeJSON(json, response);
		return json;
	}

	protected HttpServletResponse addUnCacheHeader(HttpServletResponse response) {
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		return response;
	}

	/**
	 * 生成HTML
	 * 
	 * @param templatePath
	 * @param map
	 * @param htmlPath
	 * @throws Exception
	 */
	public void toHTML(String templatePath, Map<String, Object> map, String htmlPath) throws TemplateException, IOException {
		String filePath = new StringBuilder(SystemProperties.getLocalPath()).append(htmlPath).toString();
		OutputStream os = new FileOutputStream(new File(filePath));
		OutputStreamWriter writer = new OutputStreamWriter(os, charset);
		FreeMarker.out.write(templatePath, this.addBasePath(map), writer);
		writer.close();
		os.close();
	}

	/**
	 * 获取内容
	 * 
	 * @param templatePath
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public String toContent(String templatePath, Map<String, Object> map) throws TemplateException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FreeMarker.out.write(templatePath, this.addBasePath(map), new OutputStreamWriter(baos, charset));
		return new String(baos.toByteArray(), charset);
	}

	/**
	 * 输出页面内容
	 * 
	 * @param templatePath
	 * @param map
	 * @throws Exception
	 */
	public void toPage(String templatePath, Map<String, Object> map, HttpServletResponse response) throws TemplateException, IOException {
		response.setCharacterEncoding(charset);
		FreeMarker.out.write(templatePath, this.addBasePath(map), response.getWriter());
	}

	private Map<String, Object> addBasePath(Map<String, Object> map) {
		map = map == null ? new HashMap<String, Object>() : map;
		map.put("basePath", "");
		return map;
	}

	protected static final String charset = "UTF-8";
	protected static final String MP4 = "video/mp4";
	protected static final String X_DOWNLOAD = "application/x-download";
	protected static final String Json = "text/json";
	protected static final String IMAGE_GIF = "image/gif";
	protected static final String XML = "text/xml";
	protected static final String TEXT = "text/plain";
	protected static final String HTML = "text/html";

	/**
	 * 写出文件及其文字内容
	 * 
	 * @param fileName
	 * @param content
	 */
	public void writeFile(String fileName, String content, HttpServletResponse response) {
		this.writeContent(content, this.setFileDownLoadHead(response, fileName));
	}

	/**
	 * 写出文件及其数据流
	 * 
	 * @param fileName
	 * @param in
	 */
	public void writeFile(String fileName, InputStream in, HttpServletResponse response) {
		this.writeStream(in, this.setFileDownLoadHead(response, fileName));
	}

	/**
	 * 设置文件头为文件下载，其文件名为<fileName>
	 * 
	 * @param response
	 * @param fileName
	 * @return
	 */
	public HttpServletResponse setFileDownLoadHead(HttpServletResponse response, String fileName) {
		try {
			String encoding = charset;
			response.setContentType(X_DOWNLOAD);
			fileName = URLEncoder.encode(URLDecoder.decode(fileName, encoding), encoding);
			response.addHeader("Content-Disposition", "attachment;" + "filename=\"" + fileName + "\"");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 写出JSON
	 * 
	 * @param s
	 */
	public void writeJSON(String json, HttpServletResponse response) {
		response.setContentType(Json);
		this.writeContent(json, response);
	}

	/**
	 * 写出XML
	 * 
	 * @param xml
	 */
	public void writeXML(String xml, HttpServletResponse response) {
		response.setContentType(XML);
		this.writeContent(xml, response);
	}

	/**
	 * 写出图片
	 * 
	 * @param image
	 */
	public void writeImage(File image, HttpServletResponse response) {
		response.setContentType(IMAGE_GIF);
		try {
			this.writeStream(new FileInputStream(image), response);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 默认相应方式写出字节
	 * 
	 * @param bytes
	 */
	public void writeBytes(byte[] bytes, HttpServletResponse response) {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		this.writeStream(bis, response);
	}

	/**
	 * 写出内容/字符串的基本方法，编码由配置文件决定，参考remote.properties
	 * 
	 * @param content
	 * @param response
	 */
	public void writeContent(String content, HttpServletResponse response) {
		String contentType = response.getContentType();
		if (contentType == null) {
			contentType = TEXT;
			response.setContentType(contentType);
		}
		response.setCharacterEncoding(charset);
		BufferedWriter fw = null;
		try {
			fw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), charset));
			fw.write(content);
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 写出数据流
	 * 
	 * @param in
	 * @param response
	 */
	public void writeStream(InputStream in, HttpServletResponse response) {
		if (in != null) {
			response.setCharacterEncoding(charset);
			OutputStream out = null;
			try {
				int length = in.available();
				response.setHeader("Accept-Ranges", "bytes");
				response.setHeader("Content-Length", String.valueOf(length));
				byte[] bs = new byte[length];
				out = response.getOutputStream();// 从response中获取getOutputStream
				while (in.read(bs) != -1) {
					out.write(bs);
					out.flush();
				}
			} catch (Exception e) {

			} finally {
				try {
					if (out != null) {
						out.close();
					}
					in.close(); // 读完关闭InputStream
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}


	public static String getBasePath(HttpServletRequest request) {
		String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
		return basePath;
	}
}
