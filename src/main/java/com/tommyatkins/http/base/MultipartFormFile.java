package com.tommyatkins.http.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MultipartFormFile {

	private String name; // 变量名

	private InputStream fis;

	private String contentType; // 文件类型

	private String fileName; // 文件名

	public MultipartFormFile() {
		// TODO Auto-generated constructor stub
	}

	public MultipartFormFile(File file, String name, String contentType) throws IOException {
		this.name = name;
		this.fis = new FileInputStream(file);
		this.contentType = contentType;
		this.fileName = file.getName();
	}

	public String getName() {
		return name;
	}

	public InputStream getInputStream() {
		return fis;
	}

	public String getContentType() {
		return contentType;
	}

	public String getFileName() {
		return fileName;
	}
}
