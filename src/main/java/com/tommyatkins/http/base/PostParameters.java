package com.tommyatkins.http.base;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

public class PostParameters extends ArrayList<KeyValuePair> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1340570732996012851L;
	private String encoding;

	public PostParameters(String encoding) {
		super();
		this.encoding = encoding;
	}

	public PostParameters(String key, String value) {
		this("UTF-8");
		this.add(key, value);
	}

	public PostParameters add(String key, String value) {
		this.add(new KeyValuePair(key, value));

		return this;
	}

	public String format() throws IOException {
		StringBuffer param = new StringBuffer();
		if (this.size() > 0) {
			Iterator<KeyValuePair> pairs = this.iterator();
			if (pairs.hasNext()) {
				KeyValuePair pair = pairs.next();
				param.append(pair.getKey());
				param.append("=");
				param.append(URLEncoder.encode(pair.getValue(), encoding));
			}
			while (pairs.hasNext()) {
				KeyValuePair pair = pairs.next();
				param.append("&");
				param.append(pair.getKey());
				param.append("=");
				param.append(URLEncoder.encode(pair.getValue(), encoding));
			}
		}
		return param.toString();
	}

}
