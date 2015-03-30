package com.tommyatkins.freemarker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreeMarker {
	public final Configuration cfg;
	public static FreeMarker store = null;

	private FreeMarker(Configuration cfg) {
		this.cfg = cfg;
	}

	public static void init(Configuration cfg) {
		if (store == null) {
			store = new FreeMarker(cfg);
		}
	}

	public static Template getTemplate(String templatePath) throws IOException {
		return store.getConfiguration().getTemplate(templatePath);
	}

	private Configuration getConfiguration() {
		return cfg;
	}

	public static class out {

		/**
		 * 
		 * @param templatePath
		 * @param map
		 * @param os
		 * @throws TemplateException
		 * @throws IOException
		 */
		public static void write(String templatePath, Map<String, Object> map, OutputStream os) throws TemplateException, IOException {
			write(getTemplate(templatePath), map, new OutputStreamWriter(os));
		}

		/**
		 * 
		 * @param templateName
		 * @param map
		 * @param writer
		 * @throws TemplateException
		 * @throws IOException
		 */
		public static void write(String templatePath, Map<String, Object> map, Writer writer) throws TemplateException, IOException {
			write(getTemplate(templatePath), map, writer);
		}

		/**
		 * 
		 * @param tmp
		 * @param map
		 * @param os
		 * @throws TemplateException
		 * @throws IOException
		 */
		public static void write(Template tmp, Map<String, Object> map, OutputStream os) throws TemplateException, IOException {
			write(tmp, map, new OutputStreamWriter(os));
		}

		/**
		 * 
		 * @param tmp
		 * @param map
		 * @param writer
		 * @throws TemplateException
		 * @throws IOException
		 */
		public static void write(Template tmp, Map<String, Object> map, Writer writer) throws TemplateException, IOException {
			tmp.process(map, writer);
		}
	}
}
