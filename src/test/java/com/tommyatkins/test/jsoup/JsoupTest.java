package com.tommyatkins.test.jsoup;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class JsoupTest {

	public static void main(String[] args) throws IOException {

		String content = baidu("睿峰科技");
		String noScript = content;
		ByteBuffer data = ByteBuffer.wrap(noScript.getBytes("UTF-8"));
		try (FileOutputStream fileOutputStream = new FileOutputStream("clone.html");

		FileChannel fileChannel = fileOutputStream.getChannel();) {
			fileChannel.write(data);
			fileChannel.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String baidu(String keyword) throws IOException {
		Connection conn = Jsoup.connect("http://news.baidu.com/ns");
		conn.data("word", keyword);
		Document docoment = conn.get();
		Element container = docoment.getElementById("container");
		Elements results = container.select("li.result");
		for (Element result : results) {
			String summary = result.getElementsByClass("c-summary").first().text().replace("百度快照", "");
			System.out.println(summary);
		}
		return docoment.html();
	}

	public static String noScript(String script) {
		Whitelist whitelist = Whitelist.relaxed();
		return Jsoup.clean(script, whitelist );
	}

	public static Document html2dom(String html) {
		return Jsoup.parse(html);
	}
}
