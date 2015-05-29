package com.tommyatkins.test.string;

import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

public class UnicodeTest {

	public static void main(String[] args) throws Exception {
		FileInputStream fis = new FileInputStream("C:\\Users\\pc\\Desktop\\data.json");
		byte[] b = new byte[fis.available()];
		fis.read(b);
		UnicodeUnescaper e = new UnicodeUnescaper();
		System.out.println(e.translate(decode(new String(b,"UTF-8"))));
		fis.close();
	}

	public static String decode(String s) {
		Pattern p = Pattern.compile("&#\\d{4,5};");
		Matcher m = p.matcher(s);
		StringBuffer sb = new StringBuffer();
		int start = 0, end = 0;
		while (m.find()) {
			start = m.start() + 2;
			end = m.end() - 1;
			m.appendReplacement(sb, String.valueOf(single(s.substring(start, end))));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public static char single(String sub) {
		return single(Integer.parseInt(sub));
	}

	public static char single(int n) {
		return (char) n;
	}
}
