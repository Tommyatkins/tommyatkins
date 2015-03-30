package com.tommyatkins.fasedfs;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.DownloadStream;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.ProtoCommon;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

public class FastDfs {
	private static String Url;
	static {
		Properties properties = new Properties();
		try {
			String path = FastDfs.class.getResource("/system.properties").getPath().replaceAll("%20", " ");
			FileInputStream fis = new FileInputStream(path);
			properties.load(fis);
			fis.close();
			Url = properties.getProperty("fastdfs.url");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException, MyException, NoSuchAlgorithmException {
		ClientGlobal.init(FastDfs.class.getResource("/system.properties").getPath().replaceAll("%20", " "));
		// System.out.println(Url);
		// String path = "C:\\Users\\pc\\Desktop\\p&v\\foot.gif";
		// FileInputStream fis = new FileInputStream(path);
		// String filename = "foot.gif";
		// String[] result = FastDfs.uploadFile(fis, filename, fis.available());
		// fis.close();
		// String group = result[0];
		// String file = result[1];
		// String url = getHttpUrl(filename, group, file);
		// System.out.println(group);
		// System.out.println(file);
		// System.out.println(url);

		// deleteFile("group1", "M00/00/21/oYYBAFSI-DyAPcPyACxo-npLXE4196.gif");
	}

	/**
	 * 上传文件
	 * 
	 * @param inStream
	 *            文件输入流
	 * @param uploadFileName
	 *            文件名，如test.txt
	 * @param fileLength
	 *            文件长度
	 * @return String[] String[0]组名 String[1]文件名
	 * @throws IOException
	 */
	public static String[] uploadFile(InputStream inStream, String uploadFileName, long fileLength) throws IOException {

		String[] results = null;
		String fileExtName = "";
		if (uploadFileName.contains(".")) {
			fileExtName = uploadFileName.substring(uploadFileName.lastIndexOf(".") + 1);
		} else {
			System.out.println("文件名格式错误，必须包括点号!");
			return null;
		}

		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		StorageServer storageServer = null;
		StorageClient client = new StorageClient(trackerServer, storageServer);

		NameValuePair[] metaList = new NameValuePair[3];
		metaList[0] = new NameValuePair("fileName", uploadFileName);
		metaList[1] = new NameValuePair("fileExtName", fileExtName);
		metaList[2] = new NameValuePair("fileLength", String.valueOf(fileLength));

		try {
			results = client.upload_file(null, fileLength, new UploadFileSender(inStream), fileExtName, metaList);
		} catch (Exception e) {
			System.out.println("client.upload_file文件上传错误!");
		} finally {
			trackerServer.close();
		}
		return results;
	}

	/**
	 * 上传文件
	 * 
	 * @param inStream
	 *            文件输入流
	 * @param uploadFileName
	 *            文件名，如test.txt
	 * @param fileLength
	 *            文件长度
	 * @return String[] String[0]组名 String[1]文件名
	 * @throws IOException
	 */
	public static String[] uploadFile(byte[] fileByte, String uploadFileName, long fileLength) throws IOException {

		String[] results = null;
		String fileExtName = "";
		if (uploadFileName.contains(".")) {
			fileExtName = uploadFileName.substring(uploadFileName.lastIndexOf(".") + 1);
		} else {
			System.out.println("文件名格式错误，必须包括点号!");
			return null;
		}

		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		StorageServer storageServer = null;
		StorageClient client = new StorageClient(trackerServer, storageServer);

		NameValuePair[] metaList = new NameValuePair[3];
		metaList[0] = new NameValuePair("fileName", uploadFileName);
		metaList[1] = new NameValuePair("fileExtName", fileExtName);
		metaList[2] = new NameValuePair("fileLength", String.valueOf(fileLength));

		try {
			ByteArrayInputStream inStream = new ByteArrayInputStream(fileByte);
			results = client.upload_file(null, fileLength, new UploadFileSender(inStream), fileExtName, metaList);
		} catch (Exception e) {
			System.out.println("client.upload_file文件上传错误!");
		} finally {
			trackerServer.close();
		}
		return results;
	}

	public static boolean deleteFile(String groupName, String deleteFileName) throws IOException {
		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		StorageClient client = getStorageClient(trackerServer);
		try {
			int res = client.delete_file(groupName, deleteFileName);
			if (res == 0) {
				return true;
			}
		} catch (MyException e) {
			System.out.println("client.delete_file文件删除错误!请查看是否存在该文件。");
		} finally {
			trackerServer.close();
		}
		return false;
	}

	public static boolean deleteFile(String fileUrl) throws IOException {
		if (null == fileUrl || fileUrl.isEmpty()) {
			throw new FileNotFoundException(fileUrl + " is invalid or not found.");
		} else {
			fileUrl = fileUrl.trim();
			return deleteFile(fileUrl.substring(0, fileUrl.indexOf("/")), fileUrl.substring(fileUrl.indexOf("/") + 1, fileUrl.length()));
		}
	}

	public static FileInfo getFileInfo(String groupName, String fileName) throws IOException {
		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		StorageClient client = getStorageClient(trackerServer);
		FileInfo fInfo = null;
		try {
			fInfo = client.query_file_info(groupName, fileName);
		} catch (Exception e) {
			System.out.println("client.query_file_info获取文件信息错误!");
		} finally {
			trackerServer.close();
		}
		return fInfo;
	}

	public static String getIp(String groupName, String fileName) throws IOException {
		FileInfo fInfo = getFileInfo(groupName, fileName);
		return fInfo == null ? null : fInfo.getSourceIpAddr();
	}

	public static String getToken(String fileName, int ts) throws IOException, NoSuchAlgorithmException {
		String token = null;
		try {
			token = ProtoCommon.getToken(fileName, ts, ClientGlobal.getG_secret_key());
		} catch (MyException e) {
			System.out.println("获取token错误!");
		}
		return token;
	}

	/**
	 * 获得文件访问路径（外网访问）
	 * 
	 * @param downName
	 *            文件下载后的保存名（可以为null,可以不带后缀名，后缀名由fileName决定）
	 * @param groupName
	 * @param fileName
	 */
	public static String getHttpUrl(String downName, String groupName, String fileName) throws NoSuchAlgorithmException, IOException {
		StringBuilder httpUrlBu = new StringBuilder("https://" + Url + "/download?p=");
		int ts = (int) (System.currentTimeMillis() / 1000);
		String token = getToken(fileName, ts);
		String ip = getIp(groupName, fileName);
		String[] ips = ip.split("\\.");

		httpUrlBu.append(ips[2]);
		httpUrlBu.append(".");
		httpUrlBu.append(ips[3]);
		httpUrlBu.append("&token=");
		httpUrlBu.append(token);
		httpUrlBu.append("&file=");
		httpUrlBu.append(groupName);
		httpUrlBu.append("/");
		httpUrlBu.append(fileName);
		httpUrlBu.append("&ts=");
		httpUrlBu.append(ts);
		// 下载名
		if (downName != null) {
			String ex = getExtention(fileName.trim());// 后缀
			int pointIndex = downName.lastIndexOf(".");
			if (pointIndex == -1) {
				downName = downName + "." + ex;
			} else {
				downName = downName.substring(0, pointIndex) + "." + ex;
			}
			httpUrlBu.append("&n=");
			httpUrlBu.append(URLEncoder.encode(downName, "UTF-8"));
		}
		return httpUrlBu.toString();
	}

	public static String getHttpUrl(String downName, String fileUrl) throws NoSuchAlgorithmException, IOException {
		return getHttpUrl(downName, fileUrl.substring(0, fileUrl.indexOf("/")), fileUrl.substring(fileUrl.indexOf("/") + 1, fileUrl.length()));
	}

	public static byte[] getFileByByte(String groupName, String fileName) throws IOException, MyException {
		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		byte[] b = null;
		try {
			StorageClient client = getStorageClient(trackerServer);
			b = client.download_file(groupName, fileName);
		} finally {
			trackerServer.close();
		}
		return b;
	}

	public static byte[] getFileByByte(String fileUrl) throws IOException, MyException {
		fileUrl = fileUrl.trim();
		return getFileByByte(fileUrl.substring(0, fileUrl.indexOf("/")), fileUrl.substring(fileUrl.indexOf("/") + 1, fileUrl.length()));
	}

	public static int getFileByStream(String groupName, String fileName, OutputStream out) throws IOException, MyException {
		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		try {
			StorageClient client = getStorageClient(trackerServer);
			return client.download_file(groupName, fileName, new DownloadStream(out));
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
			trackerServer.close();
		}
	}

	public static int getFileByStream(String fileUrl, OutputStream out) throws IOException, MyException {
		fileUrl = fileUrl.trim();
		return getFileByStream(fileUrl.substring(0, fileUrl.indexOf("/")), fileUrl.substring(fileUrl.indexOf("/") + 1, fileUrl.length()), out);
	}

	/**
	 * 不要在用这个方法，此方法不安全
	 * 
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private static StorageClient getStorageClient() throws IOException {
		TrackerClient tracker = new TrackerClient();
		TrackerServer trackerServer = tracker.getConnection();
		StorageServer storageServer = null;
		return new StorageClient(trackerServer, storageServer);
	}

	private static StorageClient getStorageClient(TrackerServer trackerServer) throws IOException {
		return new StorageClient(trackerServer, null);
	}

	private static String getExtention(String fileName) {
		if (fileName == null || fileName.trim().isEmpty()) {
			return "";
		}
		int pos = fileName.lastIndexOf(".");
		if (pos + 1 >= fileName.length()) {
			return "";
		}
		return fileName.substring(pos + 1).toLowerCase();
	}
}
