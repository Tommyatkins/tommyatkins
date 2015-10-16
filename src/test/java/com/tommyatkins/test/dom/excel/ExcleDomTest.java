package com.tommyatkins.test.dom.excel;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcleDomTest {

	private final static int SHEET_AT = 0;
	private final static int ANALYSIS_START_INDEX = 2;
	private final static int SOURCE_COLUMN_INDEX = 1;
	private final static int TRANSLATION_COLUMN_INDEX = 2;

	private final static String PARAMETER_FLAG_START = "\\{";
	private final static String PARAMETER_FLAG_END = "\\}";
	private final static String ALLOW_MARK = ":_";

	private final static String FINAL_PARAMETER_PATTERN = "\\{[0-9a-zA-Z:_]+\\}";

	public static void main(String[] args) throws Exception {
		load("C:\\Users\\pc\\Desktop\\example.xlsx");
	}

	public static void load(String path) throws Exception {
		FileInputStream fis = new FileInputStream(path);
		Workbook workbook = WorkbookFactory.create(fis);
		Sheet sheet = workbook.getSheetAt(SHEET_AT);
		int totalRow = sheet.getPhysicalNumberOfRows();
		for (int i = ANALYSIS_START_INDEX; i < totalRow; i++) {
			Row row = sheet.getRow(i);
			String source = row.getCell(SOURCE_COLUMN_INDEX).getStringCellValue();
			String translation = row.getCell(TRANSLATION_COLUMN_INDEX).getStringCellValue();
			String[] unmatchs = findUnmatchParameters(translation, searchParameters(source));
			if (unmatchs.length == 0) {
				System.out.println(String.format("row %d is ok.", i + 1));
			} else {
				System.out.println(String.format("row %d has unmatch parameter:", i + 1));
				for (String unmatch : unmatchs) {
					System.out.println(unmatch);
				}
			}
		}
	}

	public static String[] searchParameters(String source) {
		String rule = FINAL_PARAMETER_PATTERN == null || FINAL_PARAMETER_PATTERN.trim().isEmpty() ? String.format("%s[0-9a-zA-Z%s]+%s",
				PARAMETER_FLAG_START, ALLOW_MARK, PARAMETER_FLAG_END) : FINAL_PARAMETER_PATTERN;
		Pattern p = Pattern.compile(rule);
		Matcher m = p.matcher(source);
		List<String> parameters = new ArrayList<String>();
		while (m.find()) {
			parameters.add(source.substring(m.start(), m.end()));
		}
		return parameters.toArray(new String[] {});
	}

	public static String[] findUnmatchParameters(String translation, String[] parameters) {
		List<String> unmatchs = new ArrayList<String>();
		for (String parameter : parameters) {
			if (translation.indexOf(parameter) < 0) {
				unmatchs.add(parameter);
			}
		}
		return unmatchs.toArray(new String[] {});
	}
}
