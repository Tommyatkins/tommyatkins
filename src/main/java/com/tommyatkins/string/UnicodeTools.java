package com.tommyatkins.string;

public class UnicodeTools {

	private final static int CHINEXE_BEGIN = 0x4E00;
	private final static int CHINEXE_END = 0x9FBF;

	/**
	 * 字符串全部转换
	 * 
	 * @param s
	 * @return
	 */
	public static String string2unicode(String s) {
		StringBuilder u = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			int c = (char) s.charAt(i);
			u.append("\\u").append(Integer.toHexString(c));
		}
		return u.toString();
	}

	/**
	 * 完全的UNICODE字符串转换，不能包括其他类型的编码字符
	 * 
	 * @param u
	 * @return
	 */
	public static String unicode2string(String u) {
		String[] codes = u.split("\\\\u");
		StringBuilder s = new StringBuilder();
		for (int i = 1; i < codes.length; i++) {
			char c = (char) Integer.parseInt(codes[i], 16);
			s.append(c);
		}
		return s.toString();
	}

	/**
	 * 只转换中文的部分，其他保留
	 * 
	 * @param s
	 * @return
	 */
	public static String chinese2string(String s) {
		return string2unicodeLimitedDistance(s, CHINEXE_BEGIN, CHINEXE_END);
	}

	/**
	 * 只转换中文UNICODE范围的部分，其他保留
	 * 
	 * @param u
	 * @return
	 */
	public static String string2chinese(String u) {
		String[] codes = u.split("\\\\u");
		StringBuilder s = new StringBuilder();
		if (codes.length > 1) {
			s.append(codes[0]);
			for (int i = 1; i < codes.length; i++) {
				String code = codes[i];
				int cl = code.length();
				if (cl >= 4) {
					String t_code = code.substring(0, 4);
					char c = (char) Integer.parseInt(t_code, 16);
					s.append(c);
					if (cl > 4) {
						String l_code = code.substring(4, cl);
						s.append(l_code);
					}
				} else {
					if (cl > 0) {
						s.append(code);
					}
				}
			}
		}

		return s.toString();
	}
	
	/**
	 * 
	 * @param utfString
	 * @return
	 */
	public static String convert(String utfString){
		StringBuilder sb = new StringBuilder();
		int i = -1;
		int pos = 0;
		
		while((i=utfString.indexOf("\\u", pos)) != -1){
			sb.append(utfString.substring(pos, i));
			if(i+5 < utfString.length()){
				pos = i+6;
				sb.append((char)Integer.parseInt(utfString.substring(i+2, i+6), 16));
			}
		}
		sb.append(utfString.substring(pos));		
		return sb.toString();
	}

	public static String string2unicodeLimitedDistance(String s, int begin, int end) {
		StringBuilder u = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char ca = s.charAt(i);
			int c = (char) ca;
			if (c >= begin && c <= end) {
				u.append("\\u").append(Integer.toHexString(c));
			} else {
				u.append(ca);
			}

		}
		return u.toString();
	}
}
