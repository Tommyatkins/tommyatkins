package com.tommyatkins.ios.ipa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class IpaInformation {

	private String name; // 显示的应用名称
	private String identifier; // 应用Id
	private String version; // 版本
	private String CFBundleName; // 应用名称
	private String MinimumOSVersion; // 最低版本要求
	private byte[] icon;

	public byte[] getIcon() {
		return icon;
	}

	public String getCFBundleName() {
		return CFBundleName;
	}

	public void setCFBundleName(String bundleName) {
		CFBundleName = bundleName;
	}

	public String getVersion() {
		return version;
	}

	public void setCFBundleVersion(String version) {
		this.version = version;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setCFBundleIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return this.name;
	}

	public void setCFBundleDisplayName(String name) {
		this.name = name;
	}

	public String getMinimumOSVersion() {
		return MinimumOSVersion;
	}

	public void setMinimumOSVersion(String minimumOSVersion) {
		MinimumOSVersion = minimumOSVersion;
	}

	public void setIcon(byte[] icon) {
		this.icon = icon;
	}

	public boolean saveIconAs(File iconFile) {
		boolean success = false;
		if (this.icon != null && this.icon.length > 0) {
			success = IconHandler.convertPNGFile(this.icon, iconFile);
		}
		if (this.icon != null && !success) {
			try {
				OutputStream os = new FileOutputStream(iconFile);
				os.write(this.icon);
				os.flush();
				os.close();
				success = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return success;
	}

}
