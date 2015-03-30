package com.tommyatkins.ios.ipa;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.axiom.om.OMElement;

import com.dd.plist.PropertyListParser;
import com.tommyatkins.xml.XmlReader;

public class IpaInformationReader {

	private final static String PLIST = "Info.plist";
	private final static String SETTER_HEAD = "set";
	private final static String CHARSET = "UTF-8";
	private final static String DICT = "dict";
	private static final String ICON = "/icon.png";

	public static IpaInformation getInfomation(File ipa) {
		IpaInformation infomation = null;
		InputStream in = null;
		InputStream iconStream = null;
		ZipFile zip = null;
		if (ipa != null && ipa.exists()) {
			XmlReader reader = null;
			try {
				zip = new ZipFile(ipa);
				Enumeration<? extends ZipEntry> entries = zip.entries();
				/**
				 * 获取IPA中的Info.PLIST文件
				 */
				boolean hasPlist = false, hasIcon = false;
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (entry.getName().toUpperCase().contains(ICON.toUpperCase())) {
						iconStream = zip.getInputStream(entry);
						hasIcon = true;
					} else if (isIpaInfoPlist(entry.getName())) {
						in = zip.getInputStream(entry);
						hasPlist = true;
					}
					if (hasPlist && hasIcon) {
						break;
					}
				}
				if (in != null) {
					/**
					 * 解析生成信息对象
					 */
					String informationXML = PropertyListParser.parse(in).toXMLPropertyList();
					infomation = new IpaInformation();
					reader = XmlReader.buildXmlDoc(informationXML, CHARSET);
					OMElement dictElement = reader.getChildEleByParentEle(reader.getDocument().getOMDocumentElement(), DICT);
					Collection<OMElement> children = reader.getChildren(dictElement);
					Iterator<OMElement> iterator = children.iterator();
					while (iterator.hasNext()) {
						try {
							OMElement key = iterator.next();
							if (iterator.hasNext()) {
								OMElement value = iterator.next();
								Method setter = infomation.getClass().getMethod(
										new StringBuffer().append(SETTER_HEAD).append(key.getText()).toString(), String.class);
								String val = value.getLocalName();
								/**
								 * Some value are Boolean type.
								 */
								if (!val.equals(String.valueOf(true)) && !val.equals(String.valueOf(false))) {
									val = value.getText();
								}
								setter.invoke(infomation, val);
							}
						} catch (Exception e) {
							continue;
						}
					}

				}

				if (iconStream != null) {
					byte[] iconBytes = new byte[iconStream.available()];
					iconStream.read(iconBytes);
					if (infomation != null) {
						infomation.setIcon(iconBytes);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
					if (in != null) {
						in.close();
					}
					if (iconStream != null) {
						iconStream.close();
					}
					if (zip != null) {
						zip.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return infomation;
	}

	public static IpaInformation getInfomation(String filePath) {
		return getInfomation(new File(filePath));
	}

	/**
	 * edit：2013-12-24 by kzl 获取Info.plist的逻辑修改，固定在Payload/Appname/Info.plist的文件才是正确的。
	 * 
	 * @param name
	 * @return
	 */
	private static boolean isIpaInfoPlist(String name) {
		boolean result = false;
		if (name.endsWith(PLIST)) {
			/**
			 * 重复两次截取操作，获得正确的文件位置。
			 */
			int index = name.indexOf("/") + 1;
			if (index > 0 && index < name.length()) {
				name = name.substring(index);
				index = name.indexOf("/") + 1;
				if (index > 0 && index < name.length()) {
					name = name.substring(index);
					result = name.equals(PLIST);
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		getInfomation(new File("C:\\Users\\xusd\\Desktop\\smartcode.ipa"));
	}
}
