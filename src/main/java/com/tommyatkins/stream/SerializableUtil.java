package com.tommyatkins.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class SerializableUtil {

	/**
	 * 保存实例到<filePath>下
	 * 
	 * @param o
	 * @param filePath
	 * @throws IOException
	 */
	public static void writeClassFile(Object o, String filePath) throws IOException {
		writeClassFile(o, new File(filePath));
	}

	/**
	 * 保存实例到<file>下
	 * 
	 * @param o
	 * @param file
	 * @throws IOException
	 */
	public static void writeClassFile(Object o, File file) throws IOException {
		OutputStream os = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(o);
		oos.close();
		os.close();
	}

	/**
	 * 从<filePath>读取实例
	 * 
	 * @param filePath
	 * @return
	 */
	public static Object readClassFile(String filePath) {
		return readClassFile(new File(filePath));
	}

	/**
	 * 实例转换成输入流
	 * 
	 * @param o
	 * @return
	 * @throws IOException
	 */
	public static InputStream tranObjectToInputStream(Object o) throws IOException {
		return new ByteArrayInputStream(tranObjectToByte(o));
	}
	
	/**
	 * 实例转换成字节码
	 * 
	 * @param o
	 * @return
	 * @throws IOException
	 */
	public static byte[] tranObjectToByte(Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		baos.close();
		oos.flush();
		oos.close();
		return baos.toByteArray();
	}

	/**
	 * 字节数据转换成对象
	 * 
	 * @param data
	 * @return
	 */
	public static Object tranBytesToObject(byte[] data) {
		Object o = null;
		try {
			if (data != null && data.length > 0) {
				InputStream is = new ByteArrayInputStream(data);
				ObjectInputStream ois = new ObjectInputStream(is);
				o = ois.readObject();
				ois.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * 从<file>读取实例
	 * 
	 * @param file
	 * @return
	 */
	public static Object readClassFile(File file) {
		Object o = null;
		InputStream is = null;
		ObjectInputStream ois = null;
		try {
			if (file != null && file.exists()) {
				is = new FileInputStream(file);
				ois = new ObjectInputStream(is);
				o = ois.readObject();
				ois.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if (is!=null) {
					is.close();
				}
				if (ois!=null) {
					ois.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return o;
	}

}
