package com.tommyatkins.j2ee;

import java.util.Properties;

public class SystemProperties {

	private static SystemProperties systemProperties;

	public final String localPath; // 本地根路径

	private SystemProperties(String localPath, Properties properties) {
		this.localPath = localPath;
	}

	public final static String getLocalPath() {
		return systemProperties.localPath;
	}

}
